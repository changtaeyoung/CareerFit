package com.careerfit.backend.infrastructure.external.dart;

import com.careerfit.backend.infrastructure.external.dart.dto.CorpCodeItem;
import com.careerfit.backend.infrastructure.external.dart.dto.EmpSttusResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * DART Open API HTTP 클라이언트.
 *
 * ── 제공하는 API ──────────────────────────────────────────────────────────
 * 1) fetchCorpCodes()  — corpCode.xml (전체 기업코드 목록, ZIP)
 * 2) fetchEmpSttus()   — empSttus.json (직원 현황 + 평균 급여)
 *
 * ── 이 클래스의 역할 ──────────────────────────────────────────────────────
 * 외부 API 호출의 세부 사항(URL, 헤더, 응답 파싱)을 캡슐화한다.
 * 서비스 레이어(DartSyncService)는 이 클래스만 의존하고,
 * DART API의 URL이 바뀌거나 인증 방식이 바뀌어도 서비스는 변경이 없다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DartClient {

    private final DartCorpCodeParser corpCodeParser;
    private final RestClient restClient = RestClient.create();

    @Value("${dart.api-key}")
    private String apiKey;

    @Value("${dart.base-url}")
    private String baseUrl;

    // ── corpCode.xml ───────────────────────────────────────────────────────

    public List<CorpCodeItem> fetchCorpCodes() {
        log.info("[DartClient] corpCode.xml 다운로드 시작 — API Key 설정 여부: {}",
                apiKey != null && !apiKey.isBlank() ? "설정됨 (앞 4자리: " + apiKey.substring(0, Math.min(4, apiKey.length())) + "...)" : "❌ 비어있음");
        try {
            byte[] zipBytes = restClient.get()
                    .uri(baseUrl + "/corpCode.xml?crtfc_key=" + apiKey)
                    .retrieve()
                    .body(byte[].class);

            if (zipBytes == null || zipBytes.length == 0) {
                log.warn("[DartClient] corpCode.xml 응답이 비어 있음");
                return List.of();
            }

            log.info("[DartClient] ZIP 다운로드 완료 — {}bytes", zipBytes.length);

            // 너무 작으면 에러 응답 (정상 ZIP은 수MB)
            if (zipBytes.length < 500) {
                log.warn("[DartClient] 응답 크기가 너무 작음 ({}bytes). 에러 응답 내용: {}",
                        zipBytes.length, new String(zipBytes, java.nio.charset.StandardCharsets.UTF_8));
                return List.of();
            }

            return parseZip(zipBytes);

        } catch (Exception e) {
            log.error("[DartClient] corpCode.xml 다운로드 실패: {}", e.getMessage(), e);
            return List.of();
        }
    }

    private List<CorpCodeItem> parseZip(byte[] zipBytes) {
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                log.debug("[DartClient] ZIP 내부 파일: {}", entry.getName());
                if (entry.getName().toLowerCase().endsWith(".xml")) {
                    log.info("[DartClient] XML 파일 발견: {} — 파싱 시작", entry.getName());
                    return corpCodeParser.parse(zis);
                }
                zis.closeEntry();
            }
            log.warn("[DartClient] ZIP 내부에 XML 파일 없음");
        } catch (Exception e) {
            log.error("[DartClient] ZIP 파싱 실패: {}", e.getMessage(), e);
        }
        return List.of();
    }


    /**
     * 특정 기업의 직원 현황(평균 급여 포함)을 조회한다.
     *
     * @param corpCode  DART 기업 고유코드 (8자리)
     * @param bsnsYear  사업연도 (예: "2024")
     * @return EmpSttusResponse (status="000"이면 정상, list에 직원 현황 데이터)
     */
    public EmpSttusResponse fetchEmpSttus(String corpCode, String bsnsYear) {
        log.debug("[DartClient] empSttus 조회 — corpCode: {}, year: {}", corpCode, bsnsYear);
        try {
            EmpSttusResponse response = restClient.get()
                    .uri(baseUrl + "/empSttus.json"
                            + "?crtfc_key=" + apiKey
                            + "&corp_code=" + corpCode
                            + "&bsns_year=" + bsnsYear
                            + "&reprt_code=11011")
                    .retrieve()
                    .body(EmpSttusResponse.class);

            if (response == null) {
                log.warn("[DartClient] empSttus 응답 null — corpCode: {}", corpCode);
                return null;
            }

            if (!response.isSuccess()) {
                // "013" = 데이터 없음 (해당 연도 미공시), 정상적인 케이스
                log.debug("[DartClient] empSttus 데이터 없음 — corpCode: {}, status: {}, msg: {}",
                        corpCode, response.getStatus(), response.getMessage());
                return null;
            }

            return response;

        } catch (Exception e) {
            log.error("[DartClient] empSttus 조회 실패 — corpCode: {}, 에러: {}", corpCode, e.getMessage());
            return null;
        }
    }
}
