package com.careerfit.backend.domain.coverletter.controller;

import com.careerfit.backend.common.jwt.CustomUserDetails;
import com.careerfit.backend.common.response.ApiResponse;
import com.careerfit.backend.domain.coverletter.dto.CoverLetterRequest;
import com.careerfit.backend.domain.coverletter.dto.CoverLetterResponse;
import com.careerfit.backend.domain.coverletter.service.CoverLetterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "CoverLetter", description = "자소서 작성 · 조회 · 삭제 API")
@RestController
@RequestMapping("/api/cover-letters")
@RequiredArgsConstructor
public class CoverLetterController {

    private final CoverLetterService coverLetterService;

    // 자기소개서를 저장, 동일 문항에 이미 작성된 내용이 있으면 자동으로 수정 - upsert 방식 이용
    @Operation(
            summary = "자기소개서 저장/수정",
            description = "동일 문항(questionId)에 이미 작성된 자소서가 있으면 본문을 수정, 없으면 새로 저장 (upsert)"
    )
    @PostMapping
    public ResponseEntity<ApiResponse<CoverLetterResponse>> save(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CoverLetterRequest request) {

        CoverLetterResponse response = coverLetterService.save(userDetails.getUserId(), request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("자기소개서가 저장되었습니다", response));
    }

    // 특정 공고에 대해 내가 작성한 자소서 전체 목록을 문항 내용과 함께 반환
    @Operation(
            summary = "공고별 내 자소서 목록 조회",
            description = "postingId 기준으로 내가 작성한 모든 문항의 자소서를 반환. 문항 내용(questionContent) 포함"
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<CoverLetterResponse>>> getByPosting(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam Long postingId) {

        List<CoverLetterResponse> response = coverLetterService.getByPosting(
                userDetails.getUserId(), postingId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 자소서 ID로 단건 조회, 본인이 작성한 자소서가 아니면 404를 반환
    @Operation(
            summary = "자소서 단건 조회",
            description = "본인 소유가 아니면 404"
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CoverLetterResponse>> getById(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {

        CoverLetterResponse response = coverLetterService.getById(userDetails.getUserId(), id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 자소서를 삭제, 본인 소유가 아니면 404를 반환
    @Operation(
            summary = "자소서 삭제",
            description = "본인 소유가 아니면 404"
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> delete(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {

        coverLetterService.delete(userDetails.getUserId(), id);
        return ResponseEntity.ok(ApiResponse.success("자소서가 삭제되었습니다", null));
    }
}
