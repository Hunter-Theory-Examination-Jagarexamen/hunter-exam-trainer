import { Navigate, Outlet } from "react-router-dom";
import { getTokenStatus } from "../../utils/authToken";

const ProtectedRoute = () => {

    const status = getTokenStatus();

    if (status === "missing") {
        return <Navigate to="/login" replace />
    }

    // Checked here, before any protected page is shown, so an old login
    // doesn't have to wait for the backend to reject it.
    if (status !== "valid") {
        localStorage.removeItem("token");
        return <Navigate to={status === "expired" ? "/login?expired=1" : "/login"} replace />
    }

    return <Outlet />;
};

export default ProtectedRoute;
