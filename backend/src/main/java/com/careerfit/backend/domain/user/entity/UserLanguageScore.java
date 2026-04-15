package com.careerfit.backend.domain.user.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDate;

@Getter
@Builder
@Schema(description = "사용자 어학 점수")
public class UserLanguageScore {

    @Schema(description = "어학 점수 ID")
    private Long id;

    @Schema(description = "스펙 버전 ID")
    private Long specVersionId;

    @Schema(description = "어학 종류", example = "TOEIC")
    private String langType;

    @Schema(description = "점수 (점수제)", example = "850")
    private Integer score;

    @Schema(description = "등급 (등급제)", example = "IM2")
    private String grade;

    @Schema(description = "취득일", example = "2024-11-01")
    private LocalDate acquiredAt;
}