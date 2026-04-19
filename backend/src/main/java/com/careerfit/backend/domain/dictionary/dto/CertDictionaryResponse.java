package com.careerfit.backend.domain.dictionary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "자격증/어학 사전 응답 DTO")
@Getter
@Builder
public class CertDictionaryResponse {

    @Schema(description = "사전 ID (user_certificate.cert_id에 사용)", example = "1")
    private Long id;

    @Schema(description = "자격증/어학명", example = "정보처리기사")
    private String name;

    @Schema(description = "발급기관", example = "한국산업인력공단")
    private String issuer;

    @Schema(description = "카테고리 (IT | 금융 | 어학)", example = "IT")
    private String category;
}
