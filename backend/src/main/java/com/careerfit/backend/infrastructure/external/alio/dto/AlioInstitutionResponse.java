package com.careerfit.backend.infrastructure.external.alio.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * ALIO 기관 정보 API 응답 DTO.
 *
 * ── API 엔드포인트 ────────────────────────────────────────────────────────
 * GET https://www.alio.go.kr/openApi/getPublicInstInfo.do
 *   ?apiKey={기관정보_API_KEY}
 *   &type=json
 *   &instNm={기관명}
 *
 * ── 응답 구조 ─────────────────────────────────────────────────────────────
 * {
 *   "result": {
 *     "list": [{
 *       "INST_NM": "한국전력공사",
 *       "INST_CODE": "1741",            ← company.alio_code에 저장
 *       "EST_DATE": "19610701",         ← 설립일 (yyyyMMdd)
 *       "ADRES": "전라남도 나주시...",
 *       "EMP_CNT": "23456",             ← 임직원 수
 *       "INST_TYPE_NM": "시장형공기업", ← 기관 유형
 *       "RPRSNT_TELNO": "061-345-3114"
 *     }]
 *   }
 * }
 *
 * ── instNm 검색 특성 ─────────────────────────────────────────────────────
 * ALIO는 기관명 부분 검색을 지원함. "한국전력"으로 검색 시 "한국전력공사" 반환.
 * 다만 여러 건이 반환될 수 있으므로 서비스에서 정확히 일치하는 건을 선택해야 함.
 */
@Getter
@NoArgsConstructor
public class AlioInstitutionResponse {

    @JsonProperty("result")
    private Result result;

    public List<InstItem> getList() {
        if (result == null || result.getList() == null) return List.of();
        return result.getList();
    }

    @Getter
    @NoArgsConstructor
    public static class Result {
        @JsonProperty("list")
        private List<InstItem> list;
    }

    @Getter
    @NoArgsConstructor
    public static class InstItem {

        /** 기관명 */
        @JsonProperty("INST_NM")
        private String instNm;

        /** ALIO 기관 코드. company.alio_code에 저장됨 */
        @JsonProperty("INST_CODE")
        private String instCode;

        /** 설립일 (yyyyMMdd 형식). 예: "19610701" */
        @JsonProperty("EST_DATE")
        private String estDate;

        /** 주소 */
        @JsonProperty("ADRES")
        private String adres;

        /** 임직원 수 (문자열). parseInt 필요 */
        @JsonProperty("EMP_CNT")
        private String empCnt;

        /** 기관 유형. 예: "시장형공기업", "준시장형공기업", "준정부기관" */
        @JsonProperty("INST_TYPE_NM")
        private String instTypeNm;

        /** 대표 전화번호 */
        @JsonProperty("RPRSNT_TELNO")
        private String rprsntTelno;

        /** 설립연도 파싱 (estDate의 앞 4자리) */
        public Integer parseFoundedYear() {
            if (estDate == null || estDate.length() < 4) return null;
            try {
                return Integer.parseInt(estDate.substring(0, 4));
            } catch (NumberFormatException e) {
                return null;
            }
        }

        /** 임직원 수 파싱 */
        public Integer parseEmpCnt() {
            if (empCnt == null || empCnt.isBlank()) return null;
            try {
                return Integer.parseInt(empCnt.replaceAll("[,\\s]", ""));
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }
}
