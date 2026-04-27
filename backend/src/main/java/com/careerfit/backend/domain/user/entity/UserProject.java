package com.careerfit.backend.domain.user.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDate;

@Getter
@Builder
@Schema(description = "포트폴리오 프로젝트")
public class UserProject {

    @Schema(description = "프로젝트 ID")
    private Long id;

    @Schema(description = "스펙 버전 ID")
    private Long specVersionId;

    @Schema(description = "프로젝트명", example = "CareerFit")
    private String title;

    @Schema(description = "프로젝트 설명")
    private String description;

    @Schema(description = "GitHub URL", example = "https://github.com/user/project")
    private String githubUrl;

    @Schema(description = "시작일", example = "2024-01-01")
    private LocalDate startedAt;

    @Schema(description = "종료일 (진행중이면 null)", example = "2024-06-30")
    private LocalDate endedAt;

    @Schema(description = "진행 상태", example = "완료")
    private String status;
}