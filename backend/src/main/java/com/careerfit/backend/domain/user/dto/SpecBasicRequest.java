package com.careerfit.backend.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Schema(description = "스펙 기본 정보 등록 요청")
public class SpecBasicRequest {

    @NotBlank(message = "학력을 입력해주세요")
    @Schema(description = "학력", example = "졸업")
    private String education;

    @NotBlank(message = "학교명을 입력해주세요")
    @Schema(description = "학교명", example = "한국대학교")
    private String university;

    @DecimalMin(value = "0.0", message = "학점은 0.0 이상이어야 합니다")
    @DecimalMax(value = "4.5", message = "학점은 4.5 이하여야 합니다")
    @Schema(description = "학점 (4.5 만점)", example = "3.8")
    private BigDecimal gpa;

    @NotEmpty(message = "희망 직무를 1개 이상 선택해주세요")
    @Schema(description = "희망 직무 목록", example = "[\"BACKEND\", \"FULLSTACK\"]")
    private List<String> wantedJobs;

    @Schema(description = "기술스택 목록")
    private List<SkillItem> skills;

    @Getter
    @Setter
    @Schema(description = "기술스택 항목")
    public static class SkillItem {

        @Schema(description = "기술 사전 ID", example = "1")
        private Long skillId;

        @Schema(description = "숙련도", example = "상")
        private String proficiency;
    }
}