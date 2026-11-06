import "../../styles/header.css";
import logo from "../../assets/images/logo.svg";
import type { User } from "../../types/user.ts";
import { Menu } from "lucide-react";
import { useTranslation } from "react-i18next";

interface HeaderProps {
    user: User | null;
    onMenuToggle: () => void;
}

const Header = ({ user, onMenuToggle }: HeaderProps) => {
    const { t } = useTranslation();

    return (
        <header className="header">
            <div className="header-left">
                <button
                    type="button"
                    className="menu-toggle"
                    onClick={onMenuToggle}
                    aria-label={t("layout.menuAria")}
                >
                    <Menu size={24} />
                </button>

                <img
                    src={logo}
                    alt={t("brand.logoAlt")}
                    className="header-logo"
                />
                <h1>{t("layout.appTitle")}</h1>
            </div>

            <div className="header-right">
                <span>{t("layout.welcome", { name: user ? user.fullName : t("layout.defaultUser") })}</span>
            </div>
        </header>
    );
};

export default Header;
