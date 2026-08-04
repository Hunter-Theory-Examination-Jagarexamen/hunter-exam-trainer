import "../../styles/header.css"
import logo from "../../assets/images/logo.svg";

const Header = () => {
    return (
        <header className="header">
            <div className="header-left">
                <img src={logo} alt="Hunter Exam Trainer Logo" className="header-logo" />
                <h1>Hunter Theory Exam</h1>
            </div>
            <div className="header-right">
                <span>Welcome, User</span>
            </div>
        </header>
    );
};

export default Header;