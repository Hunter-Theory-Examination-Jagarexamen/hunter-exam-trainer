import PracticeQuestions from "./PracticeQuestions";

// Random practice (#151): the server picks questions from all subjects and
// checks every answer. The round starts as soon as the page opens, the same
// way choosing a subject starts a subject round.
const RandomPractice = () => {
    return <PracticeQuestions mode="random" />;
};

export default RandomPractice;
