import {Navigate, Outlet, useOutletContext} from "react-router-dom";
import {useTranslation} from "react-i18next";
import {UserRole, type CurrentUserState} from "../../types/user";

const AdminRoute = () => {
    const { t } = useTranslation();
    const {user, isLoading, error} = useOutletContext<CurrentUserState>();

    if (isLoading) return <p role="status">{t("common.loadingUserDetails")}</p>;
    if (error) return <p role="alert">{error}</p>;
    if (user?.role !== UserRole.ADMIN) return <Navigate to="/dashboard" replace />;

    // Forward the current user to admin sub-pages (LearnerDetailPage needs it
    // to detect self-demotion); react-router's Outlet does not chain contexts.
    return <Outlet context={{user, isLoading, error}} />;
};

export default AdminRoute;
