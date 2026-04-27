package com.careerfit.backend.infrastructure.external.alio.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * ALIO 사업 정보 API 응답 DTO.
 *
 * ── API 엔드포인트 ────────────────────────────────────────────────────────
 * GET https://www.alio.go.kr/openApi/getPublicInstBizInfo.do
 *   ?apiKey={사업정보_API_KEY}
 *   &type=json
 *   &instCode={ALIO 기관 코드}   ← AlioInstitutionResponse.InstItem.instCode
 *
 * ── 응답 구조 ─────────────────────────────────────────────────────────────
 * {
 *   "result": {
 *     "list": [{
 *       "INST_NM": "한국전력공사",
 *       "MAIN_BIZ_NM": "전력 생산 및 공급",
 *       "BIZ_SUMMARY": "한국전력공사는 전기의 생산, 송전, 변전, 배전 및 판매...",
 *       "BIZ_YEAR": "2024"
 *     }]
 *   }
 * }
 *
 * ── 사용 목적 ─────────────────────────────────────────────────────────────
 * company.business_overview 필드를 채우는 데 사용.
 * BIZ_SUMMARY가 있으면 그것을, 없으면 MAIN_BIZ_NM을 사용.
 */
@Getter
@NoArgsConstructor
public class AlioBizResponse {

    @JsonProperty("result")
    private Result result;

    public List<BizItem> getList() {
        if (result == null || result.getList() == null) return List.of();
        return result.getList();
    }

    @Getter
    @NoArgsConstructor
    public static class Result {
        @JsonProperty("list")
        private List<BizItem> list;
    }

    @Getter
    @NoArgsConstructor
    public static class BizItem {

        @JsonProperty("INST_NM")
        private String instNm;

        /** 주요 사업명 */
        @JsonProperty("MAIN_BIZ_NM")
        private String mainBizNm;

        /** 사업 요약 설명. company.business_overview에 저장 */
        @JsonProperty("BIZ_SUMMARY")
        private String bizSummary;

        /** 기준 연도 */
        @JsonProperty("BIZ_YEAR")
        private String bizYear;

        /**
         * 사업 개요 텍스트 추출.
         * BIZ_SUMMARY 우선, 없으면 MAIN_BIZ_NM 사용.
         */
        public String extractOverview() {
            if (bizSummary != null && !bizSummary.isBlank()) return bizSummary.trim();
            if (mainBizNm != null && !mainBizNm.isBlank()) return mainBizNm.trim();
            return null;
        }
    }
}
