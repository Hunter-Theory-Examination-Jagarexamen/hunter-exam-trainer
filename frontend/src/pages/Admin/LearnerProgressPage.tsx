import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
import PageTitle from "../../components/common/PageTitle";
import { getLearners } from "../../services/adminLearnerService";
import type { LearnerSummary } from "../../types/learnerProgress";
import "../../styles/admin.css";

// /admin/learners — every user by name: admins first, then students (the backend
// sends them in that order). A name opens that user's progress.
const LearnerProgressPage = () => {
    const { t } = useTranslation();
    const [learners, setLearners] = useState<LearnerSummary[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [loadFailed, setLoadFailed] = useState(false);

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

    // One labelled group; the heading's id names the list for screen readers.
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
