import PageTitle from "../../components/common/PageTitle.tsx";
import PracticeCards from "../../components/practice/PracticeCards.tsx";

const Practice = () => {
    return (
        <div>
            <PageTitle
                title="Practice"
                subtitle="Choose a mode to start practicing."
            />

            <PracticeCards />

        </div>
    );
};

export default Practice;