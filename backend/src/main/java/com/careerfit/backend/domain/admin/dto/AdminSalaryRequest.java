package com.careerfit.backend.domain.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 관리자 연봉 수동 입력 요청 DTO.
 *
 * source가 자동으로 MANUAL로 설정됨.
 */
@Schema(description = "관리자 연봉 수동 입력 요청")
@Getter
@NoArgsConstructor
public class AdminSalaryRequest {

    @Schema(description = "연봉 유형 (ENTRY=신입초봉, AVERAGE=평균연봉)",
            example = "ENTRY", allowableValues = {"ENTRY", "AVERAGE"})
    @NotBlank
    private String salaryType;

    @Schema(description = "금액 (만원 단위)", example = "4500")
    @NotNull
    @Min(1000) @Max(9999999)
    private Integer amount;

    @Schema(description = "기준 연도", example = "2024")
    @NotNull
    @Min(2000) @Max(2100)
    private Integer year;

    @Schema(description = "출처 URL (선택)", example = "https://www.ibk.co.kr/recruit/salary")
    private String sourceUrl;
}
