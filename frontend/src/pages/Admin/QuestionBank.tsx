import {useEffect, useRef, useState} from "react";
import apiClient from "../../api/apiClient";
import PageTitle from "../../components/common/PageTitle";
import type {Subject} from "../../types/subject";
import type {Question} from "../../types/question";
import "../../styles/admin.css";
import QuestionForm from "./QuestionForm";
import QuestionImage from "../../components/common/QuestionImage";
import {deleteQuestion, questionMutationError} from "../../services/adminQuestionService";
import {useTranslation} from "react-i18next";
import {useSearchParams} from "react-router-dom";

const PAGE_SIZE = 20;

const SubjectQuestions = ({subjectId, subjects, onCountChange, onBusyChange, onSavedElsewhere, openOnLoad, onOpenedOnLoad}: {
    subjectId: number; subjects: Subject[]; onCountChange: (id: number, delta: number) => void;
    // A question to open in the editor once the list has loaded (from a link, ?edit=<id>).
    openOnLoad?: number | null; onOpenedOnLoad?: () => void;
    onBusyChange: (busy: boolean) => void;
    onSavedElsewhere: (question: Question, created: boolean) => void;
}) => {
    const {t} = useTranslation();
    const [questions, setQuestions] = useState<Question[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState("");
    const [search, setSearch] = useState("");
    const [page, setPage] = useState(1);
    const [editor, setEditor] = useState<Question | "create" | null>(null);
    const [deleting, setDeleting] = useState<Question | null>(null);
    const [deleteError, setDeleteError] = useState("");
    const [submitting, setSubmitting] = useState(false);
    const [success, setSuccess] = useState("");
    const pending = useRef(false);
    const addButton = useRef<HTMLButtonElement>(null);

    useEffect(() => {
        if (editor === null && deleting === null) addButton.current?.focus();
    }, [editor, deleting]);

    function closeEditor() {
        setEditor(null);
        onBusyChange(false);
    }

    function saved(question: Question) {
        const oldSubject = editor && editor !== "create" ? editor.subject.id : null;
        setQuestions(previous => {
            const remaining = previous.filter(item => item.id !== question.id);
            return question.subject.id === subjectId ? [question, ...remaining] : remaining;
        });
        if (oldSubject !== question.subject.id) {
            if (oldSubject !== null) onCountChange(oldSubject, -1);
            onCountChange(question.subject.id, 1);
        }
        // Keep matching search results, but reveal a saved question if the old
        // search no longer matches it. Browsing stays in the selected subject.
        if (question.subject.id === subjectId) {
            if (![question.id.toString(), question.questionText, question.optionA, question.optionB,
                question.optionC, question.optionD, question.explanation].some(value =>
                value?.toLocaleLowerCase().includes(search.trim().toLocaleLowerCase()))) setSearch("");
            setPage(1);
        }
        setSuccess(t(editor === "create" ? "questionBank.successCreated" : "questionBank.successUpdated",
            {id: question.id, subject: question.subject.name}));
        closeEditor();
        if (question.subject.id !== subjectId) onSavedElsewhere(question, editor === "create");
    }

    async function confirmDelete() {
        if (!deleting || pending.current) return;
        pending.current = true;
        setSubmitting(true);
        setDeleteError("");
        try {
            await deleteQuestion(deleting.id);
            setQuestions(previous => previous.filter(question => question.id !== deleting.id));
            onCountChange(subjectId, -1);
            setSuccess(t("questionBank.successDeleted", {id: deleting.id}));
            setDeleting(null);
            onBusyChange(false);
        } catch (error: unknown) {
            setDeleteError(questionMutationError(error));
        } finally {
            pending.current = false;
            setSubmitting(false);
        }
    }

    useEffect(() => {
        let active = true;
        apiClient(`/api/admin/questions?subjectId=${subjectId}`).then((data: Question[]) => {
            if (active) {
                setQuestions(data);
                setIsLoading(false);
                const linked = openOnLoad ? data.find(question => question.id === openOnLoad) : undefined;
                if (linked) {
                    setEditor(linked);
                    onBusyChange(true);
                    onOpenedOnLoad?.();
                }
            }
        }).catch(() => {
            if (active) {
                setError(t("questionBank.loadQuestionsFailed"));
                setIsLoading(false);
            }
        });
        return () => { active = false; };
    }, [subjectId]);

    if (isLoading) return <p role="status">{t("questionBank.loadingQuestions")}</p>;
    if (error) return <p role="alert">{error}</p>;

    const term = search.trim().toLocaleLowerCase();
    const filtered = questions.filter(question => [
        question.id.toString(), question.questionText,
        question.optionA, question.optionB, question.optionC, question.optionD,
        question.explanation,
    ].some(value => value?.toLocaleLowerCase().includes(term)));
    const pageCount = Math.ceil(filtered.length / PAGE_SIZE);
    const currentPage = Math.min(page, Math.max(1, pageCount));

    return <>
        <div className="question-bank-actions">
            <button ref={addButton} type="button" disabled={editor !== null || deleting !== null}
                onClick={() => { setSuccess(""); setEditor("create"); onBusyChange(true); }}>{t("questionBank.addQuestion")}</button>
        </div>
        {success && <p role="status">{success}</p>}
        {editor !== null && <QuestionForm question={editor === "create" ? undefined : editor}
            subjectId={subjectId} subjects={subjects} onSaved={saved} onCancel={closeEditor} />}
        {deleting && <section className="card question-delete-confirmation" aria-labelledby="delete-question-title">
            <h2 id="delete-question-title">{t("questionBank.deleteTitle", {id: deleting.id})}</h2>
            <p>{deleting.questionText}</p>
            <p>{t("questionBank.deleteWarning")}</p>
            {deleteError && <p role="alert">{deleteError}</p>}
            <div className="question-bank-actions">
                <button autoFocus type="button" disabled={submitting} onClick={() => {
                    setDeleting(null); onBusyChange(false);
                }}>{t("questionBank.cancelDeletion")}</button>
                <button type="button" className="question-delete" disabled={submitting} onClick={confirmDelete}>
                    {submitting ? t("questionBank.deleting") : t("questionBank.confirmDeletion")}</button>
            </div>
        </section>}
        {questions.length === 0 && <p>{t("questionBank.noQuestions")}</p>}
        <div className="question-bank-field">
            <label htmlFor="question-search">{t("questionBank.searchLabel")}</label>
            <input id="question-search" type="search" value={search}
                placeholder={t("questionBank.searchPlaceholder")}
                onChange={event => { setSearch(event.target.value); setPage(1); }} />
        </div>
        <p role="status">{t("questionBank.questionCount", {filtered: filtered.length, total: questions.length})}</p>
        {questions.length > 0 && filtered.length === 0 && <p>{t("questionBank.noMatch")}</p>}
        <div className="question-bank-list">
            {filtered.slice((currentPage - 1) * PAGE_SIZE, currentPage * PAGE_SIZE).map(question => (
                <article className="card" key={question.id}>
                    <h2 className="card-title">#{question.id}: {question.questionText}</h2>
                    <QuestionImage imageUrl={question.imageUrl} questionText={question.questionText} />
                    <ol type="A">
                        {[question.optionA, question.optionB, question.optionC, question.optionD].map((option, index) => (
                            <li key={index}>{option}</li>
                        ))}
                    </ol>
                    <p><strong>{t("questionBank.correctAnswer")}</strong> {question.correctAnswer}</p>
                    {question.explanation && <p><strong>{t("questionBank.explanation")}</strong> {question.explanation}</p>}
                    <div className="question-bank-actions">
                        <button type="button" aria-label={t("questionBank.editAria", {id: question.id})} disabled={editor !== null || deleting !== null}
                            onClick={() => { setSuccess(""); setEditor(question); onBusyChange(true); }}>{t("questionBank.edit")}</button>
                        <button type="button" className="question-delete" aria-label={t("questionBank.deleteAria", {id: question.id})}
                            disabled={editor !== null || deleting !== null} onClick={() => {
                                setSuccess(""); setDeleteError(""); setDeleting(question);
                                onBusyChange(true);
                            }}>{t("questionBank.delete")}</button>
                    </div>
                </article>
            ))}
        </div>
        {pageCount > 1 && <nav className="question-bank-pagination" aria-label={t("questionBank.pagesAria")}>
            <button type="button" disabled={currentPage === 1} onClick={() => setPage(currentPage - 1)}>{t("questionBank.previous")}</button>
            <span>{t("questionBank.page", {current: currentPage, total: pageCount})}</span>
            <button type="button" disabled={currentPage === pageCount} onClick={() => setPage(currentPage + 1)}>{t("questionBank.next")}</button>
        </nav>}
    </>;
};

const QuestionBank = () => {
    const {t} = useTranslation();
    const [busy, setBusy] = useState(false);
    const [success, setSuccess] = useState("");
    const [subjects, setSubjects] = useState<Subject[]>([]);
    const [subjectId, setSubjectId] = useState<number | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState("");
    // ?edit=12 (e.g. from a feedback report) opens question 12 in the editor.
    const [searchParams, setSearchParams] = useSearchParams();
    const [openOnLoad, setOpenOnLoad] = useState<number | null>(() => Number(searchParams.get("edit")) || null);
    const [linkError, setLinkError] = useState("");

    useEffect(() => {
        let active = true;
        apiClient("/api/subjects").then(async (data: Subject[]) => {
            // For a linked question, start in its subject; otherwise in the first one.
            let startSubject = data[0]?.id ?? null;
            if (openOnLoad) {
                try {
                    const linked: Question = await apiClient(`/api/admin/questions/${openOnLoad}`);
                    startSubject = linked.subject.id;
                } catch {
                    if (active) {
                        setLinkError(t("questionBank.linkedQuestionNotFound", {id: openOnLoad}));
                        setOpenOnLoad(null);
                    }
                }
            }
            if (active) {
                setSubjects(data);
                setSubjectId(startSubject);
                setIsLoading(false);
            }
        }).catch(() => {
            if (active) {
                setError(t("questionBank.loadSubjectsFailed"));
                setIsLoading(false);
            }
        });
        return () => { active = false; };
    }, []);

    return <div className="question-bank">
        <PageTitle title={t("questionBank.title")} subtitle={t("questionBank.subtitle")} />
        {success && <p role="status">{success}</p>}
        {linkError && <p role="alert">{linkError}</p>}
        {isLoading ? <p role="status">{t("questionBank.loadingSubjects")}</p> : error ? <p role="alert">{error}</p> :
            subjects.length === 0 ? <p>{t("questionBank.noSubjects")}</p> : <>
                <div className="question-bank-field">
                    <label htmlFor="question-subject">{t("questionBank.subjectLabel")}</label>
                    <select id="question-subject" value={subjectId ?? ""} disabled={busy}
                        onChange={event => { setSuccess(""); setSubjectId(Number(event.target.value)); }}>
                        {subjects.map(subject => <option key={subject.id} value={subject.id}>
                            {subject.name} ({subject.questionCount})
                        </option>)}
                    </select>
                </div>
                {subjectId !== null && <SubjectQuestions key={subjectId} subjectId={subjectId} subjects={subjects}
                    openOnLoad={openOnLoad}
                    onOpenedOnLoad={() => {
                        // Only once: clear it, and take ?edit off the address.
                        setOpenOnLoad(null);
                        setSearchParams({}, {replace: true});
                    }}
                    onBusyChange={value => { setBusy(value); if (value) setSuccess(""); }}
                    onSavedElsewhere={(question, created) => {
                        setSuccess(t(created ? "questionBank.successCreated" : "questionBank.successUpdated",
                            {id: question.id, subject: question.subject.name}));
                        setSubjectId(question.subject.id);
                    }}
                    onCountChange={(id, delta) => setSubjects(previous => previous.map(subject =>
                        subject.id === id ? {...subject, questionCount: Math.max(0, subject.questionCount + delta)} : subject))} />}
            </>}
    </div>;
};

export default QuestionBank;
