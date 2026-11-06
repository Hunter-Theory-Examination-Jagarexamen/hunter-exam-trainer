import { useTranslation } from "react-i18next";
import PageTitle from "../../components/common/PageTitle.tsx";
import PracticeCards from "../../components/practice/PracticeCards.tsx";

const Practice = () => {
    const { t } = useTranslation();

    return (
        <div>
            <PageTitle
                title={t("practice.title")}
                subtitle={t("practice.subtitle")}
            />

            <PracticeCards />

        </div>
    );
};

export default Practice;
