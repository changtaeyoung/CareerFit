package com.careerfit.backend.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 페이지네이션이 있는 목록 응답 공통 DTO.
 *
 * ApiResponse<PageResponse<T>> 형태로 사용.
 * 프론트는 data.content로 목록을, data.totalPages로 전체 페이지 수를 받는다.
 */
@Schema(description = "페이지네이션 목록 응답")
@Getter
@Builder
public class PageResponse<T> {

    @Schema(description = "현재 페이지 데이터 목록")
    private List<T> content;

    @Schema(description = "전체 데이터 건수", example = "19")
    private int totalCount;

    @Schema(description = "전체 페이지 수", example = "2")
    private int totalPages;

    @Schema(description = "현재 페이지 번호 (1 기반)", example = "1")
    private int currentPage;

    @Schema(description = "페이지 크기", example = "12")
    private int pageSize;

    // 편의 팩토리 메서드 — totalPages를 직접 계산
    public static <T> PageResponse<T> of(List<T> content, int totalCount, int currentPage, int pageSize) {
        int totalPages = pageSize > 0 ? (int) Math.ceil((double) totalCount / pageSize) : 0;
        return PageResponse.<T>builder()
                .content(content)
                .totalCount(totalCount)
                .totalPages(Math.max(totalPages, 1))  // 데이터 0건이어도 1페이지
                .currentPage(currentPage)
                .pageSize(pageSize)
                .build();
    }
}
