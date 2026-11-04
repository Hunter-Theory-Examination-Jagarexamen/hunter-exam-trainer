import PageTitle from "../../components/common/PageTitle.tsx";
import PracticeCards from "../../components/practice/PracticeCards.tsx";

const Practice = () => {
    return (
        <div>
            <PageTitle
                title="Öva"
                subtitle="Välj ett läge för att börja öva."
            />

            <PracticeCards />

        </div>
    );
};

export default Practice;
