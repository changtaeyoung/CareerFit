import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { BarChart3, Trash2 } from 'lucide-react';
import { getAnalysisHistory, deleteReport } from '../api/analysis';
import type { AnalysisHistoryResponse } from '../api/analysis';
import ScoreGauge from '../components/ScoreGauge';
import StatusBadge from '../components/StatusBadge';
import EmptyState from '../components/EmptyState';
import ConfirmModal from '../components/ConfirmModal';

export default function AnalysisHistory() {
    const navigate = useNavigate();
    const [history, setHistory] = useState<AnalysisHistoryResponse[]>([]);
    const [loading, setLoading] = useState(true);
    const [deleteTarget, setDeleteTarget] = useState<number | null>(null);
    const [deleting, setDeleting] = useState(false);

    const fetchHistory = async () => {
        setLoading(true);
        try {
            const data = await getAnalysisHistory();
            setHistory(data);
        } catch {
            setHistory([]);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => { fetchHistory(); }, []);

    const handleDelete = async () => {
        if (!deleteTarget) return;
        setDeleting(true);
        try {
            await deleteReport(deleteTarget);
            setHistory((prev) => prev.filter((h) => h.reportId !== deleteTarget));
        } catch {
            alert('삭제에 실패했습니다');
        } finally {
            setDeleting(false);
            setDeleteTarget(null);
        }
    };

    const formatDate = (dateStr: string) => {
        return new Date(dateStr).toLocaleDateString('ko-KR', {
            year: 'numeric', month: 'short', day: 'numeric',
        });
    };

    return (
        <div className="space-y-6">
            <div>
                <h1 className="section-title">핏 분석 히스토리</h1>
                <p className="section-subtitle mt-1">나의 핏 분석 결과를 확인하고 관리하세요</p>
            </div>

            {loading ? (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                    {Array.from({ length: 6 }).map((_, i) => (
                        <div key={i} className="glass-card p-6 animate-pulse">
                            <div className="w-16 h-16 bg-surface-200 dark:bg-surface-700 rounded-full mx-auto mb-4" />
                            <div className="h-5 bg-surface-200 dark:bg-surface-700 rounded w-2/3 mx-auto mb-2" />
                            <div className="h-4 bg-surface-200 dark:bg-surface-700 rounded w-1/2 mx-auto" />
                        </div>
                    ))}
                </div>
            ) : history.length === 0 ? (
                <EmptyState
                    icon={<BarChart3 className="w-8 h-8 text-surface-400" />}
                    title="분석 결과가 없습니다"
                    description="채용공고에서 핏 분석을 실행해보세요"
                    action={
                        <button onClick={() => navigate('/postings')} className="btn-primary text-sm">
                            채용공고 보러가기
                        </button>
                    }
                />
            ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                    {history.map((item, i) => (
                        <motion.div
                            key={item.reportId}
                            initial={{ opacity: 0, y: 20 }}
                            animate={{ opacity: 1, y: 0 }}
                            transition={{ delay: 0.05 * i }}
                            className="glass-card p-6 group relative"
                        >
                            {/* 삭제 버튼 */}
                            <button
                                onClick={(e) => { e.stopPropagation(); setDeleteTarget(item.reportId); }}
                                className="absolute top-3 right-3 p-1.5 rounded-lg
                                           opacity-0 group-hover:opacity-100
                                           hover:bg-red-50 dark:hover:bg-red-500/10 transition-all"
                            >
                                <Trash2 className="w-4 h-4 text-red-500" />
                            </button>

                            <div
                                onClick={() => navigate(`/analysis/${item.reportId}`)}
                                className="cursor-pointer"
                            >
                                <div className="flex items-center justify-center mb-4">
                                    <ScoreGauge score={item.totalScore} size={80} strokeWidth={7} showLabel={false} />
                                </div>

                                <div className="text-center">
                                    <p className="font-semibold text-surface-900 dark:text-surface-100 truncate">
                                        {item.companyName}
                                    </p>
                                    <p className="text-xs text-surface-500 dark:text-surface-400 truncate mt-0.5">
                                        {item.postingTitle}
                                    </p>
                                </div>

                                <div className="flex items-center justify-between mt-4 pt-3 border-t border-surface-200 dark:border-surface-700">
                                    <span className="text-xs text-surface-400 dark:text-surface-500">
                                        {formatDate(item.createdAt)}
                                    </span>
                                    <div className="flex items-center gap-2">
                                        <StatusBadge status={item.status} />
                                    </div>
                                </div>
                            </div>
                        </motion.div>
                    ))}
                </div>
            )}

            <ConfirmModal
                isOpen={deleteTarget !== null}
                title="분석 결과 삭제"
                message="이 분석 결과를 삭제하시겠습니까? 갭 분석과 액션 플랜도 함께 삭제됩니다."
                confirmLabel="삭제"
                variant="danger"
                onConfirm={handleDelete}
                onCancel={() => setDeleteTarget(null)}
                isLoading={deleting}
            />
        </div>
    );
}
