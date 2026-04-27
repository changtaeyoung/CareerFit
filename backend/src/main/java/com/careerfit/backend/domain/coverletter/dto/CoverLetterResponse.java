package com.careerfit.backend.domain.coverletter.dto;

import com.careerfit.backend.domain.coverletter.entity.CoverLetter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Schema(description = "자소서 응답 DTO")
@Getter
@Builder
public class CoverLetterResponse {

    @Schema(description = "자소서 ID", example = "1")
    private Long id;

    @Schema(description = "채용공고 ID", example = "1")
    private Long postingId;

    @Schema(description = "자소서 문항 ID", example = "2")
    private Long questionId;

    @Schema(description = "문항 내용 (JOIN으로 가져옴)", example = "지원 동기를 작성해주세요 (700자)")
    private String questionContent;

    @Schema(description = "자소서 본문", example = "저는 금융 IT 백엔드 개발자로서...")
    private String content;

    @Schema(description = "최초 작성 일시", example = "2025-03-15T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "최종 수정 일시", example = "2025-03-16T14:20:00")
    private LocalDateTime updatedAt;

    // CoverLetter 엔티티를 응답 DTO로 변환 (questionContent는 JOIN 결과로 별도 세팅)
    public static CoverLetterResponse from(CoverLetter coverLetter) {
        return CoverLetterResponse.builder()
                .id(coverLetter.getId())
                .postingId(coverLetter.getPostingId())
                .questionId(coverLetter.getQuestionId())
                .content(coverLetter.getContent())
                .createdAt(coverLetter.getCreatedAt())
                .updatedAt(coverLetter.getUpdatedAt())
                .build();
    }
}
