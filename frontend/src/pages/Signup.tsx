import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Mail, Lock, User, ArrowRight, AlertCircle, CheckCircle2, Sun, Moon } from 'lucide-react';
import { signup } from '../api/auth';
import { useAuthStore } from '../store/authStore';
import { useThemeStore } from '../store/themeStore';
import AnimatedBackground from '../components/AnimatedBackground';

export default function Signup() {
    const navigate = useNavigate();
    const setAuth = useAuthStore((state) => state.setAuth);
    const { theme, toggleTheme } = useThemeStore();

    const [name, setName] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [passwordConfirm, setPasswordConfirm] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState('');

    const passwordMatch = password && passwordConfirm && password === passwordConfirm;
    const passwordMismatch = password && passwordConfirm && password !== passwordConfirm;

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        if (password !== passwordConfirm) {
            setError('비밀번호가 일치하지 않습니다');
            return;
        }

        setError('');
        setIsLoading(true);

        try {
            const response = await signup({ email, password, passwordConfirm, name });
            setAuth(
                { userId: response.userId, email: response.email, name: response.name },
                response.accessToken,
                response.refreshToken
            );
            navigate('/dashboard');
        } catch (err: any) {
            setError(err.response?.data?.message || '회원가입에 실패했습니다');
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="min-h-screen bg-gradient-to-br from-surface-50 via-brand-50 to-accent-50
                         dark:from-surface-950 dark:via-surface-900 dark:to-surface-900
                         flex items-center justify-center p-4 relative overflow-hidden transition-colors duration-500">
            <AnimatedBackground />

            {/* 다크/라이트 모드 토글 */}
            <button
                onClick={toggleTheme}
                className="absolute top-4 right-4 z-20 p-2.5 rounded-xl
                           bg-white/60 dark:bg-surface-800/60 backdrop-blur-lg
                           border border-surface-200/60 dark:border-surface-700/60
                           hover:bg-white/80 dark:hover:bg-surface-700/80
                           text-surface-600 dark:text-surface-300
                           transition-all duration-200 shadow-sm"
            >
                {theme === 'light' ? <Moon className="w-5 h-5" /> : <Sun className="w-5 h-5" />}
            </button>

            <motion.div
                initial={{ opacity: 0, y: 30 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.6, ease: 'easeOut' }}
                className="relative w-full max-w-md z-10"
            >
                <div className="glass-card p-10">
                    <div className="text-center mb-8">
                        <motion.div
                            initial={{ scale: 0, rotate: -180 }}
                            animate={{ scale: 1, rotate: 0 }}
                            transition={{ delay: 0.2, type: 'spring', stiffness: 150 }}
                            className="inline-block mb-6"
                        >
                            <div className="w-20 h-20 rounded-3xl flex items-center justify-center shadow-lg shadow-brand-500/30 overflow-hidden bg-white dark:bg-surface-800">
                                <img src="/logo.png" alt="CareerFit" className="w-16 h-16 object-contain" />
                            </div>
                        </motion.div>
                        <h1 className="text-3xl font-bold text-surface-900 dark:text-surface-50 mb-2 tracking-tight">
                            시작하기
                        </h1>
                        <p className="text-surface-500 dark:text-surface-400 text-sm">
                            CareerFit과 함께 커리어를 설계하세요
                        </p>
                    </div>

                    {error && (
                        <motion.div
                            initial={{ opacity: 0, y: -10 }}
                            animate={{ opacity: 1, y: 0 }}
                            className="mb-6 p-4 bg-red-50 dark:bg-red-500/10 border border-red-200 dark:border-red-500/20 rounded-xl flex items-center gap-2"
                        >
                            <AlertCircle className="w-5 h-5 text-red-500 flex-shrink-0" />
                            <p className="text-sm text-red-700 dark:text-red-400">{error}</p>
                        </motion.div>
                    )}

                    <form onSubmit={handleSubmit} className="space-y-4">
                        <div>
                            <label className="label-text">이름</label>
                            <div className="relative group">
                                <User className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-surface-400 group-focus-within:text-brand-600 dark:group-focus-within:text-brand-400 transition-colors" />
                                <input
                                    id="signup-name"
                                    type="text"
                                    value={name}
                                    onChange={(e) => setName(e.target.value)}
                                    placeholder="홍길동"
                                    className="input-field pl-12"
                                    required
                                />
                            </div>
                        </div>

                        <div>
                            <label className="label-text">이메일</label>
                            <div className="relative group">
                                <Mail className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-surface-400 group-focus-within:text-brand-600 dark:group-focus-within:text-brand-400 transition-colors" />
                                <input
                                    id="signup-email"
                                    type="email"
                                    value={email}
                                    onChange={(e) => setEmail(e.target.value)}
                                    placeholder="example@email.com"
                                    className="input-field pl-12"
                                    required
                                />
                            </div>
                        </div>

                        <div>
                            <label className="label-text">비밀번호</label>
                            <div className="relative group">
                                <Lock className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-surface-400 group-focus-within:text-brand-600 dark:group-focus-within:text-brand-400 transition-colors" />
                                <input
                                    id="signup-password"
                                    type="password"
                                    value={password}
                                    onChange={(e) => setPassword(e.target.value)}
                                    placeholder="영문+숫자+특수문자 8자 이상"
                                    minLength={8}
                                    className="input-field pl-12"
                                    required
                                />
                            </div>
                        </div>

                        <div>
                            <label className="label-text">비밀번호 확인</label>
                            <div className="relative group">
                                <Lock className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-surface-400 group-focus-within:text-brand-600 dark:group-focus-within:text-brand-400 transition-colors" />
                                <input
                                    id="signup-password-confirm"
                                    type="password"
                                    value={passwordConfirm}
                                    onChange={(e) => setPasswordConfirm(e.target.value)}
                                    placeholder="비밀번호 재입력"
                                    className={`input-field pl-12 pr-12 ${
                                        passwordMismatch
                                            ? 'border-red-300 dark:border-red-500/50 focus:ring-red-500/20 focus:border-red-500'
                                            : passwordMatch
                                                ? 'border-accent-300 dark:border-accent-500/50 focus:ring-accent-500/20 focus:border-accent-500'
                                                : ''
                                    }`}
                                    required
                                />
                                {passwordMatch && (
                                    <motion.div
                                        initial={{ scale: 0 }}
                                        animate={{ scale: 1 }}
                                        className="absolute right-4 top-1/2 -translate-y-1/2"
                                    >
                                        <CheckCircle2 className="w-5 h-5 text-accent-500" />
                                    </motion.div>
                                )}
                            </div>
                            {passwordMismatch && (
                                <p className="text-xs text-red-600 dark:text-red-400 mt-1.5 ml-1">
                                    비밀번호가 일치하지 않습니다
                                </p>
                            )}
                        </div>

                        <motion.button
                            id="signup-submit"
                            type="submit"
                            disabled={isLoading}
                            whileHover={{ scale: 1.01 }}
                            whileTap={{ scale: 0.99 }}
                            className="w-full btn-primary flex items-center justify-center gap-2 group mt-6"
                        >
                            {isLoading ? (
                                <motion.div
                                    animate={{ rotate: 360 }}
                                    transition={{ duration: 1, repeat: Infinity, ease: 'linear' }}
                                    className="w-5 h-5 border-2 border-white border-t-transparent rounded-full"
                                />
                            ) : (
                                <>
                                    회원가입
                                    <ArrowRight className="w-5 h-5 group-hover:translate-x-1 transition-transform" />
                                </>
                            )}
                        </motion.button>
                    </form>

                    <div className="mt-6 text-center">
                        <p className="text-surface-500 dark:text-surface-400 text-sm">
                            이미 계정이 있으신가요?{' '}
                            <Link
                                to="/login"
                                className="text-brand-600 dark:text-brand-400 hover:text-brand-700 dark:hover:text-brand-300 font-semibold transition-colors"
                            >
                                로그인
                            </Link>
                        </p>
                    </div>
                </div>
            </motion.div>
        </div>
    );
}