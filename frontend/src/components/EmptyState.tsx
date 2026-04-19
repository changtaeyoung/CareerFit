import { motion } from 'framer-motion';
import { Inbox } from 'lucide-react';

interface EmptyStateProps {
    icon?: React.ReactNode;
    title: string;
    description?: string;
    action?: React.ReactNode;
}

export default function EmptyState({ icon, title, description, action }: EmptyStateProps) {
    return (
        <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5 }}
            className="flex flex-col items-center justify-center py-16 px-4"
        >
            <div className="w-16 h-16 rounded-2xl bg-surface-100 dark:bg-surface-800
                            flex items-center justify-center mb-4">
                {icon || <Inbox className="w-8 h-8 text-surface-400" />}
            </div>
            <h3 className="text-lg font-semibold text-surface-700 dark:text-surface-300 mb-1">
                {title}
            </h3>
            {description && (
                <p className="text-sm text-surface-500 dark:text-surface-400 text-center max-w-sm">
                    {description}
                </p>
            )}
            {action && <div className="mt-4">{action}</div>}
        </motion.div>
    );
}
