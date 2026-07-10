import Header from "./Header";
import Sidebar from "./Sidebar";

interface LayoutProps {
    children: React.ReactNode;
}

const Layout = ({ children } : LayoutProps) => {
    return (
        <>
            <Header />
            <Sidebar />
            <main>
                {children}
            </main>
        </>
    );
};

export default Layout;