import "../../styles/mockexam.css";

const ExamInstructions = () => {
    return (
        <section className="mockExam-section">

            <h2>Instruktioner</h2>

            <ul className="instructions-list">
                <li>Provet innehåller 70 flervalsfrågor.</li>
                <li>Du har 60 minuter på dig att slutföra provet.</li>
                <li>Varje fråga har bara ett korrekt svar.</li>
                <li>Du kan granska dina svar innan du skickar in.</li>
                <li>Din poäng visas efter inlämning.</li>
            </ul>

        </section>
    );
};

export default ExamInstructions;
