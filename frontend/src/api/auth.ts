import api from './axios';

// Spring Boot AuthController와 매칭되는 타입
export interface LoginRequest {
    email: string;
    password: string;
}

export interface SignupRequest {
    email: string;
    password: string;
    passwordConfirm: string;
    name: string;
}

export interface AuthResponse {
    accessToken: string;
    refreshToken: string;
    userId: number;
    email: string;
    name: string;
}

// 로그인 API 호출
export const login = async (data: LoginRequest): Promise<AuthResponse> => {
    const response = await api.post('/auth/login', data);
    return response.data.data; // ApiResponse.success()로 감싸져 있음
};

// 회원가입 API 호출 (백엔드 엔드포인트: /auth/register)
export const signup = async (data: SignupRequest): Promise<AuthResponse> => {
    const response = await api.post('/auth/register', data);
    return response.data.data;
};