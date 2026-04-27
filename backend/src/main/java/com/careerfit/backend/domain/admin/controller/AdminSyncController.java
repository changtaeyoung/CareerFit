package com.careerfit.backend.domain.admin.controller;

import com.careerfit.backend.common.response.ApiResponse;
import com.careerfit.backend.infrastructure.external.alio.AlioSyncService;
import com.careerfit.backend.infrastructure.external.dart.DartSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 관리자용 외부 데이터 동기화 트리거 컨트롤러.
 *
 * ── 접근 권한 ──────────────────────────────────────────────────────────────
 * SecurityConfig에서 /api/admin/** 경로는 ROLE_ADMIN만 접근 가능으로 설정됨.
 * 이 컨트롤러의 모든 엔드포인트는 별도 @PreAuthorize 없이 자동으로 보호됨.
 *
 * ── 왜 202 Accepted를 반환하나 ──────────────────────────────────────────
 * 동기화 작업은 @Async로 백그라운드에서 실행되므로 완료를 기다리지 않고
 * 즉시 202 Accepted를 반환한다. 작업 결과는 서버 로그에서 확인 가능.
 * HTTP 202는 "요청은 받았으나 아직 처리 완료가 아님"을 의미하는 정확한 상태코드.
 */
@Tag(name = "Admin - Sync", description = "관리자용 외부 데이터 동기화 API")
@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminSyncController {

    private final DartSyncService dartSyncService;
    private final AlioSyncService alioSyncService;

    @Operation(
        summary = "DART corp_code 일괄 동기화",
        description = "DART corpCode.xml을 다운로드하여 dart_code가 없는 기업에 자동 매핑. " +
                      "백그라운드로 실행되며 즉시 응답 반환. 결과는 서버 로그 확인."
    )
    @PostMapping("/dart/sync-corp-code")
    public ResponseEntity<ApiResponse<String>> syncDartCorpCode() {
        log.info("[AdminSyncController] DART corp_code 동기화 트리거");
        dartSyncService.syncCorpCodes();
        return ResponseEntity.accepted()
                .body(ApiResponse.success("DART corp_code 동기화가 백그라운드에서 시작되었습니다."));
    }

    /**
     * DART empSttus API로 기업 평균 연봉 일괄 수집.
     *
     * @param bsnsYear 수집할 사업연도.
     *                 기본값: 전년도 (현재 연도 - 1)
     *                 DART는 이듬해 3~4월에 전년도 사업보고서 공시.
     *                 예: 2026년 4월 기준 → bsnsYear=2025 (2025년 사업보고서)
     */
    @Operation(
        summary = "DART 평균 연봉 일괄 수집",
        description = "dart_code가 있는 기업의 empSttus API를 호출하여 평균 연봉을 company_salary에 저장. " +
                      "백그라운드 실행. bsnsYear 파라미터로 수집 연도 지정 (기본: 전년도)."
    )
    @PostMapping("/dart/sync-salary")
    public ResponseEntity<ApiResponse<String>> syncDartSalary(
            @RequestParam(required = false) String bsnsYear) {
        // 기본값: 전년도 (2026년 현재 → 2025)
        if (bsnsYear == null || bsnsYear.isBlank()) {
            bsnsYear = String.valueOf(java.time.Year.now().getValue() - 1);
        }
        log.info("[AdminSyncController] DART 평균 연봉 수집 트리거 — bsnsYear: {}", bsnsYear);
        dartSyncService.syncAverageSalary(bsnsYear);
        return ResponseEntity.accepted()
                .body(ApiResponse.success(bsnsYear + "년 DART 평균 연봉 수집이 백그라운드에서 시작되었습니다."));
    }

    /**
     * ALIO 기관 정보 일괄 동기화 트리거.
     * alio_code가 없는 기업을 대상으로 기관명 → ALIO 기관코드 매핑 + 임직원/설립연도/사업개요 수집.
     */
    @Operation(
        summary = "ALIO 기관 정보 일괄 동기화",
        description = "alio_code가 없는 기업에 대해 ALIO API를 호출하여 기관코드, 임직원 수, 설립연도, 사업개요를 수집. " +
                      "민간 기업은 ALIO 미등록이므로 자동 스킵됨. 백그라운드 실행."
    )
    @PostMapping("/alio/sync-info")
    public ResponseEntity<ApiResponse<String>> syncAlioInfo() {
        log.info("[AdminSyncController] ALIO 기관 정보 동기화 트리거");
        alioSyncService.syncAlioInfo();
        return ResponseEntity.accepted()
                .body(ApiResponse.success("ALIO 기관 정보 동기화가 백그라운드에서 시작되었습니다."));
    }
}
