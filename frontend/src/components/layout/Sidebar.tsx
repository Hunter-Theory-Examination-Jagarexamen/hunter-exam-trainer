import "../../styles/sidebar.css"
import {
    LayoutDashboard,
    BookOpen,
    ClipboardCheck,
    ChartColumnIncreasing,
    LogOut,
    User
} from "lucide-react";

const Sidebar = () => {
    return (
        <aside className="sidebar">
            <nav>
                <ul>
                    <li>
                        <LayoutDashboard size={20} />
                        Dashboard
                    </li>
                    <li>
                        <BookOpen size={20} />
                        Practice
                    </li>
                    <li>
                        <ClipboardCheck size={20} />
                        Mock Exam
                    </li>
                    <li>
                        <ChartColumnIncreasing size={20} />
                        Statistics
                    </li>
                    <li>
                        <User size={20} />
                        Profile
                    </li>
                    <li>
                        <LogOut size={20} />
                        Logout
                    </li>
                </ul>
            </nav>
        </aside>
    );
};

export default Sidebar;