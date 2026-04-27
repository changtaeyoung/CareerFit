import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import {
    ArrowLeft, CheckCircle2, XCircle,
    Target, Lightbulb, Calendar
} from 'lucide-react';
import { getReportDetail } from '../api/analysis';
import type { AnalysisReportResponse } from '../api/analysis';
import ScoreGauge from '../components/ScoreGauge';

export default function AnalysisReport() {
    const { reportId } = useParams<{ reportId: string }>();
    const navigate = useNavigate();
    const [report, setReport] = useState<AnalysisReportResponse | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        if (!reportId) return;
        const fetchReport = async () => {
            setLoading(true);
            try {
                const data = await getReportDetail(Number(reportId));
                setReport(data);
            } catch {
                navigate('/analysis/history');
            } finally {
                setLoading(false);
            }
        };
        fetchReport();
    }, [reportId]);

    if (loading || !report) {
        return (
            <div className="space-y-6 animate-pulse max-w-4xl">
                <div className="h-8 bg-surface-200 dark:bg-surface-700 rounded w-32" />
                <div className="glass-card p-8">
                    <div className="w-28 h-28 bg-surface-200 dark:bg-surface-700 rounded-full mx-auto mb-6" />
                    <div className="h-6 bg-surface-200 dark:bg-surface-700 rounded w-1/3 mx-auto" />
                </div>
            </div>
        );
    }

    const priorityConfig: Record<string, { color: string; bg: string; label: string }> = {
        P1: { color: 'text-red-600 dark:text-red-400', bg: 'bg-red-100 dark:bg-red-500/15', label: '긴급' },
        P2: { color: 'text-amber-600 dark:text-amber-400', bg: 'bg-amber-100 dark:bg-amber-500/15', label: '중요' },
        P3: { color: 'text-brand-600 dark:text-brand-400', bg: 'bg-brand-100 dark:bg-brand-500/15', label: '권장' },
    };

    const scoreItems = [
        { label: '필수조건 베이스', value: report.baseScore, max: 10, desc: report.requiredAllMet ? '전체 충족' : '미충족 항목 있음' },
        { label: '정량 가점', value: report.quantitativeBonus, max: 40, desc: '자격증 + 어학 + 기술스택' },
        { label: '정성 가점', value: report.qualitativeBonus, max: 50, desc: '자소서 유사도 (추후 반영)' },
    ];

    return (
        <div className="space-y-6 max-w-6xl mx-auto">
            <button
                onClick={() => navigate('/analysis/history')}
                className="btn-ghost flex items-center gap-2 -ml-2"
            >
                <ArrowLeft className="w-4 h-4" />
                히스토리
            </button>

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                {/* 메인 컬럼 (좌측 2칸) */}
                <div className="lg:col-span-2 space-y-6">
                {/* 점수 요약 */}
                <motion.div
                    initial={{ opacity: 0, y: 20 }}
                    animate={{ opacity: 1, y: 0 }}
                    className="glass-card p-8 text-center"
                >
                    <div className="flex items-center justify-center gap-2 mb-6">
                        <span className="text-sm font-semibold text-surface-500 dark:text-surface-400">
                            {report.companyName}
                        </span>
                        <span className="text-surface-300 dark:text-surface-600">|</span>
                        <span className="text-sm text-surface-500 dark:text-surface-400">
                            {report.postingTitle}
                        </span>
                    </div>

                    <div className="flex justify-center mb-6">
                        <ScoreGauge score={report.totalScore} size={160} strokeWidth={12} label="최종 핏 점수" />
                    </div>

                    <div className="flex items-center justify-center gap-3 mb-6">
                        <span className={`flex items-center gap-1.5 text-sm font-medium px-4 py-1.5 rounded-full ${
                            report.requiredAllMet
                                ? 'bg-accent-100 dark:bg-accent-500/15 text-accent-700 dark:text-accent-400'
                                : 'bg-red-100 dark:bg-red-500/15 text-red-700 dark:text-red-400'
                        }`}>
                            {report.requiredAllMet
                                ? <><CheckCircle2 className="w-4 h-4" /> 필수조건 전체 충족</>
                                : <><XCircle className="w-4 h-4" /> 필수조건 미충족</>
                            }
                        </span>
                        <span className="text-xs text-surface-400 dark:text-surface-500 flex items-center gap-1 border border-surface-200 dark:border-surface-700 px-3 py-1.5 rounded-full">
                            <Calendar className="w-3.5 h-3.5" />
                            스펙 v{report.specVersionNo}
                        </span>
                    </div>

                    <div className="grid grid-cols-3 gap-4 mt-6 pt-6 border-t border-surface-200 dark:border-surface-700">
                        {scoreItems.map((item) => (
                            <div key={item.label}>
                                <p className="text-2xl font-bold text-surface-900 dark:text-surface-100">
                                    {item.value}<span className="text-sm font-normal text-surface-400">/{item.max}</span>
                                </p>
                                <p className="text-sm font-medium text-surface-700 dark:text-surface-300 mt-1">{item.label}</p>
                                <p className="text-xs text-surface-400 dark:text-surface-500 mt-0.5">{item.desc}</p>
                            </div>
                        ))}
                    </div>
                </motion.div>

                {/* 갭 분석 */}
            {report.gaps && report.gaps.length > 0 && (
                <motion.div
                    initial={{ opacity: 0, y: 20 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ delay: 0.1 }}
                    className="glass-card p-6"
                >
                    <div className="flex items-center gap-2 mb-5">
                        <Target className="w-5 h-5 text-brand-500" />
                        <h2 className="font-semibold text-surface-800 dark:text-surface-200 text-lg">갭 분석</h2>
                    </div>

                    <div className="overflow-x-auto">
                        <table className="w-full text-sm">
                            <thead>
                                <tr className="border-b border-surface-200 dark:border-surface-700">
                                    <th className="text-left py-3 px-4 text-xs font-semibold text-surface-500 dark:text-surface-400 uppercase">카테고리</th>
                                    <th className="text-left py-3 px-4 text-xs font-semibold text-surface-500 dark:text-surface-400 uppercase">항목</th>
                                    <th className="text-left py-3 px-4 text-xs font-semibold text-surface-500 dark:text-surface-400 uppercase">요구 수준</th>
                                    <th className="text-left py-3 px-4 text-xs font-semibold text-surface-500 dark:text-surface-400 uppercase">내 수준</th>
                                    <th className="text-center py-3 px-4 text-xs font-semibold text-surface-500 dark:text-surface-400 uppercase">충족</th>
                                </tr>
                            </thead>
                            <tbody>
                                {report.gaps.map((gap) => (
                                    <tr key={gap.id} className="border-b border-surface-100 dark:border-surface-700/50 last:border-0">
                                        <td className="py-3 px-4">
                                            <span className="text-xs font-medium px-2 py-0.5 rounded-full bg-surface-100 dark:bg-surface-700 text-surface-600 dark:text-surface-400">
                                                {gap.category}
                                            </span>
                                        </td>
                                        <td className="py-3 px-4 font-medium text-surface-800 dark:text-surface-200">{gap.itemName}</td>
                                        <td className="py-3 px-4 text-surface-600 dark:text-surface-400">{gap.requiredLevel}</td>
                                        <td className="py-3 px-4 text-surface-600 dark:text-surface-400">{gap.userLevel || '—'}</td>
                                        <td className="py-3 px-4 text-center">
                                            {gap.met
                                                ? <CheckCircle2 className="w-5 h-5 text-accent-500 mx-auto" />
                                                : <XCircle className="w-5 h-5 text-red-500 mx-auto" />
                                            }
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </motion.div>
            )}

                {/* 액션 플랜 */}
                {report.recommendations && report.recommendations.length > 0 && (
                    <motion.div
                        initial={{ opacity: 0, y: 20 }}
                        animate={{ opacity: 1, y: 0 }}
                        transition={{ delay: 0.2 }}
                        className="glass-card p-6"
                    >
                        <div className="flex items-center gap-2 mb-5">
                            <Lightbulb className="w-5 h-5 text-amber-500" />
                            <h2 className="font-semibold text-surface-800 dark:text-surface-200 text-lg">액션 플랜</h2>
                        </div>

                        <div className="space-y-4">
                            {report.recommendations.map((rec) => {
                                const config = priorityConfig[rec.priority] || priorityConfig.P3;
                                return (
                                    <div key={rec.id} className="flex items-start gap-4 p-4 rounded-xl border border-surface-200 dark:border-surface-700/50 bg-white dark:bg-surface-800 shadow-sm hover:shadow-md transition-shadow">
                                        <div className={`flex-shrink-0 w-12 h-12 rounded-full flex items-center justify-center ${config.bg} ${config.color}`}>
                                            <Target className="w-6 h-6" />
                                        </div>
                                        <div className="flex-1 min-w-0 pt-0.5">
                                            <div className="flex items-center gap-2 mb-1">
                                                <span className={`text-[10px] font-bold px-2 py-0.5 rounded-sm uppercase tracking-wider ${config.bg} ${config.color}`}>
                                                    {config.label}
                                                </span>
                                                <span className="text-xs font-semibold text-surface-500 dark:text-surface-400 bg-surface-100 dark:bg-surface-700 px-2 py-0.5 rounded-sm">
                                                    {rec.category}
                                                </span>
                                            </div>
                                            <p className="font-semibold text-surface-900 dark:text-surface-100 text-base">
                                                {rec.title}
                                            </p>
                                            <p className="text-sm text-surface-600 dark:text-surface-400 mt-1">
                                                {rec.description}
                                            </p>
                                        </div>
                                    </div>
                                );
                            })}
                        </div>
                    </motion.div>
                )}
                </div>

                {/* 사이드 컬럼 (우측 1칸) - AI 피드백 */}
                <div className="space-y-6">
                    <motion.div
                        initial={{ opacity: 0, x: 20 }}
                        animate={{ opacity: 1, x: 0 }}
                        transition={{ delay: 0.3 }}
                        className="glass-card p-6 sticky top-6"
                    >
                        <div className="flex items-center gap-2 mb-6 pb-4 border-b border-surface-200 dark:border-surface-700">
                            <div className="w-8 h-8 rounded-full bg-gradient-to-tr from-brand-500 to-accent-500 flex items-center justify-center text-white">
                                <Lightbulb className="w-4 h-4" />
                            </div>
                            <h2 className="font-semibold text-surface-900 dark:text-surface-100">AI 종합 분석 요약</h2>
                        </div>

                        <div className="space-y-6">
                            {/* 강점 */}
                            <div>
                                <h3 className="text-sm font-semibold text-accent-600 dark:text-accent-400 mb-3 flex items-center gap-2">
                                    <CheckCircle2 className="w-4 h-4" /> 나의 강점
                                </h3>
                                {report.strengths && report.strengths.length > 0 ? (
                                    <ul className="space-y-2">
                                        {report.strengths.map((s, i) => (
                                            <li key={i} className="text-sm text-surface-600 dark:text-surface-300 bg-accent-50 dark:bg-accent-500/10 p-3 rounded-lg border border-accent-100 dark:border-accent-500/20">
                                                {s}
                                            </li>
                                        ))}
                                    </ul>
                                ) : (
                                    <p className="text-sm text-surface-400 dark:text-surface-500 bg-surface-50 dark:bg-surface-800/50 p-3 rounded-lg border border-surface-100 dark:border-surface-700">
                                        자소서를 작성하면 AI가 강점을 분석해드립니다.
                                    </p>
                                )}
                            </div>

                            {/* 보완할 점 */}
                            <div>
                                <h3 className="text-sm font-semibold text-red-600 dark:text-red-400 mb-3 flex items-center gap-2">
                                    <XCircle className="w-4 h-4" /> 보완할 점
                                </h3>
                                {report.weaknesses && report.weaknesses.length > 0 ? (
                                    <ul className="space-y-2">
                                        {report.weaknesses.map((w, i) => (
                                            <li key={i} className="text-sm text-surface-600 dark:text-surface-300 bg-red-50 dark:bg-red-500/10 p-3 rounded-lg border border-red-100 dark:border-red-500/20">
                                                {w}
                                            </li>
                                        ))}
                                    </ul>
                                ) : (
                                    <p className="text-sm text-surface-400 dark:text-surface-500 bg-surface-50 dark:bg-surface-800/50 p-3 rounded-lg border border-surface-100 dark:border-surface-700">
                                        자소서를 작성하면 AI가 개선점을 분석해드립니다.
                                    </p>
                                )}
                            </div>

                            {/* 종합 피드백 */}
                            <div>
                                <h3 className="text-sm font-semibold text-surface-800 dark:text-surface-200 mb-2">AI 종합 피드백</h3>
                                <div className="text-sm text-surface-600 dark:text-surface-400 bg-surface-100 dark:bg-surface-700/50 p-4 rounded-xl leading-relaxed italic">
                                    {report.aiFeedback
                                        ? `"${report.aiFeedback}"`
                                        : '"자소서를 작성하고 분석을 실행하면 AI가 자소서에 대한 종합적인 피드백을 제공합니다."'
                                    }
                                </div>
                            </div>
                        </div>
                    </motion.div>
                </div>
            </div>
        </div>
    );
}
