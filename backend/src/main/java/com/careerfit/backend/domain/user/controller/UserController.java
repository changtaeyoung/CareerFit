package com.careerfit.backend.domain.user.controller;

import com.careerfit.backend.common.jwt.CustomUserDetails;
import com.careerfit.backend.common.response.ApiResponse;
import com.careerfit.backend.domain.user.dto.*;
import com.careerfit.backend.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User", description = "사용자 스펙 관리 API")
@RestController // Json Data만 주고 받는 API 서버 / HTML 렌더링 해야하면 Controller
@RequestMapping("/api/user")
@RequiredArgsConstructor // Spring Bean들을 주입받아 사용하는 클래스에서 사용
public class UserController {

    private final UserService userService;

    @Operation(summary = "스펙 기본 정보 등록", description = "학력, 희망직무, 기술스택 등록 (1단계)")
    @PostMapping("/spec")
    public ResponseEntity<ApiResponse<SpecBasicResponse>> registerBasicSpec(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody SpecBasicRequest request) {

        SpecBasicResponse response = userService.registerBasicSpec(userDetails.getUserId(), request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("스펙 기본 정보가 등록되었습니다", response));
    }

    @Operation(summary = "자격증/어학 등록", description = "자격증, 어학 점수 등록 (2단계)")
    @PostMapping("/spec/{versionId}/qualifications")
    public ResponseEntity<ApiResponse<?>> registerQualification(
            @PathVariable Long versionId,
            @RequestBody SpecQualificationRequest request) {

        userService.registerQualification(versionId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("자격증/어학 정보가 등록되었습니다", null));
    }

    @Operation(summary = "경력/프로젝트/수상 등록", description = "경력, 프로젝트, 수상 내역 등록 (3단계)")
    @PostMapping("/spec/{versionId}/experience")
    public ResponseEntity<ApiResponse<?>> registerExperience(
            @PathVariable Long versionId,
            @RequestBody SpecExperienceRequest request) {

        userService.registerExperience(versionId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("경력/프로젝트/수상 정보가 등록되었습니다", null));
    }

    @Operation(summary = "스펙 버전 삭제")
    @DeleteMapping("/spec/{versionId}")
    public ResponseEntity<ApiResponse<?>> deleteSpecVersion(
            @PathVariable Long versionId) {

        userService.deleteSpecVersion(versionId);
        return ResponseEntity.ok(ApiResponse.success("스펙이 삭제되었습니다", null));
    }

    // 비밀번호 변경
    @Operation(summary = "비밀번호 변경")
    @PatchMapping("/password")
    public ResponseEntity<ApiResponse<?>> changePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PasswordChangeRequest request) {

        userService.changePassword(userDetails.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.success("비밀번호가 변경되었습니다", null));
    }

    // 이름 수정
    @Operation(summary = "이름 수정")
    @PatchMapping("/name")
    public ResponseEntity<ApiResponse<?>> updateName(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam String name) {

        userService.updateName(userDetails.getUserId(), name);
        return ResponseEntity.ok(ApiResponse.success("이름이 수정되었습니다", null));
    }

    // 회원 탈퇴
    @Operation(summary = "회원 탈퇴")
    @DeleteMapping
    public ResponseEntity<ApiResponse<?>> deleteUser(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        userService.deleteUser(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("회원 탈퇴가 완료되었습니다", null));
    }

    @Operation(summary = "스펙 히스토리 조회", description = "전체 버전 목록")
    @GetMapping("/spec/history")
    public ResponseEntity<ApiResponse<?>> getSpecHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        return ResponseEntity.ok(
                ApiResponse.success(userService.getSpecHistory(userDetails.getUserId()))
        );
    }

    @Operation(summary = "스펙 상세 조회", description = "특정 버전 상세 내역")
    @GetMapping("/spec/{versionId}")
    public ResponseEntity<ApiResponse<?>> getSpecDetail(
            @PathVariable Long versionId) {

        return ResponseEntity.ok(
                ApiResponse.success(userService.getSpecDetail(versionId))
        );
    }

    // 스펙 완성도 정확 조회 — 필수 3항목 + 선택 5항목 기반 0~100 점수
    @Operation(
            summary = "스펙 완성도 조회",
            description = "필수(기본정보·희망직무·기술스택) 각 20점 + 선택(자격증·어학·경력·프로젝트·수상) 각 8점 = 최대 100점"
    )
    @GetMapping("/spec/{versionId}/completion")
    public ResponseEntity<ApiResponse<SpecCompletionResponse>> getSpecCompletion(
            @PathVariable Long versionId) {

        SpecCompletionResponse data = userService.getSpecCompletion(versionId);
        return ResponseEntity.ok(ApiResponse.success("스펙 완성도 조회 성공", data));
    }
}