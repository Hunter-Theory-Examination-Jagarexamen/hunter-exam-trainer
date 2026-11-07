import {useRef, useState} from "react";
import type {FormEvent} from "react";
import type {Question} from "../../types/question";
import type {Subject} from "../../types/subject";
import {createQuestion, updateQuestion, questionMutationError} from "../../services/adminQuestionService";
import QuestionImage from "../../components/common/QuestionImage";
import {isQuestionImageUrl} from "../../utils/questionImageUrl";
import {useTranslation} from "react-i18next";

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
    const {t} = useTranslation();
    const [text, setText] = useState(question?.questionText ?? "");
    const [options, setOptions] = useState({optionA: question?.optionA ?? "", optionB: question?.optionB ?? "",
        optionC: question?.optionC ?? "", optionD: question?.optionD ?? ""});
    const [correct, setCorrect] = useState<OptionKey | "">(
        question ? (`option${question.correctAnswer}` as OptionKey) : "");
    const [explanation, setExplanation] = useState(question?.explanation ?? "");
    const [imageUrl, setImageUrl] = useState(question?.imageUrl ?? "");
    const [selectedSubject, setSelectedSubject] = useState(question?.subject.id ?? subjectId);
    const [error, setError] = useState("");
    const [submitting, setSubmitting] = useState(false);
    const pending = useRef(false);

    async function submit(event: FormEvent<HTMLFormElement>) {
        event.preventDefault();
        if (pending.current) return;
        if (!text.trim()) { setError(t("questionBank.editor.errors.missingText")); return; }
        const empty = optionKeys.find(key => !options[key].trim());
        if (empty) { setError(t("questionBank.editor.errors.missingOption", {key: empty.slice(-1)})); return; }
        if (!correct) { setError(t("questionBank.editor.errors.missingCorrect")); return; }
        if (!subjects || !subjects.some(subject => subject.id === selectedSubject && subject.id > 0)) {
            setError(t("questionBank.editor.errors.invalidSubject")); return;
        }
        if ([text, ...Object.values(options), explanation].some(value => value.length > 255)) {
            setError(t("questionBank.editor.errors.tooLong")); return;
        }
        if (imageUrl.trim().length > 1000) {
            setError(t("questionBank.editor.errors.imageUrlTooLong")); return;
        }
        if (imageUrl.trim() && !isQuestionImageUrl(imageUrl.trim())) {
            setError(t("questionBank.editor.errors.imageUrlInvalid")); return;
        }
        pending.current = true;
        setSubmitting(true);
        setError("");
        const request = {questionText: text, ...options, correctAnswer: correct.slice(-1),
            explanation, subjectId: selectedSubject, imageUrl: imageUrl.trim() || null};
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
        <h2 id="question-editor-title">{question ? t("questionBank.editor.editTitle", {id: question.id, text: question.questionText}) : t("questionBank.editor.createTitle")}</h2>
        <fieldset disabled={submitting}>
            <div className="question-bank-field">
                <label htmlFor="edit-question-text">{t("questionBank.editor.textLabel")}</label>
                <textarea autoFocus id="edit-question-text" required maxLength={255} value={text} onChange={e => setText(e.target.value)} />
            </div>
            <fieldset>
                <legend>{t("questionBank.editor.legendOptions")}</legend>
                {optionKeys.map(key => <div className="question-bank-field" key={key}>
                    <label htmlFor={`edit-${key}`}>{t("questionBank.editor.optionLabel", {key: key.slice(-1)})}</label>
                    <input id={`edit-${key}`} required maxLength={255} value={options[key]}
                           onChange={e => setOptions({...options, [key]: e.target.value})} />
                    <label className="question-correct-option"><input type="radio" name="correct-option"
                                                                      checked={correct === key} onChange={() => setCorrect(key)} />{t("questionBank.editor.correctOptionLabel", {key: key.slice(-1)})}</label>
                </div>)}
            </fieldset>
            <div className="question-bank-field">
                <label htmlFor="edit-subject">{t("questionBank.editor.subjectLabel")}</label>
                <select id="edit-subject" value={selectedSubject} onChange={e => setSelectedSubject(Number(e.target.value))}>
                    {subjects && subjects.map(subject => <option key={subject.id} value={subject.id}>{subject.name}</option>)}
                </select>
            </div>
            <div className="question-bank-field">
                <label htmlFor="edit-image-url">{t("questionBank.editor.imageUrlLabel")}</label>
                <input id="edit-image-url" type="url" maxLength={1000} value={imageUrl}
                       aria-describedby="image-url-help" onChange={e => setImageUrl(e.target.value)} />
                <p id="image-url-help">{t("questionBank.editor.imageUrlHelp")}</p>
                {imageUrl && <button type="button" onClick={() => setImageUrl("")}>{t("questionBank.editor.removeImage")}</button>}
                <QuestionImage imageUrl={imageUrl} questionText={text.trim() || t("questionBank.editor.newQuestion")} />
            </div>
            <div className="question-bank-field">
                <label htmlFor="edit-explanation">{t("questionBank.editor.explanationLabel")}</label>
                <textarea id="edit-explanation" maxLength={255} value={explanation} onChange={e => setExplanation(e.target.value)} />
            </div>
        </fieldset>
        {error && <p role="alert">{error}</p>}
        <div className="question-bank-actions">
            <button type="submit" disabled={submitting}>{submitting ? t("questionBank.saving") : t("questionBank.saveQuestion")}</button>
            <button type="button" disabled={submitting} onClick={onCancel}>{t("questionBank.cancel")}</button>
        </div>
    </form>;
}
