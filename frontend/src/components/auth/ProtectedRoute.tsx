import { Navigate, Outlet } from "react-router-dom";
import { getTokenStatus } from "../../utils/authToken";

const ProtectedRoute = () => {

    const status = getTokenStatus();

    if (status === "missing") {
        return <Navigate to="/login" replace />
    }

    // Checked here, before any protected page is shown, so an old login
    // doesn't have to wait for the backend to reject it.
    // The old token is not removed here: a component must not change things
    // while rendering (React may render it twice, and the second render would
    // then see no token and drop "?expired=1"). It is harmless: auth requests
    // don't send it, and the next login replaces it.
    if (status !== "valid") {
        return <Navigate to={status === "expired" ? "/login?expired=1" : "/login"} replace />
    }

    return <Outlet />;
};

export default ProtectedRoute;
