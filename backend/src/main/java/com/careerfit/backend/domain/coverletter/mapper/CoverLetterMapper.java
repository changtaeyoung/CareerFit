package com.careerfit.backend.domain.coverletter.mapper;

import com.careerfit.backend.domain.coverletter.dto.CoverLetterResponse;
import com.careerfit.backend.domain.coverletter.entity.CoverLetter;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CoverLetterMapper {

    // 자소서 저장 (user_id + question_id 조합은 UNIQUE — 1문항 1답변)
    void insert(CoverLetter coverLetter);

    // 자소서 본문 수정 (updated_at 갱신 포함)
    void updateContent(@Param("id") Long id,
                       @Param("userId") Long userId,
                       @Param("content") String content);

    // 공고별 내 자소서 전체 조회 (문항 내용 JOIN)
    List<CoverLetterResponse> selectByPostingId(@Param("userId") Long userId,
                                                @Param("postingId") Long postingId);

    // 자소서 단건 조회 (문항 내용 JOIN)
    CoverLetterResponse selectById(@Param("id") Long id,
                                   @Param("userId") Long userId);

    // user_id + question_id 조합으로 기존 자소서 존재 여부 확인
    Long selectIdByUserAndQuestion(@Param("userId") Long userId,
                                   @Param("questionId") Long questionId);

    // 자소서 삭제
    void delete(@Param("id") Long id,
                @Param("userId") Long userId);
}
