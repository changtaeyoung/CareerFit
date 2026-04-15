package com.careerfit.backend.domain.user.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
@Schema(description = "사용자 자격증")
public class UserCertificate {

    @Schema(description = "자격증 ID")
    private Long id;

    @Schema(description = "스펙 버전 ID")
    private Long specVersionId;

    @Schema(description = "자격증 사전 ID")
    private Long certId;

    @Schema(description = "취득 상태", example = "취득")
    private String status;

    @Schema(description = "점수 (점수제 자격증만)", example = "850")
    private BigDecimal score;

    @Schema(description = "취득일", example = "2024-06-01")
    private LocalDate acquiredAt;
}