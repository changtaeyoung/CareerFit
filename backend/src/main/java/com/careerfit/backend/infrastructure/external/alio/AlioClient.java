package com.careerfit.backend.infrastructure.external.alio;

import com.careerfit.backend.infrastructure.external.alio.dto.AlioBizResponse;
import com.careerfit.backend.infrastructure.external.alio.dto.AlioInstitutionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * ALIO Open API HTTP 클라이언트.
 *
 * ── 제공하는 API ──────────────────────────────────────────────────────────
 * 1) fetchInstitution(instNm) — 기관 정보 조회 (기관코드, 설립연도, 임직원 수 등)
 * 2) fetchBizInfo(instCode)   — 사업 정보 조회 (사업 개요)
 *
 * ── 왜 API별로 키가 다른가 ────────────────────────────────────────────────
 * ALIO는 기관정보/사업정보/시설정보/행사정보를 별개 API로 운영하며
 * 각각 별도 키를 발급한다. CareerFit은 기관정보와 사업정보만 사용.
 *
 * ── ALIO API의 특성 ───────────────────────────────────────────────────────
 * - 기관명 부분 검색 지원: "한국전력"으로 조회 시 "한국전력공사" 반환 가능
 * - 응답 건수가 여러 개일 수 있으므로 서비스 레이어에서 정확한 기관명으로 필터링
 * - type=json 파라미터로 JSON 응답 요청 (기본은 XML)
 */
@Slf4j
@Component
public class AlioClient {

    private final RestClient restClient = RestClient.create();

    @Value("${alio.base-url}")
    private String baseUrl;

    @Value("${alio.institution-key}")
    private String institutionKey;

    @Value("${alio.business-key}")
    private String businessKey;

    /**
     * 기관명으로 ALIO 기관 정보를 조회한다.
     *
     * @param instNm 기관명 (예: "한국전력공사")
     * @return AlioInstitutionResponse. 데이터 없거나 실패 시 빈 리스트 포함 객체 반환
     *
     * ── 검색어 전략 ──────────────────────────────────────────────────────
     * ALIO는 정확한 기관명이 아니어도 부분 일치로 검색되지만,
     * 여러 건이 반환될 수 있으므로 서비스에서 정확한 이름으로 1건 선택.
     * 예: "한국전력" → ["한국전력공사", "한국전력기술", ...] 중 정확 일치 선택
     */
    public AlioInstitutionResponse fetchInstitution(String instNm) {
        log.debug("[AlioClient] 기관 정보 조회 — instNm: {}", instNm);
        try {
            String uri = UriComponentsBuilder
                    .fromHttpUrl(baseUrl + "/getPublicInstInfo.do")
                    .queryParam("apiKey", institutionKey)
                    .queryParam("type", "json")
                    .queryParam("instNm", instNm)
                    .encode()
                    .toUriString();

            AlioInstitutionResponse response = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(AlioInstitutionResponse.class);

            if (response == null || response.getList().isEmpty()) {
                log.debug("[AlioClient] 기관 정보 없음 — instNm: {}", instNm);
                return new AlioInstitutionResponse();
            }

            log.debug("[AlioClient] 기관 정보 조회 완료 — instNm: {}, 결과: {}건", instNm, response.getList().size());
            return response;

        } catch (Exception e) {
            log.error("[AlioClient] 기관 정보 조회 실패 — instNm: {}, 에러: {}", instNm, e.getMessage());
            return new AlioInstitutionResponse();
        }
    }

    /**
     * ALIO 기관 코드로 사업 정보를 조회한다.
     *
     * @param instCode ALIO 기관 코드 (fetchInstitution()으로 얻은 값)
     * @return AlioBizResponse. 데이터 없거나 실패 시 빈 리스트 포함 객체 반환
     */
    public AlioBizResponse fetchBizInfo(String instCode) {
        log.debug("[AlioClient] 사업 정보 조회 — instCode: {}", instCode);
        try {
            String uri = UriComponentsBuilder
                    .fromHttpUrl(baseUrl + "/getPublicInstBizInfo.do")
                    .queryParam("apiKey", businessKey)
                    .queryParam("type", "json")
                    .queryParam("instCode", instCode)
                    .encode()
                    .toUriString();

            AlioBizResponse response = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(AlioBizResponse.class);

            if (response == null || response.getList().isEmpty()) {
                log.debug("[AlioClient] 사업 정보 없음 — instCode: {}", instCode);
                return new AlioBizResponse();
            }

            log.debug("[AlioClient] 사업 정보 조회 완료 — instCode: {}, 결과: {}건", instCode, response.getList().size());
            return response;

        } catch (Exception e) {
            log.error("[AlioClient] 사업 정보 조회 실패 — instCode: {}, 에러: {}", instCode, e.getMessage());
            return new AlioBizResponse();
        }
    }
}
