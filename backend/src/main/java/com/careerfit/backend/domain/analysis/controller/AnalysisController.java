package com.careerfit.backend.domain.analysis.controller;

import com.careerfit.backend.common.jwt.CustomUserDetails;
import com.careerfit.backend.common.response.ApiResponse;
import com.careerfit.backend.domain.analysis.dto.AnalysisHistoryResponse;
import com.careerfit.backend.domain.analysis.dto.AnalysisReportResponse;
import com.careerfit.backend.domain.analysis.dto.AnalysisRequest;
import com.careerfit.backend.domain.analysis.dto.PrerequisiteCheckResponse;
import com.careerfit.backend.domain.analysis.service.AnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Analysis", description = "커리어 핏 분석 API")
@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisService analysisService;

    // 채용공고 ID와 스펙 버전으로 핏 분석을 실행, 리포트를 생성
    @Operation(
            summary = "핏 분석 실행",
            description = "specVersionId 미전달 시 현재 활성 스펙 버전 자동 사용. 필수조건 미충족 시 total_score=0으로 즉시 반환"
    )
    @PostMapping
    public ResponseEntity<ApiResponse<AnalysisReportResponse>> analyze(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AnalysisRequest request) {

        AnalysisReportResponse response = analysisService.analyze(
                userDetails.getUserId(),
                request.getJobPostingId(),
                request.getSpecVersionId()
        );
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("분석이 완료되었습니다", response));
    }

    // 로그인 사용자의 전체 분석 이력을 최신순으로 반환
    @Operation(
            summary = "분석 히스토리 조회",
            description = "내 전체 분석 이력 최신순 반환"
    )
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<AnalysisHistoryResponse>>> getHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<AnalysisHistoryResponse> history = analysisService.getHistory(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    // reportId로 점수 상세, 갭 분석, 액션 플랜을 포함한 리포트 반환
    @Operation(
            summary = "분석 리포트 상세 조회",
            description = "점수 상세, 갭 분석(CERT/LANGUAGE/SKILL), 액션 플랜(P1~P3) 포함. 없으면 404"
    )
    @GetMapping("/{reportId}")
    public ResponseEntity<ApiResponse<AnalysisReportResponse>> getReportDetail(
            @PathVariable Long reportId) {

        AnalysisReportResponse response = analysisService.getReportDetail(reportId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 분석 리포트 삭제, cascade로 gap, recommendation도 함께 제거
    @Operation(
            summary = "분석 리포트 삭제",
            description = "cascade로 gap, recommendation도 함께 삭제. 없으면 404"
    )
    @DeleteMapping("/{reportId}")
    public ResponseEntity<ApiResponse<?>> deleteReport(
            @PathVariable Long reportId) {

        analysisService.deleteReport(reportId);
        return ResponseEntity.ok(ApiResponse.success("분석 리포트가 삭제되었습니다", null));
    }

    // 채용공고 상세 진입 전 필수 조건 충족 여부를 미리 확인해 경고 메시지를 반환
    @Operation(
            summary = "공고 지원 자격 사전 체크",
            description = "eligible=false이면 failedRequirements에 미충족 필수 조건 목록 반환. 프론트에서 빨간 경고 배너로 표시"
    )
    @GetMapping("/postings/{postingId}/prerequisite-check")
    public ResponseEntity<ApiResponse<PrerequisiteCheckResponse>> checkPrerequisite(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postingId) {

        PrerequisiteCheckResponse response = analysisService.checkPrerequisite(
                userDetails.getUserId(), postingId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
