import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { useAuthStore } from './store/authStore';

// 페이지
import Login from './pages/Login';
import Signup from './pages/Signup';
import Dashboard from './pages/Dashboard';
import Companies from './pages/Companies';
import CompanyDetail from './pages/CompanyDetail';
import Postings from './pages/Postings';
import PostingDetail from './pages/PostingDetail';
import AnalysisHistory from './pages/AnalysisHistory';
import AnalysisReport from './pages/AnalysisReport';
import Spec from './pages/Spec';
import CoverLetters from './pages/CoverLetters';
import MyPage from './pages/MyPage';

// 레이아웃 & 공통
import Layout from './components/Layout';
import ToastContainer from './components/Toast';

// 로그인 필수 라우트 가드
function PrivateRoute({ children }: { children: React.ReactNode }) {
    const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
    return isAuthenticated ? <>{children}</> : <Navigate to="/login" />;
}

// 인증 페이지 가드 (로그인 상태면 대시보드로)
function PublicRoute({ children }: { children: React.ReactNode }) {
    const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
    return isAuthenticated ? <Navigate to="/dashboard" /> : <>{children}</>;
}

function App() {
    return (
        <BrowserRouter>
            <ToastContainer />
            <Routes>
                {/* 인증 페이지 (레이아웃 없음) */}
                <Route path="/login" element={<PublicRoute><Login /></PublicRoute>} />
                <Route path="/signup" element={<PublicRoute><Signup /></PublicRoute>} />

                {/* 앱 페이지 (사이드바 레이아웃) */}
                <Route
                    path="/dashboard"
                    element={
                        <PrivateRoute>
                            <Layout><Dashboard /></Layout>
                        </PrivateRoute>
                    }
                />
                <Route
                    path="/companies"
                    element={
                        <PrivateRoute>
                            <Layout><Companies /></Layout>
                        </PrivateRoute>
                    }
                />
                <Route
                    path="/companies/:id"
                    element={
                        <PrivateRoute>
                            <Layout><CompanyDetail /></Layout>
                        </PrivateRoute>
                    }
                />
                <Route
                    path="/postings"
                    element={
                        <PrivateRoute>
                            <Layout><Postings /></Layout>
                        </PrivateRoute>
                    }
                />
                <Route
                    path="/postings/:id"
                    element={
                        <PrivateRoute>
                            <Layout><PostingDetail /></Layout>
                        </PrivateRoute>
                    }
                />
                <Route
                    path="/analysis/history"
                    element={
                        <PrivateRoute>
                            <Layout><AnalysisHistory /></Layout>
                        </PrivateRoute>
                    }
                />
                <Route
                    path="/analysis/:reportId"
                    element={
                        <PrivateRoute>
                            <Layout><AnalysisReport /></Layout>
                        </PrivateRoute>
                    }
                />
                <Route
                    path="/spec"
                    element={
                        <PrivateRoute>
                            <Layout><Spec /></Layout>
                        </PrivateRoute>
                    }
                />
                <Route
                    path="/cover-letters"
                    element={
                        <PrivateRoute>
                            <Layout><CoverLetters /></Layout>
                        </PrivateRoute>
                    }
                />
                <Route
                    path="/mypage"
                    element={
                        <PrivateRoute>
                            <Layout><MyPage /></Layout>
                        </PrivateRoute>
                    }
                />

                {/* 기본 리다이렉트 */}
                <Route path="/" element={<Navigate to="/dashboard" />} />
                <Route path="*" element={<Navigate to="/dashboard" />} />
            </Routes>
        </BrowserRouter>
    );
}

export default App;