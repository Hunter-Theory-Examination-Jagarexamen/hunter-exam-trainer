import {BrowserRouter, Navigate, Route, Routes} from "react-router-dom";
import Login from "../pages/Login/Login";
import Dashboard from "../pages/Dashboard/Dashboard";
import Layout from "../components/layout/Layout";
import Practice from "../pages/Practice/Practice";
import MockExam from "../pages/MockExam/MockExam";
import Profile from "../pages/Profile/Profile";
import Statistics from "../pages/Statistics/Statistics";
import Result from "../pages/Results/Result";
import Exam from "../pages/Exam/Exam";
import PracticeSubjects from "../pages/Practice/PracticeSubjects";
import RandomPractice from "../pages/Practice/RandomPractice";
import PracticeQuestions from "../pages/Practice/PracticeQuestions";
import Register from "../pages/Register/Register";
import ResetPassword from "../pages/ResetPassword/ResetPassword";
import ForgotPassword from "../pages/ForgotPassword/ForgotPassword";
import OAuth2Redirect from "../pages/OAuth2Redirect/OAuth2Redirect";
import ProtectedRoute from "../components/auth/ProtectedRoute";
import AdminRoute from "../components/auth/AdminRoute";
import QuestionBank from "../pages/Admin/QuestionBank";

const AppRoutes = () => {
    return (
        <BrowserRouter>
            <Routes>

                {/* Public Routes */}
                <Route path="/login" element={ <Login /> } />
                <Route path="/register" element={ <Register /> } />
                <Route path="/reset-password" element={ <ResetPassword /> } />
                <Route path="/forgot-password" element={ <ForgotPassword /> } />
                <Route path="/oauth2/redirect" element={ <OAuth2Redirect /> } />

                {/* Protected Routes */}
                <Route element={<ProtectedRoute />} >

                    <Route element={ <Layout /> }>

                        <Route path="/dashboard" element={<Dashboard />} />

                        <Route path="/practice" element={<Practice />} />
                        <Route path="/practice/subjects" element={<PracticeSubjects />} />
                        <Route path="/practice/subjects/:subject" element={<PracticeQuestions />} />
                        <Route path="/practice/random" element={<RandomPractice />} />

                        <Route path="/mockexam" element={<MockExam />} />
                        <Route path="/exam" element={<Exam />} />
                        <Route path="/result" element={<Result />} />

                        <Route path="/statistics" element={<Statistics />} />

                        <Route path="/profile" element={<Profile />} />

                        <Route element={<AdminRoute />}>
                            <Route path="/admin/questions" element={<QuestionBank />} />
                        </Route>

                    </Route>
                </Route>

                {/* Default route */}
                <Route
                    path="/"
                    element={<Navigate to="/dashboard" replace /> }
                />

                {/* Unknown route */}
                <Route
                    path="*"
                    element={<Navigate to="/dashboard" replace /> }
                />

            </Routes>
        </BrowserRouter>
    );
};

export default AppRoutes;
