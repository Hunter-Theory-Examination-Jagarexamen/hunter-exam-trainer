import Header from "./Header";
import Sidebar from "./Sidebar";
import "../../styles/layout.css"
import {Outlet} from "react-router-dom";

const Layout = () => {
    return (
        <div className="layout">
            <Header />

            <div className="layout-body">
                <Sidebar />

                <main className="main-content">
                    <Outlet />
                </main>
            </div>
        </div>
    );
};

export default Layout;