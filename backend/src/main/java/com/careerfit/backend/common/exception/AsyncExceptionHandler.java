package com.careerfit.backend.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;

import java.lang.reflect.Method;

/**
 * @Async 메서드(void 반환)에서 발생한 예외를 전역으로 처리하는 핸들러.
 */
@Slf4j
public class AsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

    /**
     * @param throwable  발생한 예외
     * @param method     예외가 발생한 @Async 메서드 (클래스명.메서드명 식별용)
     * @param params     해당 메서드 호출 시 전달된 파라미터들 (디버깅용)
     */
    @Override
    public void handleUncaughtException(Throwable throwable, Method method, Object... params) {
        log.error(
            "[AsyncException] 비동기 작업 실패 — 메서드: {}.{}, 파라미터: {}, 예외: {}",
            method.getDeclaringClass().getSimpleName(),
            method.getName(),
            params,
            throwable.getMessage(),
            throwable  // 마지막 인자로 Throwable을 넘기면 스택 트레이스 전체가 로그에 출력됨
        );
    }
}
