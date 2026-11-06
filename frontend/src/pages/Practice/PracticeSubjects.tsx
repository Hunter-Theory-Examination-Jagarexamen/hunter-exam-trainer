import { useTranslation } from "react-i18next";
import PageTitle from "../../components/common/PageTitle";
import SubjectCards from "../../components/practice/SubjectCards";

const PracticeSubjects = () => {
    const { t } = useTranslation();

    return (
        <div>

            <PageTitle
                title={t("practice.subjectsTitle")}
                subtitle={t("practice.subjectsSubtitle")}
            />

            <SubjectCards />

        </div>
    );
};

export default PracticeSubjects;
