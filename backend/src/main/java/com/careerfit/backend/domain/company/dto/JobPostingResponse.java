package com.careerfit.backend.domain.company.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Schema(description = "채용공고 응답 DTO (목록 · 상세 공통)")
@Getter
@Builder
public class JobPostingResponse {

    @Schema(description = "채용공고 ID", example = "1")
    private Long id;

    @Schema(description = "기업 ID", example = "1")
    private Long companyId;

    @Schema(description = "기업명", example = "IBK기업은행")
    private String companyName;

    @Schema(description = "공고 제목", example = "2025 상반기 IBK기업은행 IT 신입 공채")
    private String title;

    @Schema(description = "원문 공고 URL — 상세 조회 시 포함, 목록 조회 시 null 가능",
            example = "https://www.ibk.co.kr/recruit/2025")
    private String url;

    @Schema(description = "공고 상태 (ACTIVE/CLOSED/SCHEDULED)", example = "ACTIVE")
    private String status;

    @Schema(description = "직무 유형 (IT/DIGITAL/BACKEND/FULLSTACK/FRONTEND/DATA/INFRA/AI/SECURITY/ETC)",
            example = "IT")
    private String jobType;

    @Schema(description = "채용 시즌", example = "2025_상반기")
    private String season;

    @Schema(description = "공고 시작일", example = "2025-03-01")
    private LocalDate startedAt;

    @Schema(description = "공고 마감일", example = "2025-03-31")
    private LocalDate deadline;
}
