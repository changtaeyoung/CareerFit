package com.careerfit.backend.domain.bookmark.service;

import com.careerfit.backend.common.exception.CustomException;
import com.careerfit.backend.common.exception.ErrorCode;
import com.careerfit.backend.domain.bookmark.dto.BookmarkRequest;
import com.careerfit.backend.domain.bookmark.dto.BookmarkResponse;
import com.careerfit.backend.domain.bookmark.entity.Bookmark;
import com.careerfit.backend.domain.bookmark.mapper.BookmarkMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final BookmarkMapper bookmarkMapper;

    // 북마크 등록 — 이미 북마크된 대상이면 DUPLICATE_BOOKMARK 예외
    @Transactional
    public void addBookmark(Long userId, BookmarkRequest request) {
        log.info("[BookmarkService] 북마크 등록 - userId: {}, targetType: {}, targetId: {}",
                userId, request.getTargetType(), request.getTargetId());

        // 중복 체크
        if ("COMPANY".equals(request.getTargetType())) {
            if (bookmarkMapper.selectByUserAndCompany(userId, request.getTargetId()) != null) {
                throw new CustomException(ErrorCode.DUPLICATE_BOOKMARK);
            }
        } else {
            if (bookmarkMapper.selectByUserAndPosting(userId, request.getTargetId()) != null) {
                throw new CustomException(ErrorCode.DUPLICATE_BOOKMARK);
            }
        }

        Bookmark bookmark = Bookmark.builder()
                .userId(userId)
                .targetType(request.getTargetType())
                .companyId("COMPANY".equals(request.getTargetType()) ? request.getTargetId() : null)
                .postingId("POSTING".equals(request.getTargetType()) ? request.getTargetId() : null)
                .build();

        bookmarkMapper.insertBookmark(bookmark);
        log.info("[BookmarkService] 북마크 등록 완료 - bookmarkId: {}", bookmark.getId());
    }

    // 북마크 목록 조회 — 로그인 유저의 전체 북마크를 최신순으로 반환
    @Transactional(readOnly = true)
    public List<BookmarkResponse> getBookmarks(Long userId) {
        log.info("[BookmarkService] 북마크 목록 조회 - userId: {}", userId);
        return bookmarkMapper.selectBookmarksByUserId(userId);
    }

    // 북마크 삭제 — 다른 유저 북마크 삭제 시도 시 FORBIDDEN 예외
    @Transactional
    public void deleteBookmark(Long userId, Long bookmarkId) {
        log.info("[BookmarkService] 북마크 삭제 - userId: {}, bookmarkId: {}", userId, bookmarkId);

        Bookmark bookmark = bookmarkMapper.selectBookmarkById(bookmarkId);
        if (bookmark == null) {
            throw new CustomException(ErrorCode.BOOKMARK_NOT_FOUND);
        }
        if (!bookmark.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        bookmarkMapper.deleteBookmark(bookmarkId);
        log.info("[BookmarkService] 북마크 삭제 완료 - bookmarkId: {}", bookmarkId);
    }
}
