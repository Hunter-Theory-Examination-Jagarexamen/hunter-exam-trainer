import {Navigate, Outlet, useOutletContext} from "react-router-dom";
import {UserRole, type CurrentUserState} from "../../types/user";

const AdminRoute = () => {
    const {user, isLoading, error} = useOutletContext<CurrentUserState>();

    if (isLoading) return <p role="status">Loading user details...</p>;
    if (error) return <p role="alert">{error}</p>;
    if (user?.role !== UserRole.ADMIN) return <Navigate to="/dashboard" replace />;

    return <Outlet />;
};

export default AdminRoute;
