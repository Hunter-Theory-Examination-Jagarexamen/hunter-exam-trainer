import Header from "./Header";
import Sidebar from "./Sidebar";
import "../../styles/layout.css"

interface LayoutProps {
    children: React.ReactNode;
}

const Layout = ({ children } : LayoutProps) => {
    return (
        <div className="layout">
            <Header />

            <div className="layout-body">
                <Sidebar />
                <main className="main-content">
                    {children}
                </main>
            </div>
        </div>
    );
};

export default Layout;