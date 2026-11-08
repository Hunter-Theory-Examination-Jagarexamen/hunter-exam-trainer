import { useState } from "react";
import { useTranslation } from "react-i18next";
import { Link } from "react-router-dom";
import QuestionImage from "../common/QuestionImage";
import { getQuestion } from "../../services/adminQuestionService";
import { errorStatus } from "../../services/apiErrors";
import type { Question } from "../../types/question";

// The reported question as it is now, shown inside a feedback item on the admin
// page, so the admin can judge the report without leaving the feedback list.
// Loaded the first time it's opened.
const ReportedQuestion = ({ questionId }: { questionId: number }) => {
    const { t } = useTranslation();
    const [isOpen, setIsOpen] = useState(false);
    const [question, setQuestion] = useState<Question | null>(null);
    const [status, setStatus] = useState<"idle" | "loading" | "gone" | "failed">("idle");

    const toggle = async () => {
        const opening = !isOpen;
        setIsOpen(opening);
        if (!opening || question !== null || status === "loading") return;
        setStatus("loading");
        try {
            setQuestion(await getQuestion(questionId));
            setStatus("idle");
        } catch (error) {
            setStatus(errorStatus(error) === 404 ? "gone" : "failed");
        }
    };

    const panelId = `reported-question-${questionId}`;

    return (
        <div className="reported-question">
            <button type="button" className="reported-question-toggle" aria-expanded={isOpen}
                    aria-controls={panelId} onClick={() => void toggle()}>
                {isOpen ? t("feedback.admin.hideQuestion") : t("feedback.admin.showQuestion")}
            </button>

            {isOpen && (
                <div id={panelId} className="reported-question-panel">
                    {status === "loading" && <p role="status">{t("feedback.admin.loadingQuestion")}</p>}
                    {status === "gone" && <p>{t("feedback.admin.questionGone")}</p>}
                    {status === "failed" && <p role="alert">{t("feedback.admin.questionLoadFailed")}</p>}
                    {question && (
                        <>
                            <h3>#{question.id}: {question.questionText}</h3>
                            <QuestionImage imageUrl={question.imageUrl} questionText={question.questionText} />
                            <ol type="A">
                                {[question.optionA, question.optionB, question.optionC, question.optionD]
                                    .map((option, index) => <li key={index}>{option}</li>)}
                            </ol>
                            <p><strong>{t("feedback.admin.correctAnswer")}</strong> {question.correctAnswer}</p>
                            {question.explanation &&
                                <p><strong>{t("feedback.admin.explanation")}</strong> {question.explanation}</p>}
                            <Link to={`/admin/questions?question=${question.id}`}>
                                {t("feedback.admin.openQuestion")}
                            </Link>
                        </>
                    )}
                </div>
            )}
        </div>
    );
};

export default ReportedQuestion;
