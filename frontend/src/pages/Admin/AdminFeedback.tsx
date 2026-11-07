import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import ReportedQuestion from "../../components/feedback/ReportedQuestion";
import PageTitle from "../../components/common/PageTitle";
import { listFeedback, setFeedbackHandled } from "../../services/feedbackService";
import type { FeedbackItem, FeedbackType } from "../../types/feedback";
import "../../styles/feedback.css";

// Every key is written in full, so a text search finds it (see the design note
// on translation keys).
const TYPE_LABEL_KEYS = {
    QUESTION: "feedback.types.QUESTION",
    APP: "feedback.types.APP",
    OTHER: "feedback.types.OTHER",
} as const;

const TYPES: FeedbackType[] = ["QUESTION", "APP", "OTHER"];

// Admin page: feedback and question reports from students, newest first.
// Unhandled items only by default; admins can show handled ones too.
const AdminFeedback = () => {
    const { t } = useTranslation();
    const [items, setItems] = useState<FeedbackItem[]>([]);
    const [typeFilter, setTypeFilter] = useState<FeedbackType | "">("");
    const [showHandled, setShowHandled] = useState(false);
    const [isLoading, setIsLoading] = useState(true);
    const [loadFailed, setLoadFailed] = useState(false);
    const [updateFailed, setUpdateFailed] = useState(false);

    // A filter change shows "loading" right away (in the event handler, not in the
    // effect: setting state directly in an effect makes React draw twice).
    const changeFilter = (change: () => void) => {
        setIsLoading(true);
        setLoadFailed(false);
        change();
    };

    // Loads again whenever a filter changes.
    useEffect(() => {
        let cancelled = false;
        listFeedback({
            type: typeFilter || undefined,
            // Showing handled items means "any"; otherwise only unhandled ones.
            handled: showHandled ? undefined : false,
        })
            .then(data => { if (!cancelled) setItems(data); })
            .catch(() => { if (!cancelled) setLoadFailed(true); })
            .finally(() => { if (!cancelled) setIsLoading(false); });
        // A slow answer for an old filter must not overwrite the newer one.
        return () => { cancelled = true; };
    }, [typeFilter, showHandled]);

    const toggleHandled = async (item: FeedbackItem) => {
        setUpdateFailed(false);
        try {
            const updated = await setFeedbackHandled(item.id, !item.handled);
            setItems(current => showHandled
                ? current.map(other => other.id === updated.id ? updated : other)
                // When only unhandled items are shown, a handled item leaves the list.
                : current.filter(other => other.id !== updated.id));
        } catch {
            setUpdateFailed(true);
        }
    };

    return (
        <div className="admin-feedback">
            <PageTitle title={t("feedback.admin.title")} subtitle={t("feedback.admin.subtitle")} />

            <div className="admin-feedback-filters">
                <label htmlFor="feedback-type-filter">{t("feedback.admin.typeFilter")}</label>
                <select id="feedback-type-filter" value={typeFilter}
                        onChange={event => changeFilter(() => setTypeFilter(event.target.value as FeedbackType | ""))}>
                    <option value="">{t("feedback.admin.allTypes")}</option>
                    {TYPES.map(type => (
                        <option key={type} value={type}>{t(TYPE_LABEL_KEYS[type])}</option>
                    ))}
                </select>
                <label className="admin-feedback-checkbox">
                    <input type="checkbox" checked={showHandled}
                           onChange={event => changeFilter(() => setShowHandled(event.target.checked))} />
                    {t("feedback.admin.showHandled")}
                </label>
            </div>

            {updateFailed && <p className="feedback-error" role="alert">{t("feedback.admin.updateFailed")}</p>}

            {isLoading && <p role="status">{t("feedback.admin.loading")}</p>}
            {!isLoading && loadFailed && <p className="feedback-error" role="alert">{t("feedback.admin.loadFailed")}</p>}
            {!isLoading && !loadFailed && items.length === 0 && <p>{t("feedback.admin.empty")}</p>}

            {!isLoading && !loadFailed && items.length > 0 && (
                <ul className="admin-feedback-list">
                    {items.map(item => (
                        <li key={item.id} className={`admin-feedback-item${item.handled ? " is-handled" : ""}`}>
                            <div className="admin-feedback-meta">
                                <strong>{t(TYPE_LABEL_KEYS[item.type])}</strong>
                                <span>{new Date(item.createdAt).toLocaleString("sv-SE")}</span>
                                {item.handled && <span>{t("feedback.admin.handled")}</span>}
                            </div>

                            {item.questionText && (
                                <>
                                    <p className="admin-feedback-question">
                                        <strong>{t("feedback.admin.question")}</strong> {item.questionText}
                                        {item.questionId === null &&
                                            <em> {t("feedback.admin.questionDeleted")}</em>}
                                    </p>
                                    {item.questionId !== null && <ReportedQuestion questionId={item.questionId} />}
                                </>
                            )}

                            <p className="admin-feedback-text">{item.text}</p>

                            <button type="button" className="practice-secondary-button"
                                    onClick={() => void toggleHandled(item)}>
                                {item.handled ? t("feedback.admin.markUnhandled") : t("feedback.admin.markHandled")}
                            </button>
                        </li>
                    ))}
                </ul>
            )}
        </div>
    );
};

export default AdminFeedback;
