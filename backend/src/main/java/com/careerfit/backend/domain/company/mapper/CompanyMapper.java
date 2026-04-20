package com.careerfit.backend.domain.company.mapper;

import com.careerfit.backend.domain.company.dto.JobPostingResponse;
import com.careerfit.backend.domain.company.entity.Company;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CompanyMapper {

    // 기업 목록 조회 (페이징 + 키워드 검색)
    List<Company> selectCompanyList(@Param("offset") int offset,
                                    @Param("limit") int limit,
                                    @Param("industry") String industry,
                                    @Param("companyType") String companyType,
                                    @Param("keyword") String keyword);

    // 기업 목록 전체 건수 (페이지네이션 totalPages 계산용)
    int countCompanyList(@Param("industry") String industry,
                         @Param("companyType") String companyType,
                         @Param("keyword") String keyword);

    // 기업 상세 조회
    Company selectCompanyById(@Param("id") Long id);

    // 기업별 채용공고 목록
    List<JobPostingResponse> selectPostingsByCompanyId(@Param("companyId") Long companyId);

    // 채용공고 상세 조회
    JobPostingResponse selectPostingById(@Param("id") Long id);

    // 전체 채용공고 목록 (페이징 + 직무/상태/키워드 필터)
    List<JobPostingResponse> selectPostings(@Param("offset") int offset,
                                            @Param("limit") int limit,
                                            @Param("jobType") String jobType,
                                            @Param("status") String status,
                                            @Param("keyword") String keyword);

    // 채용공고 전체 건수 (페이지네이션 totalPages 계산용)
    int countPostings(@Param("jobType") String jobType,
                      @Param("status") String status,
                      @Param("keyword") String keyword);
}
