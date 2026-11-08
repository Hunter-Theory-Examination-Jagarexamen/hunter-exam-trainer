import { useEffect } from "react";
import { useNavigate } from "react-router-dom";

// Logging out is a page of its own: the menu's "Log out" just navigates
// here. A page that asks before leaving (the mock exam, #186) can then stop
// the logout like any other navigation, and the login is only forgotten
// once the student really leaves (#188).
const Logout = () => {
    const navigate = useNavigate();

    useEffect(() => {
        localStorage.removeItem("token");
        // replace: the Back button shouldn't come back to /logout.
        navigate("/login", { replace: true });
    }, [navigate]);

    return null;
};

export default Logout;
