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
                    aria-label="Växla navigeringsmeny"
                >
                    <Menu size={24} />
                </button>

                <img
                    src={logo}
                    alt="Hunter Exam Trainer Logotyp"
                    className="header-logo"
                />
                <h1>Jägarexamen Teori</h1>
            </div>

            <div className="header-right">
                <span>Välkommen, {user ? user.fullName : "Användare"}</span>
            </div>
        </header>
    );
};

export default Header;
