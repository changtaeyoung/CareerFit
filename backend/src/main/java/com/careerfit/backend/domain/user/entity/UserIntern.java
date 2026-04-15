package com.careerfit.backend.domain.user.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDate;

@Getter
@Builder
@Schema(description = "인턴 · 경력 이력")
public class UserIntern {

    @Schema(description = "인턴 ID")
    private Long id;

    @Schema(description = "스펙 버전 ID")
    private Long specVersionId;

    @Schema(description = "회사명", example = "카카오")
    private String companyName;

    @Schema(description = "담당 직무", example = "백엔드 개발")
    private String role;

    @Schema(description = "업무 내용")
    private String description;

    @Schema(description = "시작일", example = "2024-07-01")
    private LocalDate startedAt;

    @Schema(description = "종료일 (재직중이면 null)", example = "2024-12-31")
    private LocalDate endedAt;
}