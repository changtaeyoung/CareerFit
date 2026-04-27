package com.careerfit.backend.infrastructure.external.alio;

import com.careerfit.backend.domain.company.entity.Company;
import com.careerfit.backend.domain.company.mapper.CompanyMapper;
import com.careerfit.backend.infrastructure.external.alio.dto.AlioBizResponse;
import com.careerfit.backend.infrastructure.external.alio.dto.AlioInstitutionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * ALIO 기관 정보 동기화 서비스.
 *
 * ── 역할 ──────────────────────────────────────────────────────────────────
 * 1) alio_code가 없는 공기업 목록 조회
 * 2) ALIO 기관 정보 API로 기관 코드(alio_code), 임직원 수, 설립연도 수집
 * 3) ALIO 사업 정보 API로 사업 개요(business_overview) 수집
 * 4) company 테이블 업데이트
 *
 * ── 왜 공기업만 대상인가 ─────────────────────────────────────────────────
 * ALIO는 공공기관 경영정보 공개 시스템이므로 민간 기업(KB국민은행, 삼성생명 등)은
 * ALIO에 등록되어 있지 않다. alio_code가 채워질 기업은 사실상 공기업/준정부기관뿐.
 *
 * ── 기관명 매칭 전략 ─────────────────────────────────────────────────────
 * ALIO API는 instNm(기관명)으로 부분 검색을 지원한다.
 * 여러 건이 반환될 수 있으므로 정확히 일치하는 기관명을 우선 선택하고,
 * 없으면 포함 관계로 첫 번째 매칭을 선택한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlioSyncService {

    private final AlioClient alioClient;
    private final CompanyMapper companyMapper;

    /**
     * ALIO 기관 정보 일괄 동기화.
     *
     * @Async: 기업 수만큼 API 호출이 발생하므로 백그라운드 실행
     * @Transactional: @Async 스레드 자체 트랜잭션 선언
     */
    @Async
    @Transactional
    public void syncAlioInfo() {
        log.info("[AlioSyncService] ALIO 기관 정보 동기화 시작");

        // alio_code 없는 기업 전체 조회 (민간 기업도 포함되지만 ALIO에 없으면 스킵됨)
        List<Company> targets = companyMapper.selectCompaniesWithoutAlioCode();
        log.info("[AlioSyncService] 동기화 대상 기업: {}개", targets.size());

        int successCount = 0;
        int skipCount    = 0;

        for (Company company : targets) {
            try {
                // 1) 기관 정보 조회
                AlioInstitutionResponse instResponse = alioClient.fetchInstitution(company.getName());
                AlioInstitutionResponse.InstItem instItem = findBestMatch(instResponse.getList(), company.getName());

                if (instItem == null) {
                    // 민간 기업이나 ALIO에 미등록 기관은 정상적으로 스킵
                    log.debug("[AlioSyncService] ⏭️ ALIO 미등록 기관 스킵: {}", company.getName());
                    skipCount++;
                    // API 과호출 방지
                    Thread.sleep(300);
                    continue;
                }

                log.info("[AlioSyncService] ✅ 기관 매칭: {} → ALIO코드: {}", company.getName(), instItem.getInstCode());

                // 2) 사업 정보 조회 (기관 코드로)
                String businessOverview = null;
                AlioBizResponse bizResponse = alioClient.fetchBizInfo(instItem.getInstCode());
                if (!bizResponse.getList().isEmpty()) {
                    // 가장 최근 연도 데이터 선택 (BIZ_YEAR 기준)
                    businessOverview = bizResponse.getList().stream()
                            .findFirst()
                            .map(AlioBizResponse.BizItem::extractOverview)
                            .orElse(null);
                }

                // 3) company 테이블 업데이트
                companyMapper.updateAlioInfo(
                        company.getId(),
                        instItem.getInstCode(),
                        instItem.parseEmpCnt(),
                        instItem.parseFoundedYear(),
                        businessOverview
                );

                log.info("[AlioSyncService] ✅ 기관 정보 저장 완료: {} — 임직원: {}명, 설립: {}년",
                        company.getName(), instItem.parseEmpCnt(), instItem.parseFoundedYear());
                successCount++;

                // ALIO API 과호출 방지 — 기업당 0.3초 간격
                Thread.sleep(300);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("[AlioSyncService] 인터럽트 발생 — 동기화 중단");
                break;
            } catch (Exception e) {
                log.error("[AlioSyncService] {} 동기화 실패: {}", company.getName(), e.getMessage());
            }
        }

        log.info("[AlioSyncService] ALIO 기관 정보 동기화 완료 — 성공: {}개, 스킵(ALIO 미등록): {}개",
                successCount, skipCount);
    }

    /**
     * ALIO API 결과에서 우리 DB 기업명과 가장 잘 맞는 항목을 선택한다.
     *
     * 우선순위:
     *   1) instNm이 정확히 일치하는 항목
     *   2) instNm이 우리 DB 이름을 포함하는 항목 (또는 반대)
     *   3) 없으면 null (스킵 대상)
     *
     * ── 왜 단순 첫 번째 선택이 아닌가 ───────────────────────────────────
     * ALIO 부분 검색은 "한국전력"으로 조회 시 "한국전력공사", "한국전력기술",
     * "한국전력거래소" 등 여러 건이 반환될 수 있다.
     * 잘못된 기관 코드가 저장되면 이후 사업 정보 조회와 경영공시 크롤링이
     * 모두 틀린 기관 기준으로 동작하므로 정확한 매칭이 중요하다.
     */
    private AlioInstitutionResponse.InstItem findBestMatch(
            List<AlioInstitutionResponse.InstItem> items, String targetName) {

        if (items == null || items.isEmpty()) return null;

        // 1) 정확 일치
        return items.stream()
                .filter(item -> targetName.equals(item.getInstNm()))
                .findFirst()
                // 2) 포함 관계 매칭
                .orElseGet(() -> items.stream()
                        .filter(item -> item.getInstNm() != null &&
                                (item.getInstNm().contains(targetName) ||
                                 targetName.contains(item.getInstNm())))
                        .findFirst()
                        .orElse(null));
    }
}
