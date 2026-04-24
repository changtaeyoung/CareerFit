package com.careerfit.backend.domain.coverletter.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "채용공고 자소서 문항")
@Getter
@Builder
public class JobPostingQuestionResponse {

    @Schema(description = "문항 ID", example = "1")
    private Long id;

    @Schema(description = "공고 ID", example = "1")
    private Long postingId;

    @Schema(description = "문항 내용", example = "지원동기를 작성해주세요. (500자)")
    private String question;

    @Schema(description = "문항 순서", example = "1")
    private Integer sortOrder;
}
