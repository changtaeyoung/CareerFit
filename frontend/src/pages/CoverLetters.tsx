import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import {
    PenLine, Plus, Trash2, Save,
    Building2, FileText, Type, BarChart3
} from 'lucide-react';
import { getPostings } from '../api/company';
import type { JobPostingResponse } from '../api/company';
import {
    saveCoverLetter, getCoverLettersByPosting, deleteCoverLetter,
} from '../api/coverLetter';
import type { CoverLetterResponse } from '../api/coverLetter';
import EmptyState from '../components/EmptyState';
import ConfirmModal from '../components/ConfirmModal';
import { toast } from '../store/toastStore';

export default function CoverLetters() {
    const navigate = useNavigate();
    const [postings, setPostings] = useState<JobPostingResponse[]>([]);
    const [selectedPostingId, setSelectedPostingId] = useState<number | null>(null);
    const [letters, setLetters] = useState<CoverLetterResponse[]>([]);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [deleteTarget, setDeleteTarget] = useState<number | null>(null);
    const [deleting, setDeleting] = useState(false);
    const [showNewDraft, setShowNewDraft] = useState(false);
    const [newContent, setNewContent] = useState('');

    useEffect(() => {
        const fetchPostings = async () => {
            try {
                const data = await getPostings(1, 100);
                setPostings(data);
            } catch {
                setPostings([]);
            } finally {
                setLoading(false);
            }
        };
        fetchPostings();
    }, []);

    const handleSelectPosting = async (postingId: number) => {
        if (selectedPostingId === postingId) {
            setSelectedPostingId(null);
            setLetters([]);
            return;
        }
        setSelectedPostingId(postingId);
        try {
            const data = await getCoverLettersByPosting(postingId);
            setLetters(data);
        } catch {
            setLetters([]);
        }
    };

    const handleSaveDraft = async () => {
        if (!selectedPostingId || !newContent.trim()) return;
        setSaving(true);
        try {
            await saveCoverLetter({
                postingId: selectedPostingId,
                questionId: 0,
                content: newContent,
            });
            toast.success('자소서가 저장되었습니다');
            // 새로고침
            const data = await getCoverLettersByPosting(selectedPostingId);
            setLetters(data);
            setShowNewDraft(false);
            setNewContent('');
        } catch (err: any) {
            toast.error(err.response?.data?.message || '저장에 실패했습니다');
        } finally {
            setSaving(false);
        }
    };

    const handleDelete = async () => {
        if (!deleteTarget || !selectedPostingId) return;
        setDeleting(true);
        try {
            await deleteCoverLetter(deleteTarget);
            toast.success('자소서가 삭제되었습니다');
            const data = await getCoverLettersByPosting(selectedPostingId);
            setLetters(data);
        } catch {
            toast.error('삭제에 실패했습니다');
        } finally {
            setDeleting(false);
            setDeleteTarget(null);
        }
    };

    const charCount = (text: string) => text.replace(/\s/g, '').length;

    return (
        <div className="space-y-6">
            <div>
                <h1 className="section-title">자소서 관리</h1>
                <p className="section-subtitle mt-1">채용공고별로 자소서를 작성하고 관리하세요</p>
            </div>

            {loading ? (
                <div className="space-y-3">
                    {Array.from({ length: 4 }).map((_, i) => (
                        <div key={i} className="glass-card p-5 animate-pulse">
                            <div className="h-5 bg-surface-200 dark:bg-surface-700 rounded w-2/3 mb-2" />
                            <div className="h-4 bg-surface-200 dark:bg-surface-700 rounded w-1/3" />
                        </div>
                    ))}
                </div>
            ) : postings.length === 0 ? (
                <EmptyState
                    icon={<PenLine className="w-8 h-8 text-surface-400" />}
                    title="채용공고가 없습니다"
                    description="채용공고가 등록되면 자소서를 작성할 수 있습니다"
                    action={
                        <button onClick={() => navigate('/postings')} className="btn-primary text-sm">
                            채용공고 보러가기
                        </button>
                    }
                />
            ) : (
                <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                    {/* 왼쪽: 공고 목록 */}
                    <div className="space-y-2">
                        <h2 className="text-sm font-semibold text-surface-500 dark:text-surface-400 uppercase mb-3">
                            채용공고 선택
                        </h2>
                        {postings.map((posting) => (
                            <button
                                key={posting.id}
                                onClick={() => handleSelectPosting(posting.id)}
                                className={`w-full text-left p-4 rounded-xl border transition-all duration-200
                                    ${selectedPostingId === posting.id
                                        ? 'bg-brand-50 dark:bg-brand-500/10 border-brand-300 dark:border-brand-500/30 shadow-sm'
                                        : 'bg-white dark:bg-surface-800 border-surface-200 dark:border-surface-700 hover:border-surface-300 dark:hover:border-surface-600'
                                    }`}
                            >
                                <p className="font-semibold text-surface-900 dark:text-surface-100 text-sm truncate">
                                    {posting.title}
                                </p>
                                <p className="text-xs text-surface-500 dark:text-surface-400 mt-1 flex items-center gap-1">
                                    <Building2 className="w-3 h-3" />
                                    {posting.companyName}
                                </p>
                            </button>
                        ))}
                    </div>

                    {/* 오른쪽: 자소서 내용 */}
                    <div className="lg:col-span-2">
                        {selectedPostingId ? (
                            <div className="space-y-4">
                                <div className="flex items-center justify-between">
                                    <h2 className="font-semibold text-surface-800 dark:text-surface-200">
                                        작성한 자소서 ({letters.length})
                                    </h2>
                                    <div className="flex items-center gap-3">
                                        <button
                                            onClick={() => navigate(`/postings/${selectedPostingId}`)}
                                            className="flex items-center gap-1.5 text-sm font-medium text-accent-600 dark:text-accent-400 hover:text-accent-700 transition-colors"
                                        >
                                            <BarChart3 className="w-4 h-4" />
                                            핏 분석 보기
                                        </button>
                                        <button
                                            onClick={() => setShowNewDraft(true)}
                                            className="flex items-center gap-1.5 text-sm font-medium text-brand-600 dark:text-brand-400 hover:text-brand-700 transition-colors"
                                        >
                                            <Plus className="w-4 h-4" />
                                            새 항목 작성
                                        </button>
                                    </div>
                                </div>

                                {/* 새 자소서 작성 폼 */}
                                <AnimatePresence>
                                    {showNewDraft && (
                                        <motion.div
                                            initial={{ opacity: 0, height: 0 }}
                                            animate={{ opacity: 1, height: 'auto' }}
                                            exit={{ opacity: 0, height: 0 }}
                                            className="glass-card p-5 space-y-4 overflow-hidden"
                                        >
                                            <div>
                                                <div className="flex items-center justify-between mb-2">
                                                    <label className="label-text mb-0">내용</label>
                                                    <span className={`text-xs font-medium ${
                                                        charCount(newContent) > 500
                                                            ? 'text-red-500'
                                                            : 'text-surface-400 dark:text-surface-500'
                                                    }`}>
                                                        <Type className="w-3 h-3 inline mr-1" />
                                                        {charCount(newContent)}자 (공백 제외)
                                                    </span>
                                                </div>
                                                <textarea
                                                    value={newContent}
                                                    onChange={(e) => setNewContent(e.target.value)}
                                                    rows={8}
                                                    className="input-field resize-none"
                                                    placeholder="자소서 내용을 작성하세요..."
                                                />
                                            </div>
                                            <div className="flex gap-3 justify-end">
                                                <button
                                                    onClick={() => { setShowNewDraft(false); setNewContent(''); }}
                                                    className="btn-secondary text-sm"
                                                >
                                                    취소
                                                </button>
                                                <button
                                                    onClick={handleSaveDraft}
                                                    disabled={saving || !newContent.trim()}
                                                    className="btn-primary text-sm flex items-center gap-2"
                                                >
                                                    <Save className="w-4 h-4" />
                                                    {saving ? '저장중...' : '저장'}
                                                </button>
                                            </div>
                                        </motion.div>
                                    )}
                                </AnimatePresence>

                                {/* 기존 자소서 목록 */}
                                {letters.length === 0 && !showNewDraft ? (
                                    <EmptyState
                                        icon={<FileText className="w-8 h-8 text-surface-400" />}
                                        title="작성된 자소서가 없습니다"
                                        description="새 항목 작성을 클릭하여 자소서를 시작하세요"
                                    />
                                ) : (
                                    letters.map((letter, i) => (
                                        <motion.div
                                            key={letter.id}
                                            initial={{ opacity: 0, y: 12 }}
                                            animate={{ opacity: 1, y: 0 }}
                                            transition={{ delay: 0.05 * i }}
                                            className="glass-card p-5"
                                        >
                                            {letter.questionContent && (
                                                <p className="text-sm font-semibold text-brand-600 dark:text-brand-400 mb-2">
                                                    Q. {letter.questionContent}
                                                </p>
                                            )}
                                            <p className="text-sm text-surface-700 dark:text-surface-300 whitespace-pre-wrap leading-relaxed">
                                                {letter.content}
                                            </p>
                                            <div className="flex items-center justify-between mt-4 pt-3 border-t border-surface-200 dark:border-surface-700">
                                                <div className="flex items-center gap-3 text-xs text-surface-400 dark:text-surface-500">
                                                    <span>{charCount(letter.content)}자</span>
                                                    <span>{new Date(letter.updatedAt).toLocaleDateString('ko-KR')}</span>
                                                </div>
                                                <button
                                                    onClick={() => setDeleteTarget(letter.id)}
                                                    className="p-1.5 rounded-lg hover:bg-red-50 dark:hover:bg-red-500/10 transition-colors"
                                                >
                                                    <Trash2 className="w-4 h-4 text-red-500" />
                                                </button>
                                            </div>
                                        </motion.div>
                                    ))
                                )}
                            </div>
                        ) : (
                            <div className="flex items-center justify-center h-64">
                                <p className="text-surface-400 dark:text-surface-500 text-sm">
                                    ← 왼쪽에서 채용공고를 선택하세요
                                </p>
                            </div>
                        )}
                    </div>
                </div>
            )}

            <ConfirmModal
                isOpen={deleteTarget !== null}
                title="자소서 삭제"
                message="이 자소서 항목을 삭제하시겠습니까?"
                confirmLabel="삭제"
                variant="danger"
                onConfirm={handleDelete}
                onCancel={() => setDeleteTarget(null)}
                isLoading={deleting}
            />
        </div>
    );
}
