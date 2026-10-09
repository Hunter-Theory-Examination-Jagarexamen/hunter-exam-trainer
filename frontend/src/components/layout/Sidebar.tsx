import "../../styles/sidebar.css";
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
import { NavLink, useLocation, useNavigate } from "react-router-dom";
import { UserRole, type User as CurrentUser } from "../../types/user";

interface SidebarProps {
    user: CurrentUser | null;
    isOpen: boolean;
    onClose: () => void;
}

const Sidebar = ({ user, isOpen, onClose }: SidebarProps) => {
    const location = useLocation();
    const navigate = useNavigate();

    const handleLogout = () => {
        localStorage.removeItem("token");
        onClose();
        navigate("/login");
    };

    const sidebarClass = `sidebar${isOpen ? " open" : ""}`;

    return (
        <aside className={sidebarClass}>
            <nav className="sidebar-nav">
                <NavLink to="/dashboard" onClick={onClose}>
                    <LayoutDashboard size={20} />
                    Dashboard
                </NavLink>

                <NavLink to="/practice" onClick={onClose}>
                    <BookOpen size={20} />
                    Practice
                </NavLink>

                <NavLink
                    to="/mockexam"
                    onClick={onClose}
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

                <NavLink to="/statistics" onClick={onClose}>
                    <ChartColumnIncreasing size={20} />
                    Statistics
                </NavLink>

                <NavLink to="/profile" onClick={onClose}>
                    <User size={20} />
                    Profile
                </NavLink>

                {user?.role === UserRole.ADMIN && (
                    <>
                        <NavLink to="/admin/questions" onClick={onClose}>
                            <ShieldCheck size={20} />
                            Admin
                        </NavLink>

                        <NavLink to="/admin/learners" onClick={onClose}>
                            <Users size={20} />
                            Learner Progress
                        </NavLink>
                    </>
                )}

                <button type="button" onClick={handleLogout}>
                    <LogOut size={20} />
                    Logout
                </button>
            </nav>
        </aside>
    );
};

export default Sidebar;