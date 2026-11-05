import "../../styles/header.css";
import logo from "../../assets/images/logo.svg";
import type { User } from "../../types/user.ts";
import { Menu } from "lucide-react";

interface HeaderProps {
    user: User | null;
    onMenuToggle: () => void;
}

const Header = ({ user, onMenuToggle }: HeaderProps) => {
    return (
        <header className="header">
            <div className="header-left">
                <button
                    type="button"
                    className="menu-toggle"
                    onClick={onMenuToggle}
                    aria-label="Toggle navigation menu"
                >
                    <Menu size={24} />
                </button>

                <img
                    src={logo}
                    alt="Hunter Exam Trainer Logo"
                    className="header-logo"
                />
                <h1>Hunter Theory Exam</h1>
            </div>

            <div className="header-right">
                <span>Welcome, {user ? user.fullName : "User"}</span>
            </div>
        </header>
    );
};

export default Header;