import {lazy, Suspense} from "react";
import {useTranslation} from "react-i18next";

// The statistics page (with its chart library) is loaded only when first opened.
const Statistics = lazy(() => import("./Statistics"));

const LazyStatistics = () => {
    const { t } = useTranslation();
    return (
        <Suspense fallback={<div>{t("common.loading")}</div>}>
            <Statistics />
        </Suspense>
    );
};

export default LazyStatistics;
