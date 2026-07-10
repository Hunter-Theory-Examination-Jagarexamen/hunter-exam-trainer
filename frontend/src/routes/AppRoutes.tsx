import {BrowserRouter, Route, Routes} from "react-router-dom";
import Login from "../pages/Login/Login.tsx";
import Dashboard from "../pages/Dashboard/Dashboard.tsx";
import Layout from "../components/layout/Layout.tsx";

const AppRoutes = () => {
    return (
        <BrowserRouter>
            <Routes>

                <Route
                    path="/"
                    element={
                        <Login />
                    }
                />

                <Route
                    path="/dashboard"
                    element={
                        <Layout>
                            <Dashboard />
                        </Layout>
                    }
                />

            </Routes>
        </BrowserRouter>
    );
};

export default AppRoutes;