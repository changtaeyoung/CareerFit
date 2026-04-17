package com.careerfit.backend.domain.analysis.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Schema(description = "핏 분석 요청 DTO")
@Getter
public class AnalysisRequest {

    @Schema(description = "분석할 채용공고 ID", example = "1")
    @NotNull(message = "채용공고 ID는 필수입니다")
    private Long jobPostingId;

    @Schema(description = "분석에 사용할 스펙 버전 ID (미전달 시 현재 활성 버전 사용)", example = "3")
    private Long specVersionId;  // null이면 현재 활성 버전 자동 선택
}
