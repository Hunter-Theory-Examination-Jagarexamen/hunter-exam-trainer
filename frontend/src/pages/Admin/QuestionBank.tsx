import {useEffect, useRef, useState} from "react";
import apiClient from "../../api/apiClient";
import PageTitle from "../../components/common/PageTitle";
import type {Subject} from "../../types/subject";
import type {Question} from "../../types/question";
import "../../styles/admin.css";
import QuestionForm from "./QuestionForm";
import {deleteQuestion, questionMutationError} from "../../services/adminQuestionService";

const PAGE_SIZE = 20;

const SubjectQuestions = ({subjectId, subjects, onCountChange, onBusyChange, onSavedElsewhere}: {
    subjectId: number; subjects: Subject[]; onCountChange: (id: number, delta: number) => void;
    onBusyChange: (busy: boolean) => void;
    onSavedElsewhere: (question: Question, created: boolean) => void;
}) => {
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
        setSuccess(`Question #${question.id} ${editor === "create" ? "created" : "updated"} in ${question.subject.name}.`);
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
            setSuccess(`Question #${deleting.id} deleted.`);
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
        apiClient(`/api/questions?subjectId=${subjectId}`).then((data: Question[]) => {
            if (active) {
                setQuestions(data);
                setIsLoading(false);
            }
        }).catch(() => {
            if (active) {
                setError("Failed to load questions. Please try again.");
                setIsLoading(false);
            }
        });
        return () => { active = false; };
    }, [subjectId]);

    if (isLoading) return <p role="status">Loading questions...</p>;
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
                onClick={() => { setSuccess(""); setEditor("create"); onBusyChange(true); }}>Add question</button>
        </div>
        {success && <p role="status">{success}</p>}
        {editor !== null && <QuestionForm question={editor === "create" ? undefined : editor}
            subjectId={subjectId} subjects={subjects} onSaved={saved} onCancel={closeEditor} />}
        {deleting && <section className="card question-delete-confirmation" aria-labelledby="delete-question-title">
            <h2 id="delete-question-title">Delete question #{deleting.id}?</h2>
            <p>{deleting.questionText}</p>
            <p>This permanently deletes the question. This action cannot be undone.</p>
            {deleteError && <p role="alert">{deleteError}</p>}
            <div className="question-bank-actions">
                <button autoFocus type="button" disabled={submitting} onClick={() => {
                    setDeleting(null); onBusyChange(false);
                }}>Cancel deletion</button>
                <button type="button" className="question-delete" disabled={submitting} onClick={confirmDelete}>
                    {submitting ? "Deleting..." : "Confirm deletion"}</button>
            </div>
        </section>}
        {questions.length === 0 && <p>No questions available for this subject.</p>}
        <div className="question-bank-field">
            <label htmlFor="question-search">Search questions</label>
            <input id="question-search" type="search" value={search}
                placeholder="Search by ID, question, answer or explanation"
                onChange={event => { setSearch(event.target.value); setPage(1); }} />
        </div>
        <p role="status">{filtered.length} of {questions.length} questions</p>
        {questions.length > 0 && filtered.length === 0 && <p>No questions match your search.</p>}
        <div className="question-bank-list">
            {filtered.slice((currentPage - 1) * PAGE_SIZE, currentPage * PAGE_SIZE).map(question => (
                <article className="card" key={question.id}>
                    <h2 className="card-title">#{question.id}: {question.questionText}</h2>
                    <ol type="A">
                        {[question.optionA, question.optionB, question.optionC, question.optionD].map((option, index) => (
                            <li key={index}>{option}</li>
                        ))}
                    </ol>
                    <p><strong>Correct answer:</strong> {question.correctAnswer}</p>
                    {question.explanation && <p><strong>Explanation:</strong> {question.explanation}</p>}
                    <div className="question-bank-actions">
                        <button type="button" aria-label={`Edit question ${question.id}`} disabled={editor !== null || deleting !== null}
                            onClick={() => { setSuccess(""); setEditor(question); onBusyChange(true); }}>Edit</button>
                        <button type="button" className="question-delete" aria-label={`Delete question ${question.id}`}
                            disabled={editor !== null || deleting !== null} onClick={() => {
                                setSuccess(""); setDeleteError(""); setDeleting(question);
                                onBusyChange(true);
                            }}>Delete</button>
                    </div>
                </article>
            ))}
        </div>
        {pageCount > 1 && <nav className="question-bank-pagination" aria-label="Question pages">
            <button type="button" disabled={currentPage === 1} onClick={() => setPage(currentPage - 1)}>Previous</button>
            <span>Page {currentPage} of {pageCount}</span>
            <button type="button" disabled={currentPage === pageCount} onClick={() => setPage(currentPage + 1)}>Next</button>
        </nav>}
    </>;
};

const QuestionBank = () => {
    const [busy, setBusy] = useState(false);
    const [success, setSuccess] = useState("");
    const [subjects, setSubjects] = useState<Subject[]>([]);
    const [subjectId, setSubjectId] = useState<number | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        let active = true;
        apiClient("/api/subjects").then((data: Subject[]) => {
            if (active) {
                setSubjects(data);
                setSubjectId(data[0]?.id ?? null);
                setIsLoading(false);
            }
        }).catch(() => {
            if (active) {
                setError("Failed to load subjects. Please try again.");
                setIsLoading(false);
            }
        });
        return () => { active = false; };
    }, []);

    return <div className="question-bank">
        <PageTitle title="Admin Question Bank" subtitle="Browse existing questions by subject. Search within the selected subject." />
        {success && <p role="status">{success}</p>}
        {isLoading ? <p role="status">Loading subjects...</p> : error ? <p role="alert">{error}</p> :
            subjects.length === 0 ? <p>No subjects available.</p> : <>
                <div className="question-bank-field">
                    <label htmlFor="question-subject">Subject</label>
                    <select id="question-subject" value={subjectId ?? ""} disabled={busy}
                        onChange={event => { setSuccess(""); setSubjectId(Number(event.target.value)); }}>
                        {subjects.map(subject => <option key={subject.id} value={subject.id}>
                            {subject.name} ({subject.questionCount})
                        </option>)}
                    </select>
                </div>
                {subjectId !== null && <SubjectQuestions key={subjectId} subjectId={subjectId} subjects={subjects}
                    onBusyChange={value => { setBusy(value); if (value) setSuccess(""); }}
                    onSavedElsewhere={(question, created) => {
                        setSuccess(`Question #${question.id} ${created ? "created" : "updated"} in ${question.subject.name}.`);
                        setSubjectId(question.subject.id);
                    }}
                    onCountChange={(id, delta) => setSubjects(previous => previous.map(subject =>
                        subject.id === id ? {...subject, questionCount: Math.max(0, subject.questionCount + delta)} : subject))} />}
            </>}
    </div>;
};

export default QuestionBank;
