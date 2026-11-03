import {lazy, Suspense} from "react";

// The statistics page (with its chart library) is loaded only when first opened.
const Statistics = lazy(() => import("./Statistics"));

const LazyStatistics = () => (
    <Suspense fallback={<div>Loading...</div>}>
        <Statistics />
    </Suspense>
);

export default LazyStatistics;
