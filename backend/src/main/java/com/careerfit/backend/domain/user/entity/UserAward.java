package com.careerfit.backend.domain.user.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDate;

@Getter
@Builder
@Schema(description = "수상 내역")
public class UserAward {

    @Schema(description = "수상 ID")
    private Long id;

    @Schema(description = "스펙 버전 ID")
    private Long specVersionId;

    @Schema(description = "수상명", example = "해커톤 최우수상")
    private String title;

    @Schema(description = "주관기관", example = "카카오")
    private String institution;

    @Schema(description = "수상 등급", example = "최우수")
    private String grade;

    @Schema(description = "수상일", example = "2024-09-15")
    private LocalDate awardedAt;
}