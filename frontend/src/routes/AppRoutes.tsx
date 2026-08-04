import {BrowserRouter, Navigate, Route, Routes} from "react-router-dom";
import Login from "../pages/Login/Login.tsx";
import Dashboard from "../pages/Dashboard/Dashboard.tsx";
import Layout from "../components/layout/Layout.tsx";
import Practice from "../pages/Practice/Practice.tsx";
import MockExam from "../pages/MockExam/MockExam.tsx";
import Profile from "../pages/Profile/Profile.tsx";
import Statistics from "../pages/Statistics/Statistics.tsx";

const AppRoutes = () => {
    return (
        <BrowserRouter>
            <Routes>

                <Route path="/login" element={ <Login /> } />

                <Route element={ <Layout /> }>
                    <Route path="/dashboard" element={<Dashboard />} />
                    <Route path="/practice" element={<Practice />} />
                    <Route path="/mockexam" element={<MockExam />} />
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