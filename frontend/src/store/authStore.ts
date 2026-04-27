import { create } from 'zustand';
import { jwtDecode } from 'jwt-decode';

interface User {
    userId: number;
    email: string;
    name: string;
}

interface AuthState {
    user: User | null;
    isAuthenticated: boolean;
    setAuth: (user: User, accessToken: string, refreshToken: string) => void;
    logout: () => void;
}

// localStorage에서 user 정보 복원
const getStoredUser = (): User | null => {
    try {
        const stored = localStorage.getItem('user');
        return stored ? JSON.parse(stored) : null;
    } catch {
        return null;
    }
};

// localStorage에서 엑세스 토큰 확인 및 만료 여부 검사
const checkAuthStatus = (): boolean => {
    const token = localStorage.getItem('accessToken');
    if (!token) return false;
    
    try {
        const decoded = jwtDecode(token);
        // exp는 초 단위이므로 1000을 곱해서 밀리초로 변환
        if (decoded.exp && decoded.exp * 1000 < Date.now()) {
            // 토큰 만료됨
            localStorage.removeItem('accessToken');
            localStorage.removeItem('refreshToken');
            localStorage.removeItem('user');
            return false;
        }
        return true;
    } catch {
        return false;
    }
};

export const useAuthStore = create<AuthState>((set) => ({
    user: getStoredUser(),
    isAuthenticated: checkAuthStatus(),

    setAuth: (user, accessToken, refreshToken) => {
        localStorage.setItem('accessToken', accessToken);
        localStorage.setItem('refreshToken', refreshToken);
        localStorage.setItem('user', JSON.stringify(user));
        set({ user, isAuthenticated: true });
    },

    logout: () => {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('user');
        set({ user: null, isAuthenticated: false });
    },
}));