import { useEffect } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";

// Logging out is a page of its own: the menu's "Log out" just navigates
// here. A page that asks before leaving (the mock exam, #186) can then stop
// the logout like any other navigation, and the login is only forgotten
// once the student really leaves (#188).
const Logout = () => {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();

    useEffect(() => {
        localStorage.removeItem("token");
        const target = searchParams.has("deleted") ? "/login?deleted=1" : "/login";
        navigate(target, { replace: true });
    }, [navigate, searchParams]);

    return null;
};

export default Logout;
