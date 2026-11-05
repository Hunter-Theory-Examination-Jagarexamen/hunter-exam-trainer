import {useEffect, useRef, useState} from "react";
import type {FormEvent} from "react";
import PageTitle from "../../components/common/PageTitle";
import type {Subject} from "../../types/subject";
import {createSubject, deleteSubject, listSubjects, subjectError, updateSubject} from "../../services/adminSubjectService";
import "../../styles/admin.css";
import {useTranslation} from "react-i18next";

function SubjectForm({subject, onSaved, onCancel}: {
    subject?: Subject; onSaved: (saved: Subject) => void; onCancel: () => void;
}) {
    const {t} = useTranslation();
    const [name, setName] = useState(subject?.name ?? "");
    const [description, setDescription] = useState(subject?.description ?? "");
    const [error, setError] = useState("");
    const [submitting, setSubmitting] = useState(false);
    const pending = useRef(false);

    async function submit(event: FormEvent<HTMLFormElement>) {
        event.preventDefault();
        if (pending.current) return;
        if (!name.trim()) { setError(t("subjects.editor.errors.missingName")); return; }
        if (!description.trim()) { setError(t("subjects.editor.errors.missingDescription")); return; }
        // Subject columns use JPA's default varchar(255); the DTO requires both fields.
        if (name.length > 255 || description.length > 255) {
            setError(t("subjects.editor.errors.tooLong")); return;
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
        <h2 id="subject-editor-title">{subject ? t("subjects.editor.editTitle", {name: subject.name}) : t("subjects.editor.createTitle")}</h2>
        <fieldset disabled={submitting}>
            <div className="question-bank-field">
                <label htmlFor="subject-name">{t("subjects.editor.nameLabel")}</label>
                <input autoFocus id="subject-name" required maxLength={255} value={name} onChange={e => setName(e.target.value)} />
            </div>
            <div className="question-bank-field">
                <label htmlFor="subject-description">{t("subjects.editor.descriptionLabel")}</label>
                <textarea id="subject-description" required maxLength={255} value={description} onChange={e => setDescription(e.target.value)} />
            </div>
        </fieldset>
        {error && <p role="alert">{error}</p>}
        <div className="question-bank-actions">
            <button type="submit" disabled={submitting}>{submitting ? t("subjects.saving") : t("subjects.saveSubject")}</button>
            <button type="button" disabled={submitting} onClick={onCancel}>{t("subjects.cancel")}</button>
        </div>
    </form>;
}

export default function Subjects() {
    const {t} = useTranslation();
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
            setSuccess(t("subjects.successDeleted", {name: deleting.name}));
            setDeleting(null);
        } catch (error: unknown) {
            setDeleteError(subjectError(error, "delete"));
        } finally {
            pending.current = false;
            setSubmitting(false);
        }
    }

    return <div className="question-bank">
        <PageTitle title={t("subjects.title")} subtitle={t("subjects.subtitle")} />
        {success && <p role="status">{success}</p>}
        {loading ? <p role="status">{t("subjects.loading")}</p> : error ? <>
            <p role="alert">{error}</p>
            <div className="question-bank-actions"><button type="button" onClick={() => {
                setLoading(true); setError(""); setRevision(value => value + 1);
            }}>{t("subjects.retry")}</button></div>
        </> : <>
            <div className="question-bank-actions">
                <button ref={createButton} type="button" disabled={busy} onClick={event => {
                    returnFocus.current = event.currentTarget; setSuccess(""); setEditor("create");
                }}>{t("subjects.createSubject")}</button>
                <button type="button" disabled={busy} onClick={() => {
                    setSuccess(""); setLoading(true); setRevision(value => value + 1);
                }}>{t("subjects.refreshList")}</button>
            </div>
            {editor !== null && <SubjectForm subject={editor === "create" ? undefined : editor} onCancel={() => setEditor(null)}
                                             onSaved={saved => {
                                                 setSubjects(previous => editor === "create" ? [...previous, saved] : previous.map(subject => subject.id === saved.id ? saved : subject));
                                                 setSuccess(t(editor === "create" ? "subjects.successCreated" : "subjects.successUpdated", {name: saved.name}));
                                                 setEditor(null);
                                             }} />}
            {deleting && <section className="card question-delete-confirmation" role="region" aria-labelledby="delete-subject-title">
                <h2 id="delete-subject-title">{t("subjects.deleteTitle", {name: deleting.name})}</h2>
                <p>{t("subjects.deleteWarning")}</p>
                {deleteError && <p role="alert">{deleteError}</p>}
                <div className="question-bank-actions">
                    <button autoFocus type="button" disabled={submitting} onClick={() => setDeleting(null)}>{t("subjects.cancelDeletion")}</button>
                    <button type="button" className="question-delete" disabled={submitting} onClick={confirmDelete}>
                        {submitting ? t("subjects.deleting") : t("subjects.confirmDeletion")}</button>
                </div>
            </section>}
            {subjects.length === 0 && <p>{t("subjects.noSubjects")}</p>}
            <div className="question-bank-list">{subjects.map(subject => <article className="card" key={subject.id}>
                <h2 className="card-title">{subject.name}</h2>
                <p>{subject.description}</p>
                <p>{t("subjects.questionsCount", {count: subject.questionCount})}</p>
                <div className="question-bank-actions">
                    <button type="button" disabled={busy} aria-label={t("subjects.editAria", {name: subject.name})} onClick={event => {
                        returnFocus.current = event.currentTarget; setSuccess(""); setEditor(subject);
                    }}>{t("subjects.edit")}</button>
                    <button type="button" disabled={busy} className="question-delete" aria-label={t("subjects.deleteAria", {name: subject.name})} onClick={event => {
                        returnFocus.current = event.currentTarget; setSuccess(""); setDeleteError(""); setDeleting(subject);
                    }}>{t("subjects.delete")}</button>
                </div>
            </article>)}</div>
        </>}
    </div>;
}
