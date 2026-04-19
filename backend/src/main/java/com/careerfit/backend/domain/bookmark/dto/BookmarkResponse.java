package com.careerfit.backend.domain.bookmark.dto;

import com.careerfit.backend.domain.bookmark.entity.Bookmark;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "북마크 응답 DTO")
@Getter
@Builder
public class BookmarkResponse {

    @Schema(description = "북마크 ID", example = "1")
    private Long id;

    @Schema(description = "북마크 대상 타입 (COMPANY | POSTING)", example = "POSTING")
    private String targetType;

    @Schema(description = "기업 ID (COMPANY 타입일 때)", example = "2")
    private Long companyId;

    @Schema(description = "기업명", example = "KB국민은행")
    private String companyName;

    @Schema(description = "채용공고 ID (POSTING 타입일 때)", example = "5")
    private Long postingId;

    @Schema(description = "채용공고 제목 (POSTING 타입일 때)", example = "2025 KB국민은행 IT 신입 공채")
    private String postingTitle;

    @Schema(description = "공고 마감일 (POSTING 타입일 때)", example = "2025-05-31")
    private LocalDate deadline;

    @Schema(description = "북마크 등록 시각", example = "2025-04-18T10:30:00")
    private LocalDateTime createdAt;
}
