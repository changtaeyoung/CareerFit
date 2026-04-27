package com.careerfit.backend.infrastructure.external.dart.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DART 직원 현황(empSttus) API 응답 DTO.
 *
 * ── API 엔드포인트 ────────────────────────────────────────────────────────
 * GET https://opendart.fss.or.kr/api/empSttus.json
 *   ?crtfc_key={API_KEY}
 *   &corp_code={8자리 기업코드}
 *   &bsns_year={사업연도, 예: 2024}
 *   &reprt_code=11011   ← 사업보고서 코드 (고정값)
 *
 * ── 응답 예시 ────────────────────────────────────────────────────────────
 * {
 *   "status": "000",       ← "000" = 정상, 그 외는 에러
 *   "message": "정상",
 *   "list": [
 *     {
 *       "corp_code": "00126380",
 *       "corp_name": "국민은행",
 *       "bsns_year": "2024",
 *       "reprt_code": "11011",
 *       "fo_bbm": "정규직",         ← 고용 형태 (정규직/기간제)
 *       "sexdstn": "합계",           ← 성별 (남/여/합계)
 *       "reform_bfe_emp_co_rgllbr": "...",  ← 정규직 수
 *       "avg_term": "10.1",          ← 평균 근속연수
 *       "avg_salary": "8500"         ← 1인 평균 급여 (만원 단위)
 *     },
 *     ...  ← 남/여/합계 각각 row로 나뉨
 *   ]
 * }
 *
 * ── 왜 "합계" + "정규직" 필터가 필요한가 ────────────────────────────────
 * DART는 성별(남/여/합계) × 고용형태(정규직/기간제)로 여러 row를 반환한다.
 * 우리는 "전체 평균"이 필요하므로 fo_bbm="합계", sexdstn="합계" 조합을 사용.
 * 단, 기업마다 row 구성이 다를 수 있어 서비스에서 방어적으로 처리.
 */
@Getter
@NoArgsConstructor
public class EmpSttusResponse {

    /** API 처리 상태 코드. "000" = 정상 */
    @JsonProperty("status")
    private String status;

    /** API 메시지 */
    @JsonProperty("message")
    private String message;

    /** 직원 현황 목록 (성별 × 고용형태 조합으로 여러 건) */
    @JsonProperty("list")
    private List<EmpSttusItem> list;

    /** 정상 응답 여부 */
    public boolean isSuccess() {
        return "000".equals(status);
    }

    @Getter
    @NoArgsConstructor
    public static class EmpSttusItem {

        @JsonProperty("corp_code")
        private String corpCode;

        @JsonProperty("bsns_year")
        private String bsnsYear;

        /** 고용 형태: "정규직", "기간제근로자", "합계" */
        @JsonProperty("fo_bbm")
        private String foBbm;

        /** 성별 구분: "남", "여", "합계" */
        @JsonProperty("sexdstn")
        private String sexdstn;

        /** 평균 근속연수 */
        @JsonProperty("avg_term")
        private String avgTerm;

        /**
         * 1인 평균 급여액 (만원 단위 문자열).
         * 숫자지만 DART가 String으로 내려줌. "8,500" 처럼 콤마 포함 가능.
         * parseAmount() 메서드로 Integer로 변환하여 사용.
         */
        @JsonProperty("avg_salary")
        private String avgSalary;

        /**
         * avgSalary 문자열을 Integer로 안전하게 변환.
         * 콤마(",") 제거 후 파싱. 빈 문자열이나 "-" 이면 null 반환.
         *
         * 예: "8,500" → 8500 / "-" → null / "" → null
         */
        public Integer parseAmount() {
            if (avgSalary == null || avgSalary.isBlank() || "-".equals(avgSalary.trim())) {
                return null;
            }
            try {
                return Integer.parseInt(avgSalary.replaceAll("[,\\s]", ""));
            } catch (NumberFormatException e) {
                return null;
            }
        }

        /** "합계" 행인지 확인 (남/여 합계, 고용형태 합계) */
        public boolean isTotalRow() {
            return "합계".equals(foBbm) && "합계".equals(sexdstn);
        }
    }
}
