import {useEffect, useRef, useState} from "react";
import type {FormEvent} from "react";
import PageTitle from "../../components/common/PageTitle";
import type {Subject} from "../../types/subject";
import {createSubject, deleteSubject, listSubjects, subjectError, updateSubject} from "../../services/adminSubjectService";
import "../../styles/admin.css";

function SubjectForm({subject, onSaved, onCancel}: {
    subject?: Subject; onSaved: (saved: Subject) => void; onCancel: () => void;
}) {
    const [name, setName] = useState(subject?.name ?? "");
    const [description, setDescription] = useState(subject?.description ?? "");
    const [error, setError] = useState("");
    const [submitting, setSubmitting] = useState(false);
    const pending = useRef(false);

    async function submit(event: FormEvent<HTMLFormElement>) {
        event.preventDefault();
        if (pending.current) return;
        if (!name.trim()) { setError("Enter a subject name."); return; }
        if (!description.trim()) { setError("Enter a subject description."); return; }
        // Subject columns use JPA's default varchar(255); the DTO requires both fields.
        if (name.length > 255 || description.length > 255) {
            setError("Name and description must each be at most 255 characters."); return;
        }
        pending.current = true;
        setSubmitting(true);
        setError("");
        const request = {name: name.trim(), description: description.trim()};
        try {
            onSaved(subject ? await updateSubject(subject.id, request) : await createSubject(request));
        } catch (error: unknown) {
            setError(subjectError(error, "save"));
        } finally {
            pending.current = false;
            setSubmitting(false);
        }
    }

    return <form className="card question-editor" aria-labelledby="subject-editor-title" onSubmit={submit} noValidate>
        <h2 id="subject-editor-title">{subject ? `Edit subject: ${subject.name}` : "Create subject"}</h2>
        <fieldset disabled={submitting}>
            <div className="question-bank-field">
                <label htmlFor="subject-name">Name (required)</label>
                <input autoFocus id="subject-name" required maxLength={255} value={name} onChange={e => setName(e.target.value)} />
            </div>
            <div className="question-bank-field">
                <label htmlFor="subject-description">Description (required)</label>
                <textarea id="subject-description" required maxLength={255} value={description} onChange={e => setDescription(e.target.value)} />
            </div>
        </fieldset>
        {error && <p role="alert">{error}</p>}
        <div className="question-bank-actions">
            <button type="submit" disabled={submitting}>{submitting ? "Saving..." : "Save subject"}</button>
            <button type="button" disabled={submitting} onClick={onCancel}>Cancel</button>
        </div>
    </form>;
}

export default function Subjects() {
    const [subjects, setSubjects] = useState<Subject[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [revision, setRevision] = useState(0);
    const [editor, setEditor] = useState<Subject | "create" | null>(null);
    const [deleting, setDeleting] = useState<Subject | null>(null);
    const [deleteError, setDeleteError] = useState("");
    const [submitting, setSubmitting] = useState(false);
    const [success, setSuccess] = useState("");
    const pending = useRef(false);
    const returnFocus = useRef<HTMLButtonElement | null>(null);
    const createButton = useRef<HTMLButtonElement>(null);
    const busy = editor !== null || deleting !== null;

    useEffect(() => {
        let active = true;
        listSubjects().then(data => {
            if (active) { setSubjects(data); setLoading(false); }
        }).catch(error => {
            if (active) { setError(subjectError(error, "load")); setLoading(false); }
        });
        return () => { active = false; };
    }, [revision]);

    useEffect(() => {
        if (!busy && returnFocus.current) {
            (returnFocus.current.isConnected ? returnFocus.current : createButton.current)?.focus();
        }
    }, [busy]);

    async function confirmDelete() {
        if (!deleting || pending.current) return;
        pending.current = true;
        setSubmitting(true);
        setDeleteError("");
        try {
            await deleteSubject(deleting.id);
            setSubjects(previous => previous.filter(subject => subject.id !== deleting.id));
            setSuccess(`Subject "${deleting.name}" deleted.`);
            setDeleting(null);
        } catch (error: unknown) {
            setDeleteError(subjectError(error, "delete"));
        } finally {
            pending.current = false;
            setSubmitting(false);
        }
    }

    return <div className="question-bank">
        <PageTitle title="Manage Subjects" subtitle="Create and edit subjects used by the question bank and practice mode." />
        {success && <p role="status">{success}</p>}
        {loading ? <p role="status">Loading subjects...</p> : error ? <>
            <p role="alert">{error}</p>
            <div className="question-bank-actions"><button type="button" onClick={() => {
                setLoading(true); setError(""); setRevision(value => value + 1);
            }}>Retry loading subjects</button></div>
        </> : <>
            <div className="question-bank-actions">
                <button ref={createButton} type="button" disabled={busy} onClick={event => {
                    returnFocus.current = event.currentTarget; setSuccess(""); setEditor("create");
                }}>Create subject</button>
                <button type="button" disabled={busy} onClick={() => {
                    setSuccess(""); setLoading(true); setRevision(value => value + 1);
                }}>Refresh subject list</button>
            </div>
            {editor !== null && <SubjectForm subject={editor === "create" ? undefined : editor} onCancel={() => setEditor(null)}
                onSaved={saved => {
                    setSubjects(previous => editor === "create" ? [...previous, saved] : previous.map(subject => subject.id === saved.id ? saved : subject));
                    setSuccess(`Subject "${saved.name}" ${editor === "create" ? "created" : "updated"}.`);
                    setEditor(null);
                }} />}
            {deleting && <section className="card question-delete-confirmation" role="region" aria-labelledby="delete-subject-title">
                <h2 id="delete-subject-title">Delete subject "{deleting.name}"?</h2>
                <p>This permanently deletes the subject. This action cannot be undone. Subjects used by questions or practice results cannot currently be deleted.</p>
                {deleteError && <p role="alert">{deleteError}</p>}
                <div className="question-bank-actions">
                    <button autoFocus type="button" disabled={submitting} onClick={() => setDeleting(null)}>Cancel deletion</button>
                    <button type="button" className="question-delete" disabled={submitting} onClick={confirmDelete}>
                        {submitting ? "Deleting..." : "Confirm deletion"}</button>
                </div>
            </section>}
            {subjects.length === 0 && <p>No subjects available. Create a subject to get started.</p>}
            <div className="question-bank-list">{subjects.map(subject => <article className="card" key={subject.id}>
                <h2 className="card-title">{subject.name}</h2>
                <p>{subject.description}</p>
                <p>{subject.questionCount} questions</p>
                <div className="question-bank-actions">
                    <button type="button" disabled={busy} aria-label={`Edit subject ${subject.name}`} onClick={event => {
                        returnFocus.current = event.currentTarget; setSuccess(""); setEditor(subject);
                    }}>Edit</button>
                    <button type="button" disabled={busy} className="question-delete" aria-label={`Delete subject ${subject.name}`} onClick={event => {
                        returnFocus.current = event.currentTarget; setSuccess(""); setDeleteError(""); setDeleting(subject);
                    }}>Delete</button>
                </div>
            </article>)}</div>
        </>}
    </div>;
}
