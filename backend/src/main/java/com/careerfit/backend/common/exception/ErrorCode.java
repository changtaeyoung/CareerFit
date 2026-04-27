package com.careerfit.backend.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    // ── 인증 · 회원가입 ───────────────────────────────
    DUPLICATE_EMAIL         (HttpStatus.CONFLICT,            "이미 사용 중인 이메일입니다"),
    EMAIL_VERIFY_FAILED     (HttpStatus.BAD_REQUEST,         "이메일 인증번호가 일치하지 않습니다"),
    PASSWORD_MISMATCH       (HttpStatus.BAD_REQUEST,         "비밀번호가 일치하지 않습니다"),
    USER_NOT_FOUND          (HttpStatus.NOT_FOUND,           "사용자를 찾을 수 없습니다"),
    INVALID_CREDENTIALS     (HttpStatus.UNAUTHORIZED,        "이메일 또는 비밀번호가 올바르지 않습니다"),

    // ── JWT 토큰 ──────────────────────────────────────
    TOKEN_EXPIRED           (HttpStatus.UNAUTHORIZED,        "액세스 토큰이 만료되었습니다. 재발급이 필요합니다"),
    REFRESH_TOKEN_EXPIRED   (HttpStatus.UNAUTHORIZED,        "세션이 만료되었습니다. 다시 로그인해 주세요"),
    INVALID_TOKEN           (HttpStatus.UNAUTHORIZED,        "유효하지 않은 토큰입니다"),

    // ── 인가 ──────────────────────────────────────────
    UNAUTHORIZED            (HttpStatus.UNAUTHORIZED,        "로그인이 필요합니다"),
    FORBIDDEN               (HttpStatus.FORBIDDEN,           "접근 권한이 없습니다"),

    // ── 입력값 검증 ───────────────────────────────────
    INVALID_INPUT           (HttpStatus.BAD_REQUEST,         "입력값이 올바르지 않습니다"),
    DATA_INTEGRITY_VIOLATION(HttpStatus.CONFLICT,            "데이터 정합성 제약을 위반했습니다"),

    // ── 스펙 · 사용자 ─────────────────────────────────
    SPEC_NOT_FOUND          (HttpStatus.NOT_FOUND,           "스펙 정보가 등록되지 않았습니다"),
    SPEC_VERSION_NOT_FOUND  (HttpStatus.NOT_FOUND,           "해당 스펙 버전을 찾을 수 없습니다"),
    DUPLICATE_CERTIFICATE   (HttpStatus.CONFLICT,            "이미 등록된 자격증입니다"),
    DUPLICATE_LANGUAGE      (HttpStatus.CONFLICT,            "이미 등록된 어학 성적입니다"),

    // ── 기업 · 채용공고 ───────────────────────────────
    COMPANY_NOT_FOUND       (HttpStatus.NOT_FOUND,           "기업 정보를 찾을 수 없습니다"),
    POSTING_NOT_FOUND       (HttpStatus.NOT_FOUND,           "채용공고를 찾을 수 없습니다"),
    COVER_LETTER_NOT_FOUND  (HttpStatus.NOT_FOUND,           "자기소개서를 찾을 수 없습니다"),

    // ── 북마크 ────────────────────────────────────────
    BOOKMARK_NOT_FOUND      (HttpStatus.NOT_FOUND,           "북마크를 찾을 수 없습니다"),
    DUPLICATE_BOOKMARK      (HttpStatus.CONFLICT,            "이미 북마크한 대상입니다"),

    // ── 분석 ──────────────────────────────────────────
    ANALYSIS_NOT_FOUND      (HttpStatus.NOT_FOUND,           "분석 리포트를 찾을 수 없습니다"),
    ANALYSIS_FAILED         (HttpStatus.INTERNAL_SERVER_ERROR, "분석 처리 중 오류가 발생했습니다"),

    // ── 외부 API ──────────────────────────────────────
    OPENAI_API_ERROR        (HttpStatus.SERVICE_UNAVAILABLE, "AI 분석 서버에 오류가 발생했습니다"),
    EXTERNAL_API_ERROR      (HttpStatus.SERVICE_UNAVAILABLE, "외부 API 호출 중 오류가 발생했습니다"),
    CRAWLER_ERROR           (HttpStatus.SERVICE_UNAVAILABLE, "크롤러 서버에 오류가 발생했습니다"),

    // ── 서버 ──────────────────────────────────────────
    INTERNAL_SERVER_ERROR   (HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다");

    // ─────────────────────────────────────────────────
    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() { return status; }
    public String getMessage()    { return message; }
}