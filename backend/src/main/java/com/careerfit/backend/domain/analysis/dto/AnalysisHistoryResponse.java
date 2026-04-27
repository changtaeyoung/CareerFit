package com.careerfit.backend.domain.analysis.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Schema(description = "분석 히스토리 목록 응답 DTO — 카드형 목록 표시용")
@Getter
@Builder
public class AnalysisHistoryResponse {

    @Schema(description = "분석 리포트 ID", example = "1")
    private Long reportId;

    @Schema(description = "기업명", example = "IBK기업은행")
    private String companyName;

    @Schema(description = "채용공고 제목", example = "2025 상반기 IBK기업은행 IT 신입 공채")
    private String postingTitle;

    @Schema(description = "필수조건 전부 충족 여부", example = "true")
    private boolean requiredAllMet;

    @Schema(description = "최종 핏 점수 (0~100)", example = "75")
    private int totalScore;

    @Schema(description = "분석 상태 (COMPLETED/FAILED/PENDING)", example = "COMPLETED")
    private String status;

    @Schema(description = "분석 실행 일시", example = "2025-03-15T10:30:00")
    private LocalDateTime createdAt;
}
