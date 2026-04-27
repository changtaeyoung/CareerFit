package com.careerfit.backend.domain.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 관리자 기업 정보 수동 입력 요청 DTO.
 *
 * 자동화가 어려운 인재상, 비전, 사업개요를 직접 입력.
 * null인 필드는 기존 값을 유지 (PATCH 방식).
 */
@Schema(description = "관리자 기업 정보 수동 업데이트 요청")
@Getter
@NoArgsConstructor
public class AdminCompanyUpdateRequest {

    @Schema(description = "기업 비전", example = "디지털 금융의 혁신을 선도하는 글로벌 금융그룹")
    private String vision;

    @Schema(description = "인재상", example = "도전정신, 창의성, 협력을 갖춘 디지털 인재")
    private String talentImage;

    @Schema(description = "사업 개요", example = "중소기업 전문 은행으로서 기업금융 및 개인금융 서비스 제공")
    private String businessOverview;

    @Schema(description = "임직원 수", example = "13000")
    private Integer employeeCount;

    @Schema(description = "설립연도", example = "1961")
    private Integer foundedYear;

    @Schema(description = "홈페이지 URL", example = "https://www.ibk.co.kr")
    private String website;

    @Schema(description = "소재지", example = "서울특별시 중구 을지로 79")
    private String location;
}
