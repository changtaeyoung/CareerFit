package com.careerfit.backend.domain.company.mapper;

import com.careerfit.backend.domain.company.dto.JobPostingResponse;
import com.careerfit.backend.domain.company.entity.Company;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CompanyMapper {

    // 기업 목록 조회 (페이징)
    List<Company> selectCompanyList(@Param("offset") int offset,
                                    @Param("limit") int limit,
                                    @Param("industry") String industry,
                                    @Param("companyType") String companyType);

    // 기업 상세 조회
    Company selectCompanyById(@Param("id") Long id);

    // 기업별 채용공고 목록
    List<JobPostingResponse> selectPostingsByCompanyId(@Param("companyId") Long companyId);

    // 채용공고 상세 조회
    JobPostingResponse selectPostingById(@Param("id") Long id);

    // 전체 채용공고 목록 (페이징 + 필터)
    List<JobPostingResponse> selectPostings(@Param("offset") int offset,
                                            @Param("limit") int limit,
                                            @Param("jobType") String jobType,
                                            @Param("status") String status);
}
