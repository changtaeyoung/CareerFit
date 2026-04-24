import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import {
    PenLine, Trash2, Save,
    Building2, FileText, Type, BarChart3
} from 'lucide-react';
import { getPostings } from '../api/company';
import type { JobPostingResponse } from '../api/company';
import {
    saveCoverLetter, getCoverLettersByPosting, deleteCoverLetter, getQuestionsByPosting,
} from '../api/coverLetter';
import type { CoverLetterResponse, QuestionResponse } from '../api/coverLetter';
import EmptyState from '../components/EmptyState';
import ConfirmModal from '../components/ConfirmModal';
import { toast } from '../store/toastStore';

export default function CoverLetters() {
    const navigate = useNavigate();
    const [postings, setPostings] = useState<JobPostingResponse[]>([]);
    const [selectedPostingId, setSelectedPostingId] = useState<number | null>(null);
    const [questions, setQuestions] = useState<QuestionResponse[]>([]);
    const [letters, setLetters] = useState<CoverLetterResponse[]>([]);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [deleteTarget, setDeleteTarget] = useState<number | null>(null);
    const [deleting, setDeleting] = useState(false);

    // 문항별 답변 입력 상태 (questionId → content)
    const [drafts, setDrafts] = useState<Record<number, string>>({});

    useEffect(() => {
        const fetchPostings = async () => {
            try {
                const data = await getPostings(1, 100);
                setPostings(data.content);
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
            setQuestions([]);
            setLetters([]);
            setDrafts({});
            return;
        }
        setSelectedPostingId(postingId);
        try {
            const [questionsData, lettersData] = await Promise.all([
                getQuestionsByPosting(postingId),
                getCoverLettersByPosting(postingId),
            ]);
            setQuestions(questionsData);
            setLetters(lettersData);

            // 기존 답변을 drafts에 미리 채우기 (수정용)
            const existingDrafts: Record<number, string> = {};
            lettersData.forEach((l) => {
                existingDrafts[l.questionId] = l.content;
            });
            setDrafts(existingDrafts);
        } catch {
            setQuestions([]);
            setLetters([]);
            setDrafts({});
        }
    };

    const handleDraftChange = (questionId: number, content: string) => {
        setDrafts((prev) => ({ ...prev, [questionId]: content }));
    };

    const handleSave = async (questionId: number) => {
        if (!selectedPostingId) return;
        const content = drafts[questionId]?.trim();
        if (!content) {
            toast.error('내용을 입력해주세요');
            return;
        }
        setSaving(true);
        try {
            await saveCoverLetter({
                postingId: selectedPostingId,
                questionId,
                content,
            });
            toast.success('자기소개서가 저장되었습니다');
            const data = await getCoverLettersByPosting(selectedPostingId);
            setLetters(data);
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
            toast.success('자기소개서가 삭제되었습니다');
            const data = await getCoverLettersByPosting(selectedPostingId);
            setLetters(data);
            // drafts에서도 제거
            const deleted = letters.find((l) => l.id === deleteTarget);
            if (deleted) {
                setDrafts((prev) => {
                    const copy = { ...prev };
                    delete copy[deleted.questionId];
                    return copy;
                });
            }
        } catch {
            toast.error('삭제에 실패했습니다');
        } finally {
            setDeleting(false);
            setDeleteTarget(null);
        }
    };

    const charCount = (text: string) => (text || '').replace(/\s/g, '').length;

    // 특정 문항에 이미 저장된 답변이 있는지
    const getLetterForQuestion = (questionId: number) =>
        letters.find((l) => l.questionId === questionId);

    return (
        <div className="space-y-6">
            <div>
                <h1 className="section-title">자기소개서 관리</h1>
                <p className="section-subtitle mt-1">채용공고별 문항에 맞춰 자기소개서를 작성하세요</p>
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
                    description="채용공고가 등록되면 자기소개서를 작성할 수 있습니다"
                    action={
                        <button onClick={() => navigate('/postings')} className="btn-primary text-sm">
                            채용공고 보러가기
                        </button>
                    }
                />
            ) : (
                <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                    {/* 왼쪽: 공고 목록 */}
                    <div className="space-y-2 h-[calc(100vh-14rem)] overflow-y-auto pr-2">
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

                    {/* 오른쪽: 문항 + 자기소개서 작성 */}
                    <div className="lg:col-span-2 h-[calc(100vh-14rem)] overflow-y-auto pr-2">
                        {selectedPostingId ? (
                            <div className="space-y-4">
                                <div className="flex items-center justify-between">
                                    <h2 className="font-semibold text-surface-800 dark:text-surface-200">
                                        자기소개서 문항 ({questions.length})
                                    </h2>
                                    <button
                                        onClick={() => navigate(`/postings/${selectedPostingId}`)}
                                        className="flex items-center gap-1.5 text-sm font-medium text-accent-600 dark:text-accent-400 hover:text-accent-700 transition-colors"
                                    >
                                        <BarChart3 className="w-4 h-4" />
                                        핏 분석 보기
                                    </button>
                                </div>

                                {questions.length === 0 ? (
                                    <EmptyState
                                        icon={<FileText className="w-8 h-8 text-surface-400" />}
                                        title="등록된 문항이 없습니다"
                                        description="관리자가 문항을 등록하면 자기소개서를 작성할 수 있습니다"
                                    />
                                ) : (
                                    <AnimatePresence>
                                        {questions.map((q, i) => {
                                            const existing = getLetterForQuestion(q.id);
                                            const draft = drafts[q.id] || '';
                                            const isSaved = existing != null;
                                            const isModified = isSaved && draft !== existing.content;

                                            return (
                                                <motion.div
                                                    key={q.id}
                                                    initial={{ opacity: 0, y: 12 }}
                                                    animate={{ opacity: 1, y: 0 }}
                                                    transition={{ delay: 0.05 * i }}
                                                    className="glass-card p-5 space-y-3"
                                                >
                                                    {/* 문항 제목 */}
                                                    <div className="flex items-start justify-between">
                                                        <p className="text-sm font-semibold text-brand-600 dark:text-brand-400">
                                                            Q{q.sortOrder}. {q.question}
                                                        </p>
                                                        {isSaved && (
                                                            <span className="text-xs px-2 py-0.5 rounded-full bg-green-100 dark:bg-green-500/20 text-green-700 dark:text-green-400 whitespace-nowrap ml-3">
                                                                저장됨
                                                            </span>
                                                        )}
                                                    </div>

                                                    {/* 답변 입력 */}
                                                    <div>
                                                        <div className="flex items-center justify-end mb-1">
                                                            <span className={`text-xs font-medium ${
                                                                charCount(draft) > 500
                                                                    ? 'text-red-500'
                                                                    : 'text-surface-400 dark:text-surface-500'
                                                            }`}>
                                                                <Type className="w-3 h-3 inline mr-1" />
                                                                {charCount(draft)}자 (공백 제외)
                                                            </span>
                                                        </div>
                                                        <textarea
                                                            value={draft}
                                                            onChange={(e) => handleDraftChange(q.id, e.target.value)}
                                                            rows={6}
                                                            className="input-field resize-none"
                                                            placeholder="자기소개서 내용을 작성하세요..."
                                                        />
                                                    </div>

                                                    {/* 액션 버튼 */}
                                                    <div className="flex items-center justify-between pt-1">
                                                        <div>
                                                            {isSaved && (
                                                                <span className="text-xs text-surface-400">
                                                                    마지막 저장: {new Date(existing!.updatedAt).toLocaleDateString('ko-KR')}
                                                                </span>
                                                            )}
                                                        </div>
                                                        <div className="flex gap-2">
                                                            {isSaved && (
                                                                <button
                                                                    onClick={() => setDeleteTarget(existing!.id)}
                                                                    className="p-2 rounded-lg hover:bg-red-50 dark:hover:bg-red-500/10 transition-colors"
                                                                    title="삭제"
                                                                >
                                                                    <Trash2 className="w-4 h-4 text-red-500" />
                                                                </button>
                                                            )}
                                                            <button
                                                                onClick={() => handleSave(q.id)}
                                                                disabled={saving || !draft.trim() || (isSaved && !isModified)}
                                                                className="btn-primary text-sm flex items-center gap-1.5 disabled:opacity-50"
                                                            >
                                                                <Save className="w-4 h-4" />
                                                                {saving ? '저장중...' : isSaved ? '수정' : '저장'}
                                                            </button>
                                                        </div>
                                                    </div>
                                                </motion.div>
                                            );
                                        })}
                                    </AnimatePresence>
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
                title="자기소개서 삭제"
                message="이 자기소개서 항목을 삭제하시겠습니까?"
                confirmLabel="삭제"
                variant="danger"
                onConfirm={handleDelete}
                onCancel={() => setDeleteTarget(null)}
                isLoading={deleting}
            />
        </div>
    );
}
