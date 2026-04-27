import { motion, AnimatePresence } from 'framer-motion';
import { AlertTriangle, X } from 'lucide-react';

interface ConfirmModalProps {
    isOpen: boolean;
    title: string;
    message: string;
    confirmLabel?: string;
    cancelLabel?: string;
    variant?: 'danger' | 'default';
    onConfirm: () => void;
    onCancel: () => void;
    isLoading?: boolean;
}

export default function ConfirmModal({
    isOpen,
    title,
    message,
    confirmLabel = '확인',
    cancelLabel = '취소',
    variant = 'default',
    onConfirm,
    onCancel,
    isLoading = false,
}: ConfirmModalProps) {
    return (
        <AnimatePresence>
            {isOpen && (
                <>
                    {/* 오버레이 */}
                    <motion.div
                        initial={{ opacity: 0 }}
                        animate={{ opacity: 1 }}
                        exit={{ opacity: 0 }}
                        className="fixed inset-0 bg-black/50 backdrop-blur-sm z-50"
                        onClick={onCancel}
                    />

                    {/* 모달 */}
                    <motion.div
                        initial={{ opacity: 0, scale: 0.9, y: 20 }}
                        animate={{ opacity: 1, scale: 1, y: 0 }}
                        exit={{ opacity: 0, scale: 0.9, y: 20 }}
                        transition={{ type: 'spring', duration: 0.3 }}
                        className="fixed inset-0 flex items-center justify-center z-50 p-4"
                    >
                        <div className="bg-white dark:bg-surface-800 rounded-2xl shadow-2xl
                                        border border-surface-200 dark:border-surface-700
                                        max-w-md w-full p-6"
                             onClick={(e) => e.stopPropagation()}
                        >
                            <div className="flex items-start gap-4">
                                {variant === 'danger' && (
                                    <div className="w-10 h-10 rounded-full bg-red-100 dark:bg-red-500/15
                                                    flex items-center justify-center flex-shrink-0">
                                        <AlertTriangle className="w-5 h-5 text-red-600 dark:text-red-400" />
                                    </div>
                                )}
                                <div className="flex-1 min-w-0">
                                    <div className="flex items-center justify-between">
                                        <h3 className="text-lg font-semibold text-surface-900 dark:text-surface-100">
                                            {title}
                                        </h3>
                                        <button
                                            onClick={onCancel}
                                            className="p-1 rounded-lg hover:bg-surface-100 dark:hover:bg-surface-700 transition-colors"
                                        >
                                            <X className="w-5 h-5 text-surface-400" />
                                        </button>
                                    </div>
                                    <p className="mt-2 text-sm text-surface-600 dark:text-surface-400">
                                        {message}
                                    </p>
                                </div>
                            </div>

                            <div className="flex gap-3 mt-6 justify-end">
                                <button
                                    onClick={onCancel}
                                    disabled={isLoading}
                                    className="btn-secondary text-sm py-2.5 px-5"
                                >
                                    {cancelLabel}
                                </button>
                                <button
                                    onClick={onConfirm}
                                    disabled={isLoading}
                                    className={`py-2.5 px-5 rounded-xl text-sm font-semibold transition-all
                                        disabled:opacity-50 disabled:cursor-not-allowed
                                        ${variant === 'danger'
                                            ? 'bg-red-600 hover:bg-red-700 text-white shadow-md shadow-red-500/25'
                                            : 'btn-primary'
                                        }`}
                                >
                                    {isLoading ? (
                                        <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin mx-auto" />
                                    ) : (
                                        confirmLabel
                                    )}
                                </button>
                            </div>
                        </div>
                    </motion.div>
                </>
            )}
        </AnimatePresence>
    );
}
