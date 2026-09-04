import PageTitle from "../../components/common/PageTitle";
import SubjectCards from "../../components/practice/SubjectCards";

const PracticeSubjects = () => {
    return (
        <div>

            <PageTitle
                title="Practice by Subject"
                subtitle="Choose a subject to start practicing."
            />

            <SubjectCards />

        </div>
    );
};

export default PracticeSubjects;