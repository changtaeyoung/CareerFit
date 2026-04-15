package com.careerfit.backend.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Schema(description = "경력 + 프로젝트 + 수상 등록 요청")
public class SpecExperienceRequest {

    @Schema(description = "인턴 · 경력 목록")
    private List<InternItem> interns;

    @Schema(description = "프로젝트 목록")
    private List<ProjectItem> projects;

    @Schema(description = "수상 내역 목록")
    private List<AwardItem> awards;

    @Getter
    @Setter
    @Schema(description = "인턴 · 경력 항목")
    public static class InternItem {

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

    @Getter
    @Setter
    @Schema(description = "프로젝트 항목")
    public static class ProjectItem {

        @Schema(description = "프로젝트명", example = "CareerFit")
        private String title;

        @Schema(description = "프로젝트 설명")
        private String description;

        @Schema(description = "GitHub URL", example = "https://github.com/user/project")
        private String githubUrl;

        @Schema(description = "시작일", example = "2024-01-01")
        private LocalDate startedAt;

        @Schema(description = "종료일 (진행중이면 null)")
        private LocalDate endedAt;

        @Schema(description = "진행 상태", example = "완료")
        private String status;
    }

    @Getter
    @Setter
    @Schema(description = "수상 항목")
    public static class AwardItem {

        @Schema(description = "수상명", example = "해커톤 최우수상")
        private String title;

        @Schema(description = "주관기관", example = "카카오")
        private String institution;

        @Schema(description = "수상 등급", example = "최우수")
        private String grade;

        @Schema(description = "수상일", example = "2024-09-15")
        private LocalDate awardedAt;
    }
}