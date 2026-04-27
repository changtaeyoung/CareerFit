package com.careerfit.backend.domain.company.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * 기업 상세 화면에서 보여줄 연봉 정보 DTO.
 *
 * 기업마다 여러 연도·타입의 연봉이 쌓이지만, 프론트에는 "가장 최근 연도의
 * ENTRY 1건과 AVERAGE 1건"만 전달한다. 둘 중 하나라도 없으면 해당 필드는 null.
 *
 * 단위: 만원
 */
@Schema(description = "기업 연봉 요약 응답 DTO (최신 연도 기준)")
@Getter
@Builder
public class CompanySalaryResponse {

    @Schema(description = "신입 초봉 (단위: 만원). 데이터 없으면 null", example = "5000")
    private Integer entrySalary;

    @Schema(description = "신입 초봉 기준 연도", example = "2024")
    private Integer entryYear;

    @Schema(description = "신입 초봉 출처", example = "MANUAL")
    private String entrySource;

    @Schema(description = "평균 연봉 (단위: 만원). 데이터 없으면 null", example = "8500")
    private Integer averageSalary;

    @Schema(description = "평균 연봉 기준 연도", example = "2024")
    private Integer averageYear;

    @Schema(description = "평균 연봉 출처", example = "DART_API")
    private String averageSource;
}
