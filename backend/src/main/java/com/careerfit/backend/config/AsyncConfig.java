package com.careerfit.backend.config;

import com.careerfit.backend.common.exception.AsyncExceptionHandler;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 비동기(Async) 인프라 설정 클래스.
 */
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    /**
     * CareerFit 전용 ThreadPoolTaskExecutor 빈.
     */
    @Bean(name = "taskExecutor")
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(4);          // 항상 유지할 스레드 수
        executor.setMaxPoolSize(10);          // 최대 스레드 수 (급증 시 확장)
        executor.setQueueCapacity(50);        // 스레드 포화 시 대기열 크기
        executor.setKeepAliveSeconds(60);     // 유휴 스레드 회수 시간 (초)
        executor.setThreadNamePrefix("CareerFit-Async-"); // 로그 식별용 접두어

        // 서버 종료 시 진행 중인 작업 완료 대기 (데이터 정합성 보장)
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);

        // initialize()를 반드시 호출해야 내부 ThreadPoolExecutor가 초기화된다.
        // 이 호출 없이 빈을 주입하면 NullPointerException이 발생한다.
        executor.initialize();

        return executor;
    }

    /**
     * 비동기 메서드에서 발생한 미처리 예외를 전역으로 처리하는 핸들러.
     *
     * ── 왜 별도 핸들러가 필요한가 ──────────────────────────────────────────
     * @Async 메서드는 호출자(Controller/Service) 스레드와 별도 스레드에서 실행된다.
     * 따라서 @ControllerAdvice(GlobalExceptionHandler)가 비동기 스레드의 예외를
     * 잡을 수 없다. 비동기에서 예외가 터져도 호출자는 모르고 서비스가 조용히 실패한다.
     *
     * AsyncUncaughtExceptionHandler를 등록하면 이런 '조용한 실패'를 로그로 남겨서
     * 운영 중에 문제를 빠르게 발견할 수 있다.
     *
     * ── 반환값이 void인 @Async 메서드만 해당 ────────────────────────────────
     * Future<T> 또는 CompletableFuture<T>를 반환하는 @Async 메서드는
     * 호출자가 .get()을 통해 예외를 받을 수 있어서 이 핸들러가 적용되지 않는다.
     * CareerFit에서 비동기 작업(PDF 파싱, API 크롤링)은 void 반환이 대부분이므로
     * 이 핸들러의 역할이 중요하다.
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new AsyncExceptionHandler();
    }
}
