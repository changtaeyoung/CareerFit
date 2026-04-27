package com.careerfit.backend.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Schema(description = "자격증 + 어학 등록 요청")
public class SpecQualificationRequest {

    @Schema(description = "자격증 목록")
    private List<CertItem> certificates;

    @Schema(description = "어학 점수 목록")
    private List<LanguageItem> languages;

    @Getter
    @Setter
    @Schema(description = "자격증 항목")
    public static class CertItem {

        @Schema(description = "자격증 사전 ID", example = "1")
        private Long certId;

        @Schema(description = "취득 상태", example = "취득")
        private String status;

        @Schema(description = "점수 (점수제만)", example = "850")
        private BigDecimal score;

        @Schema(description = "취득일", example = "2024-06-01")
        private LocalDate acquiredAt;
    }

    @Getter
    @Setter
    @Schema(description = "어학 점수 항목")
    public static class LanguageItem {

        @Schema(description = "어학 종류", example = "TOEIC")
        private String langType;

        @Schema(description = "점수 (점수제)", example = "850")
        private Integer score;

        @Schema(description = "등급 (등급제)", example = "IM2")
        private String grade;

        @Schema(description = "취득일", example = "2024-11-01")
        private LocalDate acquiredAt;
    }
}