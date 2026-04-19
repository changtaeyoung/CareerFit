import { create } from 'zustand';

type Theme = 'light' | 'dark';

interface ThemeState {
    theme: Theme;
    toggleTheme: () => void;
    setTheme: (theme: Theme) => void;
}

// 초기 테마 결정: localStorage → 시스템 설정 → light 기본
const getInitialTheme = (): Theme => {
    const stored = localStorage.getItem('theme') as Theme | null;
    if (stored) return stored;
    if (window.matchMedia('(prefers-color-scheme: dark)').matches) return 'dark';
    return 'light';
};

// HTML에 dark 클래스 동기화
const applyTheme = (theme: Theme) => {
    const root = document.documentElement;
    if (theme === 'dark') {
        root.classList.add('dark');
    } else {
        root.classList.remove('dark');
    }
    localStorage.setItem('theme', theme);
};

// 초기 적용
applyTheme(getInitialTheme());

export const useThemeStore = create<ThemeState>((set) => ({
    theme: getInitialTheme(),

    toggleTheme: () =>
        set((state) => {
            const next = state.theme === 'light' ? 'dark' : 'light';
            applyTheme(next);
            return { theme: next };
        }),

    setTheme: (theme) => {
        applyTheme(theme);
        set({ theme });
    },
}));
