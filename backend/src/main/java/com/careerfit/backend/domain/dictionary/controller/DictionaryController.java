package com.careerfit.backend.domain.dictionary.controller;

import com.careerfit.backend.common.response.ApiResponse;
import com.careerfit.backend.domain.dictionary.dto.CertDictionaryResponse;
import com.careerfit.backend.domain.dictionary.dto.SkillDictionaryResponse;
import com.careerfit.backend.domain.dictionary.service.DictionaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Dictionary", description = "자격증·어학·기술스택 사전 조회 API (프론트 드롭다운용)")
@RestController
@RequestMapping("/api/dictionary")
@RequiredArgsConstructor
public class DictionaryController {

    private final DictionaryService dictionaryService;

    // 자격증 사전 목록 (카테고리 선택 — IT | 금융). 어학은 별도 API로 제공
    @Operation(
            summary = "자격증 사전 조회",
            description = "프론트 자격증 드롭다운용. category 미전달 시 어학을 제외한 전체(IT+금융) 반환"
    )
    @GetMapping("/certs")
    public ResponseEntity<ApiResponse<List<CertDictionaryResponse>>> getCerts(
            @RequestParam(required = false) String category) {

        List<CertDictionaryResponse> data = dictionaryService.getCerts(category);
        return ResponseEntity.ok(ApiResponse.success("자격증 사전 조회 성공", data));
    }

    // 어학 사전 목록 (TOEIC, TOEFL, OPIC 등)
    @Operation(
            summary = "어학 사전 조회",
            description = "프론트 어학 드롭다운용. cert_dictionary에서 category='어학' 필터링하여 반환"
    )
    @GetMapping("/languages")
    public ResponseEntity<ApiResponse<List<CertDictionaryResponse>>> getLanguages() {

        List<CertDictionaryResponse> data = dictionaryService.getLanguages();
        return ResponseEntity.ok(ApiResponse.success("어학 사전 조회 성공", data));
    }

    // 기술스택 사전 목록 (Spring Boot, React 등)
    @Operation(
            summary = "기술스택 사전 조회",
            description = "프론트 기술스택 드롭다운용. category 필터 (BACKEND/FRONTEND/DATABASE/INFRA 등)"
    )
    @GetMapping("/skills")
    public ResponseEntity<ApiResponse<List<SkillDictionaryResponse>>> getSkills(
            @RequestParam(required = false) String category) {

        List<SkillDictionaryResponse> data = dictionaryService.getSkills(category);
        return ResponseEntity.ok(ApiResponse.success("기술스택 사전 조회 성공", data));
    }
}
