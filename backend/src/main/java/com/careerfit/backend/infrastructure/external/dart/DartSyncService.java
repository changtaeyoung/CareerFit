package com.careerfit.backend.infrastructure.external.dart;

import com.careerfit.backend.domain.company.entity.Company;
import com.careerfit.backend.domain.company.entity.CompanySalary;
import com.careerfit.backend.domain.company.mapper.CompanyMapper;
import com.careerfit.backend.domain.company.mapper.CompanySalaryMapper;
import com.careerfit.backend.infrastructure.external.dart.dto.CorpCodeItem;
import com.careerfit.backend.infrastructure.external.dart.dto.EmpSttusResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * DART 기업 코드(corp_code) 자동 매핑 서비스.
 *
 * ── 역할 ──────────────────────────────────────────────────────────────────
 * 1) DART에서 전체 기업 코드 목록 다운로드 (수만 건)
 * 2) 우리 DB에서 dart_code가 없는 기업 조회
 * 3) 기업명 정규화 후 매칭 → dart_code 업데이트
 *
 * ── @Async를 붙인 이유 ────────────────────────────────────────────────────
 * corpCode.xml 다운로드(ZIP, 수MB) + 파싱(수만 건) + DB UPDATE가 합쳐져서
 * 수 초~수십 초가 걸린다. 관리자가 트리거 버튼을 눌렀을 때 즉시 "동기화 시작됨"
 * 응답을 받고 백그라운드에서 실행되도록 @Async 처리.
 *
 * ── @Async + @Transactional 동시 사용 주의사항 ───────────────────────────
 * 호출자(Controller)의 트랜잭션은 @Async 스레드로 전파되지 않는다.
 * 따라서 이 메서드 자체에 @Transactional을 선언해야 DB 변경이 커밋된다.
 * (ASYNC_GUIDE.md 참고)
 *
 * ── 기업명 정규화 전략 ────────────────────────────────────────────────────
 * DART 등록명과 우리 DB 기업명이 다를 수 있다:
 *   DART: "(주)하나은행", "하나은행주식회사"
 *   우리: "하나은행"
 * 정규화: (주), ㈜, 주식회사 등의 법인 식별자를 제거하고 공백을 제거하여 비교.
 *
 * 정규화로도 매칭 안 되는 케이스는 수동 별칭(alias) 테이블로 처리:
 *   예) "토스" → DART에는 "비바리퍼블리카"
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DartSyncService {

    private final DartClient dartClient;
    private final CompanyMapper companyMapper;
    private final CompanySalaryMapper companySalaryMapper;

    /**
     * 수동 별칭 테이블.
     * 우리 DB 기업명이 DART 등록명과 완전히 달라서 정규화로도 매칭 불가한 경우.
     *
     * 키: 우리 DB의 company.name
     * 값: DART에 등록된 corp_name (정규화 전 원문)
     *
     * ── 각 별칭의 이유 ────────────────────────────────────────────────
     * - 토스          : 법인명은 "비바리퍼블리카"
     * - NH농협은행    : DART에는 "농협은행"으로 등록
     * - KB국민은행    : DART에는 "국민은행"으로 등록 (지주 통합 전 명칭)
     * - 삼성화재      : DART 등록명이 "삼성화재해상보험"
     * - IBK기업은행   : DART 등록명이 "기업은행"
     *
     * ── DART 미등록 공공기관 (ALIAS_MAP 추가 불필요, ALIO에서 처리) ──
     * - 국민건강보험공단 : 준정부기관, DART 공시 의무 없음
     * - 건강보험심사평가원 : 동일
     * 위 두 기관의 dart_code는 NULL 유지. ALIO API 연동 시 처리.
     */
    private static final Map<String, String> ALIAS_MAP = Map.of(
        "토스",        "비바리퍼블리카",
        "NH농협은행",  "농협은행",
        "KB국민은행",  "국민은행",
        "삼성화재",    "삼성화재해상보험",
        "IBK기업은행", "기업은행"
    );

    /**
     * DART corp_code 일괄 동기화.
     * 관리자가 /api/admin/dart/sync-corp-code 를 호출하면 이 메서드가 실행된다.
     *
     * @Async: CareerFit-Async-N 스레드에서 백그라운드 실행
     * @Transactional: @Async 스레드는 호출자 트랜잭션을 이어받지 못하므로 자체 선언
     */
    @Async
    @Transactional
    public void syncCorpCodes() {
        log.info("[DartSyncService] DART corp_code 동기화 시작");

        // 1. DART에서 전체 기업 코드 목록 다운로드 + 파싱
        List<CorpCodeItem> corpCodes = dartClient.fetchCorpCodes();
        if (corpCodes.isEmpty()) {
            log.warn("[DartSyncService] DART 기업 코드 목록이 비어있음 — 동기화 중단");
            return;
        }

        // 2. DART 목록을 "정규화된 기업명 → CorpCodeItem" Map으로 변환 (빠른 조회를 위해)
        // 정규화: (주), ㈜, 주식회사, 공백 제거 → 소문자 → 중복 시 첫 번째 값 유지
        Map<String, CorpCodeItem> normalizedMap = corpCodes.stream()
                .collect(Collectors.toMap(
                        item -> normalizeName(item.getCorpName()),
                        item -> item,
                        (existing, duplicate) -> existing  // 동명 기업 있으면 첫 번째 유지
                ));

        // 3. 우리 DB에서 dart_code가 없는 기업 목록 조회
        List<Company> targets = companyMapper.selectCompaniesWithoutDartCode();
        log.info("[DartSyncService] dart_code 미설정 기업 {}개 매칭 시도", targets.size());

        int successCount = 0;
        int failCount = 0;

        for (Company company : targets) {
            String matched = matchCorpCode(company.getName(), normalizedMap);
            if (matched != null) {
                companyMapper.updateDartCode(company.getId(), matched);
                log.info("[DartSyncService] ✅ 매칭 성공: {} → {}", company.getName(), matched);
                successCount++;
            } else {
                log.warn("[DartSyncService] ❌ 매칭 실패: {} (수동 입력 필요)", company.getName());
                failCount++;
            }
        }

        log.info("[DartSyncService] DART corp_code 동기화 완료 — 성공: {}개, 실패: {}개",
                successCount, failCount);
    }

    /**
     * 기업명을 기반으로 DART corp_code를 매칭한다.
     *
     * 매칭 우선순위:
     *   1) 별칭 테이블(ALIAS_MAP) 확인 → 있으면 별칭으로 DART 검색
     *   2) 정규화된 이름으로 정확히 일치하는 항목 검색
     *
     * @return 매칭된 corp_code (8자리), 없으면 null
     */
    private String matchCorpCode(String companyName, Map<String, CorpCodeItem> normalizedMap) {
        // 1) 별칭 테이블 확인
        String alias = ALIAS_MAP.get(companyName);
        String searchName = alias != null ? alias : companyName;

        // 2) 정규화 후 Map에서 조회
        String normalizedSearch = normalizeName(searchName);
        CorpCodeItem found = normalizedMap.get(normalizedSearch);

        // 매칭 실패 시 — 유사 이름 후보 3개 출력 (디버깅용)
        if (found == null) {
            log.debug("[DartSyncService] '{}' 매칭 실패. 정규화된 검색어: '{}'. 유사 후보: {}",
                    companyName,
                    normalizedSearch,
                    normalizedMap.keySet().stream()
                            .filter(k -> k.contains(normalizedSearch.substring(0, Math.min(3, normalizedSearch.length()))))
                            .limit(5)
                            .toList()
            );
        }

        return found != null ? found.getCorpCode() : null;
    }

    /**
     * 기업명 정규화 함수.
     *
     * 제거 대상: (주), ㈜, 주식회사, (유), 유한회사, 공백, 특수문자
     *
     * ── 정규식 설명 ───────────────────────────────────────────────────
     * \\(주\\)  → "(주)" 제거
     * ㈜       → "㈜" 제거
     * 주식회사  → "주식회사" 제거 (앞뒤 위치 무관)
     * \\(유\\) → "(유)" 제거
     * 유한회사  → "유한회사" 제거
     * [\\s·]+ → 공백, 중점 제거
     */
    private String normalizeName(String name) {
        if (name == null) return "";
        return name
                .replaceAll("\\(주\\)", "")
                .replaceAll("㈜", "")
                .replaceAll("주식회사", "")
                .replaceAll("\\(유\\)", "")
                .replaceAll("유한회사", "")
                .replaceAll("[\\s·]+", "")
                .toLowerCase();
    }

    // ── DART empSttus 평균 연봉 수집 ──────────────────────────────────────

    /**
     * dart_code가 있는 기업의 평균 연봉을 DART에서 수집하여 company_salary에 저장.
     *
     * ── 흐름 ──────────────────────────────────────────────────────────
     * 1) dart_code가 있는 기업 목록 조회
     * 2) 각 기업에 대해 DART empSttus API 호출 (사업연도 파라미터 수신)
     * 3) "합계" 행에서 avg_salary 추출
     * 4) company_salary에 UPSERT (이미 있으면 금액 업데이트)
     *
     * ── @Async + @Transactional ────────────────────────────────────────
     * 기업 수만큼 API 호출이 발생하므로 백그라운드 실행.
     * 각 기업별 DB 저장은 하나의 트랜잭션으로 묶임.
     *
     * ── bsnsYear 파라미터 ─────────────────────────────────────────────
     * 어느 사업연도 데이터를 가져올지 호출 시점에 결정.
     * 예: "2024" → 2024년 사업보고서의 직원 현황
     * DART는 보통 이듬해 3~4월에 전년도 사업보고서를 공시하므로
     * 매년 4월 이후에 전년도 데이터를 수집하는 것이 현실적.
     *
     * @param bsnsYear 수집할 사업연도 (예: "2024")
     */
    @Async
    @Transactional
    public void syncAverageSalary(String bsnsYear) {
        log.info("[DartSyncService] DART 평균 연봉 수집 시작 — 사업연도: {}", bsnsYear);

        List<Company> targets = companyMapper.selectCompaniesWithDartCode();
        log.info("[DartSyncService] 수집 대상 기업: {}개", targets.size());

        int successCount = 0;
        int skipCount    = 0;
        int failCount    = 0;

        for (Company company : targets) {
            try {
                EmpSttusResponse response = dartClient.fetchEmpSttus(company.getDartCode(), bsnsYear);

                if (response == null || response.getList() == null || response.getList().isEmpty()) {
                    log.debug("[DartSyncService] 데이터 없음 — {}", company.getName());
                    skipCount++;
                    continue;
                }

                // "합계" 행 추출 (성별 합계, 고용형태 합계)
                Integer avgSalary = response.getList().stream()
                        .filter(EmpSttusResponse.EmpSttusItem::isTotalRow)
                        .findFirst()
                        .map(EmpSttusResponse.EmpSttusItem::parseAmount)
                        .orElse(null);

                // 합계 행이 없으면 첫 번째 행으로 fallback
                if (avgSalary == null && !response.getList().isEmpty()) {
                    EmpSttusResponse.EmpSttusItem first = response.getList().get(0);
                    // 실제 응답 구조 확인용 (디버깅)
                    log.info("[DartSyncService] {} 응답 상세 — 행 수: {}, 첫 행: fo_bbm={}, sexdstn={}, avg_salary='{}'",
                            company.getName(),
                            response.getList().size(),
                            first.getFoBbm(),
                            first.getSexdstn(),
                            first.getAvgSalary());
                    avgSalary = first.parseAmount();
                    log.debug("[DartSyncService] 합계 행 없음 — {} 첫 번째 행 사용", company.getName());
                }

                if (avgSalary == null) {
                    // avg_salary=null 케이스 — 금융업/보험업 서식 특성
                    // DART empSttus는 제조업 위주 서식이라 금융업은 이 필드가 비어있음.
                    // → Phase 2에서 DART 사업보고서 PDF 직접 파싱으로 대체 예정
                    // → 공기업은 ALIO API/크롤링으로 별도 처리
                    log.info("[DartSyncService] ⏭️ {} — empSttus avg_salary 없음 (금융업/공기업 서식). ALIO 또는 사업보고서 파싱으로 처리 예정",
                            company.getName());
                    skipCount++;
                    continue;
                }

                // company_salary UPSERT
                companySalaryMapper.upsertSalary(CompanySalary.builder()
                        .companyId(company.getId())
                        .salaryType("AVERAGE")
                        .amount(avgSalary)
                        .year(Integer.parseInt(bsnsYear))
                        .source("DART_API")
                        .sourceUrl("https://dart.fss.or.kr/corp/search.ax?textCrpNm=" + company.getDartCode())
                        .collectedAt(java.time.LocalDateTime.now())
                        .build());

                log.info("[DartSyncService] ✅ 연봉 저장 완료: {} → {}만원 ({}년)",
                        company.getName(), avgSalary, bsnsYear);
                successCount++;

                // DART API 과호출 방지 — 기업당 0.5초 간격
                Thread.sleep(500);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("[DartSyncService] 인터럽트 발생 — 수집 중단");
                break;
            } catch (Exception e) {
                log.error("[DartSyncService] {} 연봉 수집 실패: {}", company.getName(), e.getMessage());
                failCount++;
            }
        }

        log.info("[DartSyncService] DART 평균 연봉 수집 완료 — 성공: {}개, 스킵: {}개, 실패: {}개",
                successCount, skipCount, failCount);
    }
}
