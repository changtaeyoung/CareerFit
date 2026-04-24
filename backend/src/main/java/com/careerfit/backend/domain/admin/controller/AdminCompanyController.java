package com.careerfit.backend.domain.admin.controller;

import com.careerfit.backend.common.response.ApiResponse;
import com.careerfit.backend.domain.admin.dto.AdminCompanyUpdateRequest;
import com.careerfit.backend.domain.admin.dto.AdminQuestionRequest;
import com.careerfit.backend.domain.admin.dto.AdminSalaryRequest;
import com.careerfit.backend.domain.admin.service.AdminCompanyService;
import com.careerfit.backend.domain.coverletter.dto.JobPostingQuestionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 관리자 기업 정보 수동 입력 컨트롤러.
 *
 * ── AdminSyncController와 분리한 이유 ─────────────────────────────────────
 * AdminSyncController: 외부 API 자동 동기화 트리거 (비동기, 202 반환)
 * AdminCompanyController: 수동 데이터 입력 (동기, 200 반환)
 * 역할이 다르므로 파일을 분리하여 단일책임 원칙(SRP) 준수.
 */
@Tag(name = "Admin - Company", description = "관리자용 기업 정보 수동 입력 API")
@Slf4j
@RestController
@RequestMapping("/api/admin/companies")
@RequiredArgsConstructor
public class AdminCompanyController {

    private final AdminCompanyService adminCompanyService;

    // ── 기업 기본 정보 ─────────────────────────────────────────────────────────

    @Operation(
        summary = "기업 정보 수동 업데이트 (PATCH)",
        description = "인재상, 비전, 사업개요 등 자동화 불가 정보 직접 입력. null 필드는 기존 값 유지."
    )
    @PatchMapping("/{companyId}")
    public ResponseEntity<ApiResponse<String>> updateCompanyInfo(
            @PathVariable Long companyId,
            @RequestBody AdminCompanyUpdateRequest request) {
        log.info("[AdminCompanyController] 기업 정보 업데이트 — companyId: {}", companyId);
        adminCompanyService.updateCompanyInfo(companyId, request);
        return ResponseEntity.ok(ApiResponse.success("기업 정보가 업데이트되었습니다."));
    }

    // ── 연봉 ──────────────────────────────────────────────────────────────────

    @Operation(
        summary = "연봉 수동 입력",
        description = "DART/ALIO로 수집 불가한 연봉 데이터 직접 입력. source=MANUAL로 저장."
    )
    @PostMapping("/{companyId}/salary")
    public ResponseEntity<ApiResponse<String>> addSalary(
            @PathVariable Long companyId,
            @RequestBody @Valid AdminSalaryRequest request) {
        log.info("[AdminCompanyController] 연봉 수동 입력 — companyId: {}, type: {}, year: {}, amount: {}만원",
                companyId, request.getSalaryType(), request.getYear(), request.getAmount());
        adminCompanyService.addSalary(companyId, request);
        return ResponseEntity.ok(ApiResponse.success(
                request.getYear() + "년 " + request.getSalaryType()
                + " 연봉이 입력되었습니다. (" + request.getAmount() + "만원)"));
    }

    // ── 자소서 문항 ───────────────────────────────────────────────────────────

    @Operation(summary = "공고별 자소서 문항 조회")
    @GetMapping("/{companyId}/postings/{postingId}/questions")
    public ResponseEntity<ApiResponse<List<JobPostingQuestionResponse>>> getQuestions(
            @PathVariable Long companyId,
            @PathVariable Long postingId) {
        return ResponseEntity.ok(ApiResponse.success(adminCompanyService.getQuestions(postingId)));
    }

    @Operation(
        summary = "공고 자소서 문항 등록",
        description = "채용공고의 자소서 문항을 관리자가 직접 등록. " +
                      "사용자는 등록된 문항을 선택해서 자소서 작성."
    )
    @PostMapping("/{companyId}/postings/{postingId}/questions")
    public ResponseEntity<ApiResponse<String>> addQuestion(
            @PathVariable Long companyId,
            @PathVariable Long postingId,
            @RequestBody @Valid AdminQuestionRequest request) {
        log.info("[AdminCompanyController] 문항 등록 — postingId: {}, sortOrder: {}", postingId, request.getSortOrder());
        adminCompanyService.addQuestion(postingId, request);
        return ResponseEntity.ok(ApiResponse.success("문항이 등록되었습니다."));
    }

    @Operation(summary = "공고 자소서 문항 삭제")
    @DeleteMapping("/{companyId}/postings/{postingId}/questions/{questionId}")
    public ResponseEntity<ApiResponse<String>> deleteQuestion(
            @PathVariable Long companyId,
            @PathVariable Long postingId,
            @PathVariable Long questionId) {
        log.info("[AdminCompanyController] 문항 삭제 — questionId: {}", questionId);
        adminCompanyService.deleteQuestion(questionId);
        return ResponseEntity.ok(ApiResponse.success("문항이 삭제되었습니다."));
    }
}
