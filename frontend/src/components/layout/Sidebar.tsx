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
    MessageSquare,
    Inbox,
} from "lucide-react";
import { useState } from "react";
import { createPortal } from "react-dom";
import { NavLink, useLocation, useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { UserRole, type User as CurrentUser } from "../../types/user";
import FeedbackDialog from "../feedback/FeedbackDialog";

interface SidebarProps {
    user: CurrentUser | null;
    isOpen: boolean;
    onClose: () => void;
}

const Sidebar = ({ user, isOpen, onClose }: SidebarProps) => {
    const { t } = useTranslation();
    const location = useLocation();
    const navigate = useNavigate();
    // True while the feedback dialog is open.
    const [isGivingFeedback, setIsGivingFeedback] = useState(false);

    // The /logout page forgets the login. Going there as a navigation lets
    // the mock exam ask first, without logging out a student who stays (#188).
    const handleLogout = () => {
        onClose();
        navigate("/logout");
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
                    {t("layout.navDashboard")}
                </NavLink>

                <NavLink to="/practice" onClick={onClose}>
                    <BookOpen size={20} />
                    {t("layout.navPractice")}
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
                    {t("layout.navMockExam")}
                </NavLink>

                <NavLink to="/statistics" onClick={onClose}>
                    <ChartColumnIncreasing size={20} />
                    {t("layout.navStatistics")}
                </NavLink>

                <NavLink to="/profile" onClick={onClose}>
                    <User size={20} />
                    {t("layout.navProfile")}
                </NavLink>

                <button type="button" onClick={() => { onClose(); setIsGivingFeedback(true); }}>
                    <MessageSquare size={20} />
                    {t("layout.giveFeedback")}
                </button>

                {user?.role === UserRole.ADMIN && (
                    <>
                        {/* "end": only active on /admin itself, not on /admin/questions etc. */}
                        <NavLink to="/admin" end onClick={onClose} className={headerClass}>
                            <ShieldCheck size={20} />
                            {t("layout.navAdmin")}
                        </NavLink>

                        <NavLink to="/admin/questions" onClick={onClose}>
                            <ListChecks size={20} />
                            {t("layout.navQuestions")}
                        </NavLink>

                        <NavLink to="/admin/subjects" onClick={onClose}>
                            <BookOpen size={20} />
                            {t("layout.navSubjects")}
                        </NavLink>

                        <NavLink to="/admin/learners" onClick={onClose}>
                            <Users size={20} />
                            {t("layout.navLearners")}
                        </NavLink>

                        <NavLink to="/admin/feedback" onClick={onClose}>
                            <Inbox size={20} />
                            {t("layout.navFeedback")}
                        </NavLink>
                    </>
                )}

                <button type="button" className="sidebar-logout" onClick={handleLogout}>
                    <LogOut size={20} />
                    {t("layout.logout")}
                </button>
            </nav>

            {/* Drawn at the end of <body>, not inside the sidebar: on mobile the sidebar is
                moved off-screen with a CSS transform, which would move a fixed dialog with it. */}
            {isGivingFeedback && createPortal(
                <FeedbackDialog onClose={() => setIsGivingFeedback(false)} />,
                document.body
            )}
        </aside>
    );
};

export default Sidebar;
