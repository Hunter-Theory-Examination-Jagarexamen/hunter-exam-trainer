import NotFound from "../pages/NotFound/NotFound"
import {createRoutesFromElements, Navigate, Route} from "react-router-dom";
import Login from "../pages/Login/Login";
import Dashboard from "../pages/Dashboard/Dashboard";
import Layout from "../components/layout/Layout";
import Practice from "../pages/Practice/Practice";
import MockExam from "../pages/MockExam/MockExam";
import Profile from "../pages/Profile/Profile";
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
import LearnerProgressPage from "../pages/Admin/LearnerProgressPage";
import Subjects from "../pages/Admin/Subjects";
import AdminHome from "../pages/Admin/AdminHome";
import RootLayout from "../components/layout/RootLayout";
import LazyStatistics from "../pages/Statistics/LazyStatistics";

// The app's route tree. Kept in its own file so tests can use the same routes
// with a memory router (see __tests__/routes/AppRoutes.test.tsx).
export const routes = createRoutesFromElements(
        <Route element={<RootLayout />}>

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

                        <Route path="/statistics" element={<LazyStatistics />} />

                        <Route path="/profile" element={<Profile />} />

                        <Route element={<AdminRoute />}>
                            <Route path="/admin" element={<AdminHome />} />
                            <Route path="/admin/subjects" element={<Subjects />} />
                            <Route path="/admin/questions" element={<QuestionBank />} />
                            <Route path="/admin/learners" element={<LearnerProgressPage />} />
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
                    element={<NotFound />}
                />

        </Route>
);
