import { create } from 'zustand';

export type ToastType = 'success' | 'error' | 'info' | 'warning';

export interface Toast {
    id: string;
    type: ToastType;
    message: string;
}

interface ToastState {
    toasts: Toast[];
    addToast: (type: ToastType, message: string) => void;
    removeToast: (id: string) => void;
}

export const useToastStore = create<ToastState>((set) => ({
    toasts: [],

    addToast: (type, message) => {
        const id = Date.now().toString() + Math.random().toString(36).slice(2);
        set((state) => ({
            toasts: [...state.toasts, { id, type, message }],
        }));
        // 3초 후 자동 삭제
        setTimeout(() => {
            set((state) => ({
                toasts: state.toasts.filter((t) => t.id !== id),
            }));
        }, 3000);
    },

    removeToast: (id) =>
        set((state) => ({
            toasts: state.toasts.filter((t) => t.id !== id),
        })),
}));

// 편의 함수
export const toast = {
    success: (msg: string) => useToastStore.getState().addToast('success', msg),
    error: (msg: string) => useToastStore.getState().addToast('error', msg),
    info: (msg: string) => useToastStore.getState().addToast('info', msg),
    warning: (msg: string) => useToastStore.getState().addToast('warning', msg),
};
