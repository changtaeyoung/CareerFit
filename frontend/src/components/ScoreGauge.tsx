import { motion } from 'framer-motion';

interface ScoreGaugeProps {
    score: number;        // 0~100
    size?: number;        // px, default 120
    strokeWidth?: number; // default 10
    label?: string;
    showLabel?: boolean;
}

export default function ScoreGauge({
    score,
    size = 120,
    strokeWidth = 10,
    label = '핏 점수',
    showLabel = true,
}: ScoreGaugeProps) {
    const radius = (size - strokeWidth) / 2;
    const circumference = 2 * Math.PI * radius;
    const offset = circumference - (score / 100) * circumference;

    // 점수에 따른 색상
    const getColor = () => {
        if (score >= 80) return { stroke: '#10B981', text: 'text-accent-500' };
        if (score >= 60) return { stroke: '#3B82F6', text: 'text-brand-500' };
        if (score >= 40) return { stroke: '#F59E0B', text: 'text-amber-500' };
        return { stroke: '#EF4444', text: 'text-red-500' };
    };

    const color = getColor();

    return (
        <div className="flex flex-col items-center gap-2">
            <div className="relative" style={{ width: size, height: size }}>
                <svg
                    width={size}
                    height={size}
                    className="transform -rotate-90"
                >
                    {/* 배경 원 */}
                    <circle
                        cx={size / 2}
                        cy={size / 2}
                        r={radius}
                        fill="none"
                        strokeWidth={strokeWidth}
                        className="stroke-surface-200 dark:stroke-surface-700"
                    />
                    {/* 진행 원 */}
                    <motion.circle
                        cx={size / 2}
                        cy={size / 2}
                        r={radius}
                        fill="none"
                        strokeWidth={strokeWidth}
                        stroke={color.stroke}
                        strokeLinecap="round"
                        strokeDasharray={circumference}
                        initial={{ strokeDashoffset: circumference }}
                        animate={{ strokeDashoffset: offset }}
                        transition={{ duration: 1.2, ease: 'easeOut', delay: 0.3 }}
                    />
                </svg>
                {/* 중앙 숫자 */}
                <div className="absolute inset-0 flex items-center justify-center">
                    <motion.span
                        initial={{ opacity: 0, scale: 0.5 }}
                        animate={{ opacity: 1, scale: 1 }}
                        transition={{ delay: 0.5, duration: 0.4 }}
                        className={`text-2xl font-bold ${color.text}`}
                    >
                        {score}
                    </motion.span>
                </div>
            </div>
            {showLabel && (
                <span className="text-xs font-medium text-surface-500 dark:text-surface-400">
                    {label}
                </span>
            )}
        </div>
    );
}
