import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import {
    ArrowLeft, Building2, Briefcase, Calendar,
    ExternalLink, AlertTriangle, BarChart3, Clock
} from 'lucide-react';
import { getPostingDetail } from '../api/company';
import type { JobPostingResponse } from '../api/company';
import { checkPrerequisite, analyze } from '../api/analysis';
import type { PrerequisiteCheckResponse } from '../api/analysis';
import StatusBadge from '../components/StatusBadge';

export default function PostingDetail() {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const [posting, setPosting] = useState<JobPostingResponse | null>(null);
    const [prereq, setPrereq] = useState<PrerequisiteCheckResponse | null>(null);
    const [loading, setLoading] = useState(true);
    const [analyzing, setAnalyzing] = useState(false);

    useEffect(() => {
        if (!id) return;
        const fetchData = async () => {
            setLoading(true);
            try {
                const postData = await getPostingDetail(Number(id));
                setPosting(postData);
                // 필수 자격 사전 체크
                try {
                    const prereqData = await checkPrerequisite(Number(id));
                    setPrereq(prereqData);
                } catch {
                    // 스펙 미등록 시 체크 실패 가능 — 무시
                }
            } catch {
                navigate('/postings');
            } finally {
                setLoading(false);
            }
        };
        fetchData();
    }, [id]);

    const handleAnalyze = async () => {
        if (!posting) return;
        setAnalyzing(true);
        try {
            const report = await analyze({ jobPostingId: posting.id });
            navigate(`/analysis/${report.reportId}`);
        } catch (err: any) {
            alert(err.response?.data?.message || '분석 실행에 실패했습니다. 스펙을 먼저 등록해주세요.');
        } finally {
            setAnalyzing(false);
        }
    };

    if (loading || !posting) {
        return (
            <div className="space-y-6 animate-pulse">
                <div className="h-8 bg-surface-200 dark:bg-surface-700 rounded w-32" />
                <div className="glass-card p-8">
                    <div className="h-8 bg-surface-200 dark:bg-surface-700 rounded w-2/3 mb-4" />
                    <div className="h-6 bg-surface-200 dark:bg-surface-700 rounded w-1/2" />
                </div>
            </div>
        );
    }

    const getDaysRemaining = () => {
        if (!posting.deadline) return null;
        return Math.ceil((new Date(posting.deadline).getTime() - Date.now()) / (1000 * 60 * 60 * 24));
    };

    const daysLeft = getDaysRemaining();

    return (
        <div className="space-y-6 max-w-3xl">
            <button
                onClick={() => navigate('/postings')}
                className="btn-ghost flex items-center gap-2 -ml-2"
            >
                <ArrowLeft className="w-4 h-4" />
                채용공고 목록
            </button>

            {/* 필수 자격 미충족 경고 */}
            {prereq && !prereq.eligible && (
                <motion.div
                    initial={{ opacity: 0, y: -10 }}
                    animate={{ opacity: 1, y: 0 }}
                    className="bg-red-50 dark:bg-red-500/10 border border-red-200 dark:border-red-500/20 rounded-2xl p-5"
                >
                    <div className="flex items-start gap-3">
                        <AlertTriangle className="w-5 h-5 text-red-500 mt-0.5 flex-shrink-0" />
                        <div>
                            <p className="font-semibold text-red-800 dark:text-red-400">필수 자격 미충족</p>
                            <ul className="mt-2 space-y-1">
                                {prereq.failedRequirements.map((req, i) => (
                                    <li key={i} className="text-sm text-red-700 dark:text-red-400">
                                        • {req.message}
                                    </li>
                                ))}
                            </ul>
                        </div>
                    </div>
                </motion.div>
            )}

            {/* 공고 상세 카드 */}
            <motion.div
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                className="glass-card p-8"
            >
                <div className="flex items-start justify-between gap-4 mb-6">
                    <div>
                        <h1 className="text-xl font-bold text-surface-900 dark:text-surface-50">
                            {posting.title}
                        </h1>
                        <button
                            onClick={() => navigate(`/companies/${posting.companyId}`)}
                            className="flex items-center gap-1.5 mt-2 text-brand-600 dark:text-brand-400 text-sm font-medium hover:underline"
                        >
                            <Building2 className="w-4 h-4" />
                            {posting.companyName}
                        </button>
                    </div>
                    <StatusBadge status={posting.status} size="md" />
                </div>

                <div className="grid grid-cols-2 md:grid-cols-4 gap-4 py-6 border-y border-surface-200 dark:border-surface-700">
                    <div>
                        <p className="text-xs text-surface-400 dark:text-surface-500 mb-0.5">직무</p>
                        <p className="text-sm font-medium text-surface-800 dark:text-surface-200 flex items-center gap-1.5">
                            <Briefcase className="w-4 h-4 text-surface-400" />
                            {posting.jobType}
                        </p>
                    </div>
                    <div>
                        <p className="text-xs text-surface-400 dark:text-surface-500 mb-0.5">시즌</p>
                        <p className="text-sm font-medium text-surface-800 dark:text-surface-200 flex items-center gap-1.5">
                            <Calendar className="w-4 h-4 text-surface-400" />
                            {posting.season}
                        </p>
                    </div>
                    <div>
                        <p className="text-xs text-surface-400 dark:text-surface-500 mb-0.5">접수 기간</p>
                        <p className="text-sm font-medium text-surface-800 dark:text-surface-200">
                            {posting.startedAt || '—'} ~ {posting.deadline || '—'}
                        </p>
                    </div>
                    <div>
                        <p className="text-xs text-surface-400 dark:text-surface-500 mb-0.5">남은 기간</p>
                        {daysLeft !== null && daysLeft >= 0 ? (
                            <p className={`text-sm font-bold flex items-center gap-1.5 ${
                                daysLeft <= 3 ? 'text-red-600 dark:text-red-400' :
                                daysLeft <= 7 ? 'text-amber-600 dark:text-amber-400' :
                                'text-accent-600 dark:text-accent-400'
                            }`}>
                                <Clock className="w-4 h-4" />
                                D-{daysLeft}
                            </p>
                        ) : (
                            <p className="text-sm text-surface-500 dark:text-surface-400">마감됨</p>
                        )}
                    </div>
                </div>

                {/* 액션 영역 */}
                <div className="flex flex-col sm:flex-row items-center gap-3 mt-6">
                    <motion.button
                        whileHover={{ scale: 1.02 }}
                        whileTap={{ scale: 0.98 }}
                        onClick={handleAnalyze}
                        disabled={analyzing}
                        className="btn-primary flex items-center gap-2 w-full sm:w-auto"
                    >
                        {analyzing ? (
                            <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin" />
                        ) : (
                            <BarChart3 className="w-5 h-5" />
                        )}
                        {analyzing ? '분석중...' : '핏 분석 실행'}
                    </motion.button>

                    {posting.url && (
                        <a
                            href={posting.url}
                            target="_blank"
                            rel="noopener noreferrer"
                            className="btn-secondary flex items-center gap-2 w-full sm:w-auto justify-center"
                        >
                            <ExternalLink className="w-4 h-4" />
                            원문 공고 보기
                        </a>
                    )}
                </div>
            </motion.div>
        </div>
    );
}
