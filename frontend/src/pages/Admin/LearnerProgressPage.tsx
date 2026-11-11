import { useEffect, useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import PageTitle from "../../components/common/PageTitle";
import { getLearners } from "../../services/adminLearnerService";
import type { LearnerSummary } from "../../types/learnerProgress";
import "../../styles/admin.css";

const LearnerProgressPage = () => {
    const { t } = useTranslation();
    const location = useLocation();
    const navigate = useNavigate();
    const [learners, setLearners] = useState<LearnerSummary[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [loadFailed, setLoadFailed] = useState(false);

    // Name of the student we just deleted, passed via navigate(..., { state })
    // from DangerZone. Read once on mount; the state is cleared right after
    // so a refresh doesn't re-show the banner.
    const [deletedName] = useState<string | null>(
        (location.state as { deleted?: string } | null)?.deleted ?? null
    );

    useEffect(() => {
        if (deletedName) {
            navigate(location.pathname, { replace: true, state: {} });
        }
    }, [deletedName, navigate, location.pathname]);

    useEffect(() => {
        let active = true;
        getLearners()
            .then(data => { if (active) setLearners(data); })
            .catch(() => { if (active) setLoadFailed(true); })
            .finally(() => { if (active) setIsLoading(false); });
        return () => { active = false; };
    }, []);

    const admins = learners.filter(learner => learner.role === "ADMIN");
    const students = learners.filter(learner => learner.role === "STUDENT");

    const group = (id: string, heading: string, people: LearnerSummary[]) => (
        <section className="learner-group" aria-labelledby={id}>
            <h2 id={id}>{heading} ({people.length})</h2>
            <ul className="learner-list">
                {people.map(person => (
                    <li key={person.userId}>
                        <Link to={`/admin/learners/${person.userId}`}>{person.fullName}</Link>
                    </li>
                ))}
            </ul>
        </section>
    );

    return (
        <div className="learner-overview">
            <PageTitle title={t("learnerProgress.title")} subtitle={t("learnerProgress.listSubtitle")} />

            {deletedName && (
                <p className="success-banner" role="status">
                    {t("learnerProgress.list.deleteSuccess", { name: deletedName })}
                </p>
            )}

            {isLoading && <p role="status">{t("learnerProgress.loading")}</p>}
            {!isLoading && loadFailed && <p role="alert">{t("learnerProgress.loadFailed")}</p>}

            {!isLoading && !loadFailed && <>
                {group("learner-group-admins", t("learnerProgress.admins"), admins)}
                {students.length > 0
                    ? group("learner-group-students", t("learnerProgress.students"), students)
                    : <p>{t("learnerProgress.noStudents")}</p>}
            </>}
        </div>
    );
};

export default LearnerProgressPage;
