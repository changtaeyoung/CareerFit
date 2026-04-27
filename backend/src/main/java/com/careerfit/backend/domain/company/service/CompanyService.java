package com.careerfit.backend.domain.company.service;

import com.careerfit.backend.common.dto.PageResponse;
import com.careerfit.backend.common.exception.CustomException;
import com.careerfit.backend.common.exception.ErrorCode;
import com.careerfit.backend.domain.company.dto.CompanyDetailResponse;
import com.careerfit.backend.domain.company.dto.CompanyResponse;
import com.careerfit.backend.domain.company.dto.CompanySalaryResponse;
import com.careerfit.backend.domain.company.dto.JobPostingResponse;
import com.careerfit.backend.domain.company.entity.Company;
import com.careerfit.backend.domain.company.entity.CompanySalary;
import com.careerfit.backend.domain.company.mapper.CompanyMapper;
import com.careerfit.backend.domain.company.mapper.CompanySalaryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyMapper companyMapper;
    private final CompanySalaryMapper companySalaryMapper;

    // 업종·기업유형·키워드 필터와 페이징을 적용하여 기업 목록을 조회
    // totalCount를 함께 계산해서 PageResponse로 반환 → 프론트 페이지네이션에 사용
    @Transactional(readOnly = true)
    public PageResponse<CompanyResponse> getCompanyList(int page, int size, String industry, String companyType, String keyword) {
        log.info("[CompanyService] 기업 목록 조회 - page: {}, size: {}, industry: {}, companyType: {}, keyword: {}", page, size, industry, companyType, keyword);
        int offset = (page - 1) * size;
        List<CompanyResponse> content = companyMapper.selectCompanyList(offset, size, industry, companyType, keyword)
                .stream()
                .map(CompanyResponse::from)
                .toList();
        int totalCount = companyMapper.countCompanyList(industry, companyType, keyword);
        return PageResponse.of(content, totalCount, page, size);
    }

    // 기업 ID로 상세 정보를 조회, 존재하지 않으면 COMPANY_NOT_FOUND 예외
    // 연봉 정보(최신 연도의 ENTRY/AVERAGE)를 함께 조회하여 응답에 포함
    @Transactional(readOnly = true)
    public CompanyDetailResponse getCompanyDetail(Long id) {
        log.info("[CompanyService] 기업 상세 조회 - id: {}", id);
        Company company = companyMapper.selectCompanyById(id);
        if (company == null) {
            throw new CustomException(ErrorCode.COMPANY_NOT_FOUND);
        }

        // 연봉 정보 조회 및 DTO 조립 (없으면 null)
        CompanySalaryResponse salary = buildSalaryResponse(id);

        return CompanyDetailResponse.from(company, salary);
    }

    // 기업 존재 여부를 먼저 확인, 해당 기업의 전체 채용공고 목록을 반환 (과거 공고 포함)
    @Transactional(readOnly = true)
    public List<JobPostingResponse> getPostingsByCompany(Long companyId) {
        log.info("[CompanyService] 기업별 채용공고 조회 - companyId: {}", companyId);
        // 기업 존재 여부 선체크
        if (companyMapper.selectCompanyById(companyId) == null) {
            throw new CustomException(ErrorCode.COMPANY_NOT_FOUND);
        }
        return companyMapper.selectPostingsByCompanyId(companyId);
    }

    // 공고 ID로 상세 정보를 조회, 존재하지 않으면 POSTING_NOT_FOUND 예외
    @Transactional(readOnly = true)
    public JobPostingResponse getPostingDetail(Long id) {
        log.info("[CompanyService] 채용공고 상세 조회 - id: {}", id);
        JobPostingResponse posting = companyMapper.selectPostingById(id);
        if (posting == null) {
            throw new CustomException(ErrorCode.POSTING_NOT_FOUND);
        }
        return posting;
    }

    // jobType·status·keyword 필터와 페이징을 적용하여 공고 목록을 조회
    @Transactional(readOnly = true)
    public PageResponse<JobPostingResponse> getPostings(int page, int size, String jobType, String status, String keyword) {
        log.info("[CompanyService] 전체 채용공고 조회 - page: {}, size: {}, jobType: {}, status: {}, keyword: {}", page, size, jobType, status, keyword);
        int offset = (page - 1) * size;
        List<JobPostingResponse> content = companyMapper.selectPostings(offset, size, jobType, status, keyword);
        int totalCount = companyMapper.countPostings(jobType, status, keyword);
        return PageResponse.of(content, totalCount, page, size);
    }

    // 기업의 최신 연도 연봉을 조회하여 ENTRY/AVERAGE 분류 후 DTO로 조립
    // 연봉 데이터가 하나도 없으면 null 반환 (프론트에서 '-'로 표시)
    private CompanySalaryResponse buildSalaryResponse(Long companyId) {
        List<CompanySalary> rows = companySalaryMapper.selectLatestByCompanyId(companyId);
        if (rows.isEmpty()) {
            return null;
        }

        CompanySalary entry   = findByType(rows, "ENTRY");
        CompanySalary average = findByType(rows, "AVERAGE");

        if (entry == null && average == null) {
            return null;
        }

        return CompanySalaryResponse.builder()
                .entrySalary  (entry   != null ? entry.getAmount() : null)
                .entryYear    (entry   != null ? entry.getYear()   : null)
                .entrySource  (entry   != null ? entry.getSource() : null)
                .averageSalary(average != null ? average.getAmount() : null)
                .averageYear  (average != null ? average.getYear()   : null)
                .averageSource(average != null ? average.getSource() : null)
                .build();
    }

    private CompanySalary findByType(List<CompanySalary> rows, String type) {
        return rows.stream()
                .filter(r -> type.equals(r.getSalaryType()))
                .findFirst()
                .orElse(null);
    }
}
