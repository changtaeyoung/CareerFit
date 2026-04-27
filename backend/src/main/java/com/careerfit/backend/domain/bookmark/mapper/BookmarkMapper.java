package com.careerfit.backend.domain.bookmark.mapper;

import com.careerfit.backend.domain.bookmark.dto.BookmarkResponse;
import com.careerfit.backend.domain.bookmark.entity.Bookmark;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BookmarkMapper {

    // 북마크 등록
    void insertBookmark(Bookmark bookmark);

    // 북마크 단건 조회 (id 기준 — 삭제 전 소유자 검증용)
    Bookmark selectBookmarkById(@Param("id") Long id);

    // 중복 체크: company_id 기준
    Bookmark selectByUserAndCompany(@Param("userId") Long userId,
                                    @Param("companyId") Long companyId);

    // 중복 체크: posting_id 기준
    Bookmark selectByUserAndPosting(@Param("userId") Long userId,
                                    @Param("postingId") Long postingId);

    // 북마크 목록 조회 (JOIN으로 기업명/공고명 포함, created_at DESC)
    List<BookmarkResponse> selectBookmarksByUserId(@Param("userId") Long userId);

    // 북마크 삭제
    void deleteBookmark(@Param("id") Long id);
}
