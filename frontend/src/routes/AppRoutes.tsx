import {useState} from "react";
import {createBrowserRouter, RouterProvider} from "react-router-dom";
import {routes} from "./routes";

// A "data router" (React Router v7): needed for features such as useBlocker,
// which the practice page uses to ask before leaving a round (#170).
const AppRoutes = () => {
    // Created once when the app mounts (useState keeps it between renders), so it
    // starts at the browser's current address. Tests that set the address before
    // rendering <AppRoutes /> get a fresh router each time.
    const [router] = useState(() => createBrowserRouter(routes));
    return <RouterProvider router={router} />;
};

export default AppRoutes;
