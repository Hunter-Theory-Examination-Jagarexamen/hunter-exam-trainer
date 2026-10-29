import {Outlet} from "react-router-dom";
import ServerStatusBanner from "../common/ServerStatusBanner";

// Shown on every page, above the page itself (<Outlet /> is where the page goes).
const RootLayout = () => (
    <>
        <ServerStatusBanner />
        <Outlet />
    </>
);

export default RootLayout;
