import "../../styles/sidebar.css"
import {
    LayoutDashboard,
    BookOpen,
    ClipboardCheck,
    ChartColumnIncreasing,
    LogOut,
    User,
    ShieldCheck,
    Users,
} from "lucide-react";
import {NavLink, useLocation, useNavigate} from "react-router-dom";
import {UserRole, type User as CurrentUser} from "../../types/user";

const Sidebar = ({ user }: { user: CurrentUser | null }) => {

    const location = useLocation();
    const navigate = useNavigate();

    const handleLogout = () => {

        localStorage.removeItem("token");
        navigate("/login");
    }

    return (

        <aside className="sidebar">
            <nav className="sidebar-nav">
                <NavLink to="/dashboard">
                    <LayoutDashboard size={20} />
                    Dashboard
                </NavLink>

                <NavLink to="/practice">
                    <BookOpen size={20} />
                    Practice
                </NavLink>

                <NavLink
                    to="/mockexam"
                    className={({ isActive }) =>
                        isActive ||
                        location.pathname === "/exam" ||
                        location.pathname === "/result"
                            ? "active"
                            : ""
                    }
                >
                    <ClipboardCheck size={20} />
                    Mock Exam
                </NavLink>

                <NavLink to="/statistics">
                    <ChartColumnIncreasing size={20} />
                    Statistics
                </NavLink>

                <NavLink to="/profile">
                    <User size={20} />
                    Profile
                </NavLink>

                {user?.role === UserRole.ADMIN && (
                    <>
                        <NavLink to="/admin/questions">
                            <ShieldCheck size={20} />
                            Admin
                        </NavLink>

                        <NavLink to="/admin/learners">
                            <Users size={20} />
                            Learner Progress
                        </NavLink>
                    </>
                )}

                <button
                    type="button"
                    onClick={handleLogout}
                >
                    <LogOut size={20} />
                    Logout
                </button>

            </nav>
        </aside>
    );
};

export default Sidebar;