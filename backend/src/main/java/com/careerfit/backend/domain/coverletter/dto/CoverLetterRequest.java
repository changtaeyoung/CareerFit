package com.careerfit.backend.domain.coverletter.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Schema(description = "자소서 저장/수정 요청 DTO")
@Getter
public class CoverLetterRequest {

    @Schema(description = "채용공고 ID", example = "1")
    @NotNull(message = "채용공고 ID는 필수입니다")
    private Long postingId;

    @Schema(description = "자소서 문항 ID", example = "2")
    @NotNull(message = "문항 ID는 필수입니다")
    private Long questionId;

    @Schema(description = "자소서 본문", example = "저는 금융 IT 백엔드 개발자로서...")
    @NotBlank(message = "자소서 본문은 필수입니다")
    private String content;
}
