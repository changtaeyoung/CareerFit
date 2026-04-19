package com.careerfit.backend.domain.bookmark.controller;

import com.careerfit.backend.common.jwt.CustomUserDetails;
import com.careerfit.backend.common.response.ApiResponse;
import com.careerfit.backend.domain.bookmark.dto.BookmarkRequest;
import com.careerfit.backend.domain.bookmark.dto.BookmarkResponse;
import com.careerfit.backend.domain.bookmark.service.BookmarkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Bookmark", description = "북마크 API")
@RestController
@RequestMapping("/api/user/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    // 기업 또는 채용공고를 북마크에 추가
    @Operation(
            summary = "북마크 등록",
            description = "targetType: COMPANY | POSTING, targetId: 대상 ID. 이미 북마크된 대상이면 409 반환"
    )
    @PostMapping
    public ResponseEntity<ApiResponse<?>> addBookmark(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody BookmarkRequest request) {

        bookmarkService.addBookmark(userDetails.getUserId(), request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("북마크가 추가되었습니다", null));
    }

    // 로그인 유저의 북마크 목록 조회 (기업/공고 혼합, 최신순)
    @Operation(
            summary = "북마크 목록 조회",
            description = "로그인 유저의 전체 북마크를 최신순으로 반환. COMPANY/POSTING 혼합"
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<BookmarkResponse>>> getBookmarks(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<BookmarkResponse> data = bookmarkService.getBookmarks(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("북마크 목록 조회 성공", data));
    }

    // 북마크 삭제 — 본인 북마크만 삭제 가능
    @Operation(
            summary = "북마크 삭제",
            description = "북마크 ID로 삭제. 본인 북마크가 아니면 403 반환"
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteBookmark(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {

        bookmarkService.deleteBookmark(userDetails.getUserId(), id);
        return ResponseEntity.ok(ApiResponse.success("북마크가 삭제되었습니다", null));
    }
}
