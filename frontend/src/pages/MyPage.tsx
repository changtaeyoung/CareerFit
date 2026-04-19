import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import {
    Mail, Lock, Pencil, Check, X,
    Sun, Moon, Shield
} from 'lucide-react';
import { useAuthStore } from '../store/authStore';
import { useThemeStore } from '../store/themeStore';
import { changePassword, updateName, deleteUser } from '../api/user';
import type { PasswordChangeRequest } from '../api/user';
import ConfirmModal from '../components/ConfirmModal';

export default function MyPage() {
    const { user, setAuth, logout } = useAuthStore();
    const { theme, toggleTheme } = useThemeStore();
    const navigate = useNavigate();

    // 이름 수정
    const [editingName, setEditingName] = useState(false);
    const [newName, setNewName] = useState(user?.name || '');
    const [nameSaving, setNameSaving] = useState(false);

    // 비밀번호 변경
    const [showPwForm, setShowPwForm] = useState(false);
    const [currentPw, setCurrentPw] = useState('');
    const [newPw, setNewPw] = useState('');
    const [newPwConfirm, setNewPwConfirm] = useState('');
    const [pwSaving, setPwSaving] = useState(false);
    const [pwMessage, setPwMessage] = useState({ type: '', text: '' });

    // 회원 탈퇴
    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [deleteLoading, setDeleteLoading] = useState(false);

    const handleNameSave = async () => {
        if (!newName.trim() || !user) return;
        setNameSaving(true);
        try {
            await updateName(newName.trim());
            // 로컬 상태 업데이트
            setAuth(
                { ...user, name: newName.trim() },
                localStorage.getItem('accessToken') || '',
                localStorage.getItem('refreshToken') || ''
            );
            setEditingName(false);
        } catch (err: any) {
            alert(err.response?.data?.message || '이름 수정에 실패했습니다');
        } finally {
            setNameSaving(false);
        }
    };

    const handlePasswordChange = async (e: React.FormEvent) => {
        e.preventDefault();
        if (newPw !== newPwConfirm) {
            setPwMessage({ type: 'error', text: '새 비밀번호가 일치하지 않습니다' });
            return;
        }
        setPwSaving(true);
        setPwMessage({ type: '', text: '' });
        try {
            const data: PasswordChangeRequest = {
                currentPassword: currentPw,
                newPassword: newPw,
                newPasswordConfirm: newPwConfirm,
            };
            await changePassword(data);
            setPwMessage({ type: 'success', text: '비밀번호가 변경되었습니다' });
            setShowPwForm(false);
            setCurrentPw(''); setNewPw(''); setNewPwConfirm('');
        } catch (err: any) {
            setPwMessage({ type: 'error', text: err.response?.data?.message || '비밀번호 변경에 실패했습니다' });
        } finally {
            setPwSaving(false);
        }
    };

    const handleDeleteAccount = async () => {
        setDeleteLoading(true);
        try {
            await deleteUser();
            logout();
            navigate('/login');
        } catch (err: any) {
            alert(err.response?.data?.message || '회원 탈퇴에 실패했습니다');
        } finally {
            setDeleteLoading(false);
            setShowDeleteModal(false);
        }
    };

    const userInitial = user?.name?.charAt(0) || 'U';

    return (
        <div className="space-y-6 max-w-2xl">
            <div>
                <h1 className="section-title">마이페이지</h1>
                <p className="section-subtitle mt-1">프로필 정보를 관리하세요</p>
            </div>

            {/* 프로필 카드 */}
            <motion.div
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                className="glass-card p-8"
            >
                <div className="flex items-center gap-5">
                    <div className="w-20 h-20 bg-gradient-to-br from-brand-500 to-accent-500 rounded-2xl
                                    flex items-center justify-center text-white text-2xl font-bold shadow-lg shadow-brand-500/20">
                        {userInitial}
                    </div>
                    <div className="flex-1">
                        {editingName ? (
                            <div className="flex items-center gap-2">
                                <input
                                    value={newName}
                                    onChange={(e) => setNewName(e.target.value)}
                                    className="input-field text-lg font-bold py-2"
                                    autoFocus
                                />
                                <button
                                    onClick={handleNameSave}
                                    disabled={nameSaving}
                                    className="p-2 rounded-lg bg-accent-100 dark:bg-accent-500/15 hover:bg-accent-200 dark:hover:bg-accent-500/25 transition-colors"
                                >
                                    <Check className="w-5 h-5 text-accent-600 dark:text-accent-400" />
                                </button>
                                <button
                                    onClick={() => { setEditingName(false); setNewName(user?.name || ''); }}
                                    className="p-2 rounded-lg hover:bg-surface-100 dark:hover:bg-surface-700 transition-colors"
                                >
                                    <X className="w-5 h-5 text-surface-400" />
                                </button>
                            </div>
                        ) : (
                            <div className="flex items-center gap-2">
                                <h2 className="text-2xl font-bold text-surface-900 dark:text-surface-50">
                                    {user?.name}
                                </h2>
                                <button
                                    onClick={() => setEditingName(true)}
                                    className="p-1.5 rounded-lg hover:bg-surface-100 dark:hover:bg-surface-700 transition-colors"
                                >
                                    <Pencil className="w-4 h-4 text-surface-400" />
                                </button>
                            </div>
                        )}
                        <p className="text-surface-500 dark:text-surface-400 flex items-center gap-1.5 mt-1">
                            <Mail className="w-4 h-4" />
                            {user?.email}
                        </p>
                    </div>
                </div>
            </motion.div>

            {/* 설정 섹션 */}
            <motion.div
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: 0.1 }}
                className="glass-card divide-y divide-surface-200 dark:divide-surface-700"
            >
                {/* 다크모드 설정 */}
                <div className="p-5 flex items-center justify-between">
                    <div className="flex items-center gap-3">
                        {theme === 'dark' ? (
                            <Moon className="w-5 h-5 text-brand-500" />
                        ) : (
                            <Sun className="w-5 h-5 text-amber-500" />
                        )}
                        <div>
                            <p className="font-medium text-surface-800 dark:text-surface-200">
                                {theme === 'dark' ? '다크 모드' : '라이트 모드'}
                            </p>
                            <p className="text-xs text-surface-500 dark:text-surface-400">화면 테마를 변경합니다</p>
                        </div>
                    </div>
                    <button
                        onClick={toggleTheme}
                        className={`relative w-12 h-7 rounded-full transition-colors duration-300 ${
                            theme === 'dark' ? 'bg-brand-500' : 'bg-surface-300'
                        }`}
                    >
                        <div className={`absolute top-0.5 w-6 h-6 bg-white rounded-full shadow-md transition-transform duration-300 ${
                            theme === 'dark' ? 'translate-x-5.5 left-0' : 'left-0.5'
                        }`} />
                    </button>
                </div>

                {/* 비밀번호 변경 */}
                <div className="p-5">
                    <div className="flex items-center justify-between">
                        <div className="flex items-center gap-3">
                            <Lock className="w-5 h-5 text-surface-500" />
                            <div>
                                <p className="font-medium text-surface-800 dark:text-surface-200">비밀번호 변경</p>
                                <p className="text-xs text-surface-500 dark:text-surface-400">보안을 위해 주기적으로 변경하세요</p>
                            </div>
                        </div>
                        {!showPwForm && (
                            <button onClick={() => setShowPwForm(true)} className="btn-ghost text-sm">
                                변경
                            </button>
                        )}
                    </div>

                    {pwMessage.text && (
                        <div className={`mt-3 p-3 rounded-xl text-sm ${
                            pwMessage.type === 'success'
                                ? 'bg-accent-50 dark:bg-accent-500/10 text-accent-700 dark:text-accent-400'
                                : 'bg-red-50 dark:bg-red-500/10 text-red-700 dark:text-red-400'
                        }`}>
                            {pwMessage.text}
                        </div>
                    )}

                    {showPwForm && (
                        <motion.form
                            initial={{ opacity: 0, height: 0 }}
                            animate={{ opacity: 1, height: 'auto' }}
                            exit={{ opacity: 0, height: 0 }}
                            onSubmit={handlePasswordChange}
                            className="mt-4 space-y-3"
                        >
                            <input
                                type="password"
                                value={currentPw}
                                onChange={e => setCurrentPw(e.target.value)}
                                placeholder="현재 비밀번호"
                                className="input-field"
                                required
                            />
                            <input
                                type="password"
                                value={newPw}
                                onChange={e => setNewPw(e.target.value)}
                                placeholder="새 비밀번호"
                                className="input-field"
                                minLength={8}
                                required
                            />
                            <input
                                type="password"
                                value={newPwConfirm}
                                onChange={e => setNewPwConfirm(e.target.value)}
                                placeholder="새 비밀번호 확인"
                                className="input-field"
                                required
                            />
                            <div className="flex gap-3 justify-end">
                                <button type="button" onClick={() => { setShowPwForm(false); setPwMessage({ type: '', text: '' }); }} className="btn-secondary text-sm">
                                    취소
                                </button>
                                <button type="submit" disabled={pwSaving} className="btn-primary text-sm">
                                    {pwSaving ? '변경중...' : '변경하기'}
                                </button>
                            </div>
                        </motion.form>
                    )}
                </div>

                {/* 회원 탈퇴 */}
                <div className="p-5">
                    <div className="flex items-center justify-between">
                        <div className="flex items-center gap-3">
                            <Shield className="w-5 h-5 text-red-500" />
                            <div>
                                <p className="font-medium text-red-600 dark:text-red-400">회원 탈퇴</p>
                                <p className="text-xs text-surface-500 dark:text-surface-400">모든 데이터가 삭제됩니다</p>
                            </div>
                        </div>
                        <button
                            onClick={() => setShowDeleteModal(true)}
                            className="text-sm text-red-600 dark:text-red-400 font-medium hover:underline"
                        >
                            탈퇴하기
                        </button>
                    </div>
                </div>
            </motion.div>

            <ConfirmModal
                isOpen={showDeleteModal}
                title="회원 탈퇴"
                message="정말 탈퇴하시겠습니까? 모든 스펙, 분석 결과, 자소서 데이터가 영구적으로 삭제됩니다. 이 작업은 되돌릴 수 없습니다."
                confirmLabel="탈퇴하기"
                variant="danger"
                onConfirm={handleDeleteAccount}
                onCancel={() => setShowDeleteModal(false)}
                isLoading={deleteLoading}
            />
        </div>
    );
}
