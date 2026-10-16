import "../../styles/header.css"
import logo from "../../assets/images/logo.svg";
import type {User} from "../../types/user.ts";

const Header = ({ user }: { user: User | null }) => {

    return (
        <header className="header">
            <div className="header-left">
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
