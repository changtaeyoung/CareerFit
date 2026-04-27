interface StatusBadgeProps {
    status: string;
    size?: 'sm' | 'md';
}

const statusConfig: Record<string, { bg: string; text: string; label: string }> = {
    ACTIVE: { bg: 'bg-accent-100 dark:bg-accent-500/15', text: 'text-accent-700 dark:text-accent-400', label: '진행중' },
    CLOSED: { bg: 'bg-surface-100 dark:bg-surface-700', text: 'text-surface-500 dark:text-surface-400', label: '마감' },
    SCHEDULED: { bg: 'bg-brand-100 dark:bg-brand-500/15', text: 'text-brand-700 dark:text-brand-400', label: '예정' },
    COMPLETED: { bg: 'bg-accent-100 dark:bg-accent-500/15', text: 'text-accent-700 dark:text-accent-400', label: '완료' },
    FAILED: { bg: 'bg-red-100 dark:bg-red-500/15', text: 'text-red-700 dark:text-red-400', label: '실패' },
    PENDING: { bg: 'bg-amber-100 dark:bg-amber-500/15', text: 'text-amber-700 dark:text-amber-400', label: '대기중' },
};

export default function StatusBadge({ status, size = 'sm' }: StatusBadgeProps) {
    const config = statusConfig[status] || {
        bg: 'bg-surface-100 dark:bg-surface-700',
        text: 'text-surface-600 dark:text-surface-400',
        label: status,
    };

    return (
        <span
            className={`inline-flex items-center font-medium rounded-full
                ${config.bg} ${config.text}
                ${size === 'sm' ? 'px-2.5 py-0.5 text-xs' : 'px-3 py-1 text-sm'}`}
        >
            {config.label}
        </span>
    );
}
