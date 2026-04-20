package com.careerfit.backend.domain.company.controller;

import com.careerfit.backend.common.dto.PageResponse;
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

    @Operation(summary = "기업 목록 조회",
               description = "industry/companyType/keyword 필터 + 페이징. PageResponse로 totalPages 포함 반환")
    @GetMapping("/companies")
    public ResponseEntity<ApiResponse<PageResponse<CompanyResponse>>> getCompanyList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String industry,
            @RequestParam(required = false) String companyType,
            @RequestParam(required = false) String keyword) {

        PageResponse<CompanyResponse> data = companyService.getCompanyList(page, size, industry, companyType, keyword);
        return ResponseEntity.ok(ApiResponse.success("기업 목록 조회 성공", data));
    }

    @Operation(summary = "기업 상세 조회",
               description = "vision, 인재상, 사업개요, 연봉 정보 포함. 없으면 404")
    @GetMapping("/companies/{id}")
    public ResponseEntity<ApiResponse<CompanyDetailResponse>> getCompanyDetail(
            @PathVariable Long id) {

        CompanyDetailResponse data = companyService.getCompanyDetail(id);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @Operation(summary = "기업별 채용공고 목록",
               description = "해당 기업의 전체 공고 반환 (ACTIVE/CLOSED/SCHEDULED 모두 포함)")
    @GetMapping("/companies/{id}/postings")
    public ResponseEntity<ApiResponse<List<JobPostingResponse>>> getPostingsByCompany(
            @PathVariable Long id) {

        List<JobPostingResponse> data = companyService.getPostingsByCompany(id);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @Operation(summary = "전체 채용공고 목록",
               description = "keyword: 공고 제목/기업명 검색. PageResponse로 totalPages 포함 반환")
    @GetMapping("/postings")
    public ResponseEntity<ApiResponse<PageResponse<JobPostingResponse>>> getPostings(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String jobType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {

        PageResponse<JobPostingResponse> data = companyService.getPostings(page, size, jobType, status, keyword);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @Operation(summary = "채용공고 상세 조회",
               description = "원문 URL 포함. 없으면 404")
    @GetMapping("/postings/{id}")
    public ResponseEntity<ApiResponse<JobPostingResponse>> getPostingDetail(
            @PathVariable Long id) {

        JobPostingResponse data = companyService.getPostingDetail(id);
        return ResponseEntity.ok(ApiResponse.success(data));
    }
}

