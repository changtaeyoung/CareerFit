package com.careerfit.backend.domain.company.mapper;

import com.careerfit.backend.domain.company.entity.CompanySalary;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CompanySalaryMapper {

    /**
     * 기업의 최신 연도 연봉 정보 조회.
     *
     * 기업별로 salary_type마다 가장 최근 year의 row 1건을 반환.
     * 최대 2건(ENTRY, AVERAGE) 반환, 데이터 없는 타입은 결과에 포함되지 않음.
     *
     * 서비스 레이어에서 이 리스트를 받아 salaryType 기준으로 분류해 DTO 조립.
     */
    List<CompanySalary> selectLatestByCompanyId(@Param("companyId") Long companyId);
}
