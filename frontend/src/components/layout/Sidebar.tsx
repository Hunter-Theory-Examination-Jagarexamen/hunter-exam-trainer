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
    ListChecks,
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

    // Section headers ("Dashboard", "Admin") get their own class, plus "active" on their own page.
    const headerClass = ({ isActive }: { isActive: boolean }) =>
        `sidebar-section-header${isActive ? " active" : ""}`;

    return (
        <aside className={sidebarClass}>
            <nav className="sidebar-nav">
                <NavLink to="/dashboard" onClick={onClose} className={headerClass}>
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
                        {/* "end": only active on /admin itself, not on /admin/questions etc. */}
                        <NavLink to="/admin" end onClick={onClose} className={headerClass}>
                            <ShieldCheck size={20} />
                            Admin
                        </NavLink>

                        <NavLink to="/admin/questions" onClick={onClose}>
                            <ListChecks size={20} />
                            Questions
                        </NavLink>

                        <NavLink to="/admin/subjects" onClick={onClose}>
                            <BookOpen size={20} />
                            Subjects
                        </NavLink>

                        <NavLink to="/admin/learners" onClick={onClose}>
                            <Users size={20} />
                            Learners
                        </NavLink>
                    </>
                )}

                <button type="button" className="sidebar-logout" onClick={handleLogout}>
                    <LogOut size={20} />
                    Logout
                </button>
            </nav>
        </aside>
    );
};

export default Sidebar;
