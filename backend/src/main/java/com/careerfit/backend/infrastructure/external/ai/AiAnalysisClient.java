package com.careerfit.backend.infrastructure.external.ai;

import com.careerfit.backend.infrastructure.external.ai.dto.QualitativeRequest;
import com.careerfit.backend.infrastructure.external.ai.dto.QualitativeResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * FastAPI AI 분석 서비스 HTTP 클라이언트.
 *
 * ── 역할 ──────────────────────────────────────────────────────────────────
 * Spring Boot ↔ Python FastAPI 통신 담당.
 * FastAPI가 실행 중이 아니거나 응답이 없을 때도 Spring Boot가 죽지 않도록
 * 방어적으로 예외를 처리하고 기본값(0점)을 반환한다.
 *
 * ── 왜 동기(RestClient)인가 ──────────────────────────────────────────────
 * AnalysisService.analyze()가 이미 @Async 없이 호출자 스레드에서 실행되고
 * 분석 결과가 즉시 필요하므로 동기 HTTP 호출이 적합하다.
 * FastAPI 자체가 async이므로 Spring에서 동기로 호출해도 FastAPI 쪽은 비동기 처리됨.
 */
@Slf4j
@Component
public class AiAnalysisClient {

    private final RestClient restClient = RestClient.create();

    @Value("${crawler.base-url}")
    private String crawlerBaseUrl;

    /**
     * FastAPI에 정성 분석을 요청한다.
     *
     * @param request 인재상 + JD + 자소서 목록
     * @return 분석 결과 (점수 0~50 + 피드백)
     *
     * ── 실패 시 동작 ────────────────────────────────────────────────────
     * FastAPI가 꺼져 있거나 오류 발생 시 0점(빈 결과)을 반환.
     * 정성 분석 실패가 전체 분석을 막으면 안 되기 때문.
     * 정량 분석 결과는 정상적으로 사용자에게 전달됨.
     */
    public QualitativeResponse analyzeQualitative(QualitativeRequest request) {
        try {
            log.info("[AiAnalysisClient] 정성 분석 요청 — FastAPI URL: {}", crawlerBaseUrl);
            QualitativeResponse response = restClient.post()
                    .uri(crawlerBaseUrl + "/api/analysis/qualitative")
                    .header("Content-Type", "application/json")
                    .body(request)
                    .retrieve()
                    .body(QualitativeResponse.class);

            if (response == null) {
                log.warn("[AiAnalysisClient] FastAPI 응답 null — 0점 처리");
                return QualitativeResponse.empty("AI 분석 응답을 받지 못했습니다.");
            }

            log.info("[AiAnalysisClient] 정성 분석 완료 — 점수: {}점", response.getTotalScore());
            return response;

        } catch (Exception e) {
            // FastAPI가 꺼져있거나 네트워크 오류 시 → 0점으로 분석 계속 진행
            log.warn("[AiAnalysisClient] FastAPI 호출 실패 (정성 분석 0점 처리): {}", e.getMessage());
            return QualitativeResponse.empty("AI 서비스 연결 실패 — 정성 분석 제외됨");
        }
    }
}
