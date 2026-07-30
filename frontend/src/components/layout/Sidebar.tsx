import "../../styles/sidebar.css"
import {
    LayoutDashboard,
    BookOpen,
    ClipboardCheck,
    ChartColumnIncreasing,
    LogOut,
    User
} from "lucide-react";
import {NavLink} from "react-router-dom";

const Sidebar = () => {
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
                <NavLink to="/mockexam">
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
                <NavLink to="/logout">
                    <LogOut size={20} />
                    Logout
                </NavLink>
            </nav>
        </aside>
    );
};

export default Sidebar;