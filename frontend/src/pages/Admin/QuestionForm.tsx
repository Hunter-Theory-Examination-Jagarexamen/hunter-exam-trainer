import {useRef, useState} from "react";
import type {FormEvent} from "react";
import type {Question} from "../../types/question";
import type {Subject} from "../../types/subject";
import {createQuestion, updateQuestion, questionMutationError} from "../../services/adminQuestionService";

const optionKeys = ["optionA", "optionB", "optionC", "optionD"] as const;
type OptionKey = typeof optionKeys[number];

interface Props {
    question?: Question;
    subjectId: number;
    subjects: Subject[];
    onSaved: (question: Question) => void;
    onCancel: () => void;
}

export default function QuestionForm({question, subjectId, subjects, onSaved, onCancel}: Props) {
    const [text, setText] = useState(question?.questionText ?? "");
    const [options, setOptions] = useState({optionA: question?.optionA ?? "", optionB: question?.optionB ?? "",
        optionC: question?.optionC ?? "", optionD: question?.optionD ?? ""});
    const [correct, setCorrect] = useState<OptionKey | "">(
        question ? optionKeys.find(key => question[key] === question.correctAnswer) ?? "" : "");
    const [explanation, setExplanation] = useState(question?.explanation ?? "");
    const [selectedSubject, setSelectedSubject] = useState(question?.subject.id ?? subjectId);
    const [error, setError] = useState("");
    const [submitting, setSubmitting] = useState(false);
    const pending = useRef(false);

    async function submit(event: FormEvent<HTMLFormElement>) {
        event.preventDefault();
        if (pending.current) return;
        if (!text.trim()) { setError("Enter question text."); return; }
        const empty = optionKeys.find(key => !options[key].trim());
        if (empty) { setError(`Enter text for option ${empty.slice(-1)}.`); return; }
        if (!correct) { setError("Select the correct option."); return; }
        if (!subjects.some(subject => subject.id === selectedSubject && subject.id > 0)) {
            setError("Choose an available subject."); return;
        }
        if ([text, ...Object.values(options), explanation].some(value => value.length > 255)) {
            setError("Question text, options and explanation must each be at most 255 characters."); return;
        }
        pending.current = true;
        setSubmitting(true);
        setError("");
        const request = {questionText: text, ...options, correctAnswer: options[correct],
            explanation, subjectId: selectedSubject, ...(question?.imageUrl ? {imageUrl: question.imageUrl} : {})};
        try {
            const saved = question ? await updateQuestion(question.id, request) : await createQuestion(request);
            onSaved(saved);
        } catch (error: unknown) {
            setError(questionMutationError(error));
        } finally {
            pending.current = false;
            setSubmitting(false);
        }
    }

    return <form className="card question-editor" aria-labelledby="question-editor-title" onSubmit={submit} noValidate>
        <h2 id="question-editor-title">{question ? `Edit question #${question.id}: ${question.questionText}` : "Add question"}</h2>
        <fieldset disabled={submitting}>
            <div className="question-bank-field">
                <label htmlFor="edit-question-text">Question text (required)</label>
                <textarea autoFocus id="edit-question-text" required maxLength={255} value={text} onChange={e => setText(e.target.value)} />
            </div>
            <fieldset>
                <legend>Answer options — select the correct option (required)</legend>
                {optionKeys.map(key => <div className="question-bank-field" key={key}>
                    <label htmlFor={`edit-${key}`}>Option {key.slice(-1)} (required)</label>
                    <input id={`edit-${key}`} required maxLength={255} value={options[key]}
                        onChange={e => setOptions({...options, [key]: e.target.value})} />
                    <label className="question-correct-option"><input type="radio" name="correct-option"
                        checked={correct === key} onChange={() => setCorrect(key)} />Correct option {key.slice(-1)}</label>
                </div>)}
            </fieldset>
            <div className="question-bank-field">
                <label htmlFor="edit-subject">Question subject (required)</label>
                <select id="edit-subject" value={selectedSubject} onChange={e => setSelectedSubject(Number(e.target.value))}>
                    {subjects.map(subject => <option key={subject.id} value={subject.id}>{subject.name}</option>)}
                </select>
            </div>
            <div className="question-bank-field">
                <label htmlFor="edit-explanation">Explanation (optional)</label>
                <textarea id="edit-explanation" maxLength={255} value={explanation} onChange={e => setExplanation(e.target.value)} />
            </div>
        </fieldset>
        {error && <p role="alert">{error}</p>}
        <div className="question-bank-actions">
            <button type="submit" disabled={submitting}>{submitting ? "Saving..." : "Save question"}</button>
            <button type="button" disabled={submitting} onClick={onCancel}>Cancel</button>
        </div>
    </form>;
}
