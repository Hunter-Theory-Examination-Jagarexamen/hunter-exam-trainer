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
                    Instrumentpanel
                </NavLink>

                <NavLink to="/practice" onClick={onClose}>
                    <BookOpen size={20} />
                    Öva
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
                    Övningsprov
                </NavLink>

                <NavLink to="/statistics" onClick={onClose}>
                    <ChartColumnIncreasing size={20} />
                    Statistik
                </NavLink>

                <NavLink to="/profile" onClick={onClose}>
                    <User size={20} />
                    Profil
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
                            Frågor
                        </NavLink>

                        <NavLink to="/admin/subjects" onClick={onClose}>
                            <BookOpen size={20} />
                            Ämnen
                        </NavLink>

                        <NavLink to="/admin/learners" onClick={onClose}>
                            <Users size={20} />
                            Elever
                        </NavLink>
                    </>
                )}

                <button type="button" className="sidebar-logout" onClick={handleLogout}>
                    <LogOut size={20} />
                    Logga ut
                </button>
            </nav>
        </aside>
    );
};

export default Sidebar;
