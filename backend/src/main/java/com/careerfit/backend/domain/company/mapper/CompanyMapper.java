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

    // DART 동기화용 — dart_code가 아직 없는 기업 전체 조회
    // 이미 dart_code가 채워진 기업은 제외하여 불필요한 업데이트 방지
    List<Company> selectCompaniesWithoutDartCode();

    // DART 동기화용 — 매칭된 corp_code를 company.dart_code에 저장
    void updateDartCode(@Param("id") Long id,
                        @Param("dartCode") String dartCode);

    // DART 연봉 수집용 — dart_code가 있는 기업만 대상
    // (dart_code 없으면 DART API 호출 불가)
    List<Company> selectCompaniesWithDartCode();

    // ALIO 동기화용 — alio_code가 없는 기업 전체 조회
    List<Company> selectCompaniesWithoutAlioCode();

    // ALIO 동기화용 — alio_code, 임직원 수, 설립연도, 사업개요 일괄 업데이트
    void updateAlioInfo(@Param("id") Long id,
                        @Param("alioCode") String alioCode,
                        @Param("employeeCount") Integer employeeCount,
                        @Param("foundedYear") Integer foundedYear,
                        @Param("businessOverview") String businessOverview);

    // Admin 수동 입력 — 인재상/비전/사업개요/임직원/설립연도/홈페이지/소재지 PATCH
    // null 파라미터는 기존 값 유지 (COALESCE 패턴)
    void updateCompanyInfo(@Param("id") Long id,
                           @Param("vision") String vision,
                           @Param("talentImage") String talentImage,
                           @Param("businessOverview") String businessOverview,
                           @Param("employeeCount") Integer employeeCount,
                           @Param("foundedYear") Integer foundedYear,
                           @Param("website") String website,
                           @Param("location") String location);
}
