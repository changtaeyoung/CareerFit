package com.careerfit.backend.domain.coverletter.mapper;

import com.careerfit.backend.domain.coverletter.dto.JobPostingQuestionResponse;
import com.careerfit.backend.domain.coverletter.entity.JobPostingQuestion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface JobPostingQuestionMapper {

    // 공고별 문항 전체 조회 (sort_order ASC)
    List<JobPostingQuestionResponse> selectByPostingId(@Param("postingId") Long postingId);

    // 문항 단건 조회
    JobPostingQuestionResponse selectById(@Param("id") Long id);

    // 문항 등록 (Admin)
    void insert(JobPostingQuestion question);

    // 문항 삭제 (Admin)
    void delete(@Param("id") Long id);
}
