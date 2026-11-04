import PageTitle from "../../components/common/PageTitle";
import SubjectCards from "../../components/practice/SubjectCards";

const PracticeSubjects = () => {
    return (
        <div>

            <PageTitle
                title="Öva per ämne"
                subtitle="Välj ett ämnesområde för att börja öva."
            />

            <SubjectCards />

        </div>
    );
};

export default PracticeSubjects;
