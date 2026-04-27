import { motion, AnimatePresence } from 'framer-motion';
import { CheckCircle2, XCircle, Info, AlertTriangle, X } from 'lucide-react';
import { useToastStore } from '../store/toastStore';
import type { ToastType } from '../store/toastStore';

const iconMap: Record<ToastType, React.ReactNode> = {
    success: <CheckCircle2 className="w-5 h-5 text-accent-500" />,
    error: <XCircle className="w-5 h-5 text-red-500" />,
    info: <Info className="w-5 h-5 text-brand-500" />,
    warning: <AlertTriangle className="w-5 h-5 text-amber-500" />,
};

const bgMap: Record<ToastType, string> = {
    success: 'bg-accent-50 dark:bg-accent-500/10 border-accent-200 dark:border-accent-500/20',
    error: 'bg-red-50 dark:bg-red-500/10 border-red-200 dark:border-red-500/20',
    info: 'bg-brand-50 dark:bg-brand-500/10 border-brand-200 dark:border-brand-500/20',
    warning: 'bg-amber-50 dark:bg-amber-500/10 border-amber-200 dark:border-amber-500/20',
};

export default function ToastContainer() {
    const { toasts, removeToast } = useToastStore();

    return (
        <div className="fixed top-4 right-4 z-[100] flex flex-col gap-2 max-w-sm">
            <AnimatePresence>
                {toasts.map((t) => (
                    <motion.div
                        key={t.id}
                        initial={{ opacity: 0, x: 80, scale: 0.9 }}
                        animate={{ opacity: 1, x: 0, scale: 1 }}
                        exit={{ opacity: 0, x: 80, scale: 0.9 }}
                        transition={{ type: 'spring', duration: 0.35 }}
                        className={`flex items-center gap-3 px-4 py-3 rounded-xl border shadow-lg backdrop-blur-sm ${bgMap[t.type]}`}
                    >
                        {iconMap[t.type]}
                        <p className="text-sm font-medium text-surface-800 dark:text-surface-200 flex-1">
                            {t.message}
                        </p>
                        <button
                            onClick={() => removeToast(t.id)}
                            className="p-1 rounded-lg hover:bg-black/5 dark:hover:bg-white/5 transition-colors flex-shrink-0"
                        >
                            <X className="w-4 h-4 text-surface-400" />
                        </button>
                    </motion.div>
                ))}
            </AnimatePresence>
        </div>
    );
}
