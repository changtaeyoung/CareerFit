package com.careerfit.backend.domain.company.controller;

import com.careerfit.backend.common.response.ApiResponse;
import com.careerfit.backend.domain.company.dto.CompanyDetailResponse;
import com.careerfit.backend.domain.company.dto.CompanyResponse;
import com.careerfit.backend.domain.company.dto.JobPostingResponse;
import com.careerfit.backend.domain.company.service.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Company", description = "기업 · 채용공고 조회 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    // 업종·기업유형 필터와 페이징을 적용한 기업 목록을 반환
    @Operation(
            summary = "기업 목록 조회",
            description = "업종(industry), 기업유형(companyType) 필터 + 페이징. 필터 미전달 시 전체 조회"
    )
    @GetMapping("/companies")
    public ResponseEntity<ApiResponse<List<CompanyResponse>>> getCompanyList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String industry,
            @RequestParam(required = false) String companyType) {

        List<CompanyResponse> data = companyService.getCompanyList(page, size, industry, companyType);
        return ResponseEntity.ok(ApiResponse.success("기업 목록 조회 성공", data));
    }

    // 기업 ID로 vision, 인재상, 사업개요를 포함한 기업 상세 정보를 반환
    @Operation(
            summary = "기업 상세 조회",
            description = "vision, 인재상(talentImage), 사업개요(businessOverview) 포함. 없으면 404"
    )
    @GetMapping("/companies/{id}")
    public ResponseEntity<ApiResponse<CompanyDetailResponse>> getCompanyDetail(
            @PathVariable Long id) {

        CompanyDetailResponse data = companyService.getCompanyDetail(id);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    // 특정 기업에 속한 전체 채용공고 목록을 반환 (과거 공고 포함)
    @Operation(
            summary = "기업별 채용공고 목록",
            description = "해당 기업의 전체 공고 반환 (ACTIVE/CLOSED/SCHEDULED 모두 포함). 기업 없으면 404"
    )
    @GetMapping("/companies/{id}/postings")
    public ResponseEntity<ApiResponse<List<JobPostingResponse>>> getPostingsByCompany(
            @PathVariable Long id) {

        List<JobPostingResponse> data = companyService.getPostingsByCompany(id);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    // 전체 채용공고 목록을 반환. status 미전달 시 과거 공고 포함 전체 반환
    @Operation(
            summary = "전체 채용공고 목록",
            description = "status 미전달 시 전체 조회 (과거 공고 포함) | ACTIVE=진행중 | CLOSED=마감 | SCHEDULED=예정"
    )
    @GetMapping("/postings")
    public ResponseEntity<ApiResponse<List<JobPostingResponse>>> getPostings(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String jobType,
            @RequestParam(required = false) String status) {

        List<JobPostingResponse> data = companyService.getPostings(page, size, jobType, status);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    // 채용공고 ID로 원문 URL을 포함한 공고 상세 정보를 반환한다
    @Operation(
            summary = "채용공고 상세 조회",
            description = "원문 URL(url) 포함. 과거 공고도 조회 가능. 없으면 404"
    )
    @GetMapping("/postings/{id}")
    public ResponseEntity<ApiResponse<JobPostingResponse>> getPostingDetail(
            @PathVariable Long id) {

        JobPostingResponse data = companyService.getPostingDetail(id);
        return ResponseEntity.ok(ApiResponse.success(data));
    }
}
