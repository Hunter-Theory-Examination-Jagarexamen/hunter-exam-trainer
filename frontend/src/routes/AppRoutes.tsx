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
import PracticeSubjects from "../pages/Practice/PracticeSubjects.tsx";
import RandomPractice from "../pages/Practice/RandomPractice.tsx";
import PracticeQuestions from "../pages/Practice/PracticeQuestions.tsx";
import Register from "../pages/Register/Register.tsx";

const AppRoutes = () => {
    return (
        <BrowserRouter>
            <Routes>

                <Route path="/login" element={ <Login /> } />
                <Route path="/register" element={ <Register /> } />

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

                </Route>

                <Route
                    path="/"
                    element={<Navigate to="/dashboard" replace /> }
                />

                <Route
                    path="* "
                    element={<Navigate to="/dashboard" replace /> }
                />

            </Routes>
        </BrowserRouter>
    );
};

export default AppRoutes;