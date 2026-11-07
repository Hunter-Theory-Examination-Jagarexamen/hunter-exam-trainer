import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { useTranslation } from "react-i18next";
import PageTitle from "../../components/common/PageTitle";
import LearnerProgressCard from "../../components/admin/LearnerProgressCard";
import { getLearnerProgress } from "../../services/adminLearnerService";
import { errorStatus } from "../../services/apiErrors";
import type { LearnerProgress } from "../../types/learnerProgress";

// /admin/learners/:userId — one user's progress (student or admin).
const LearnerDetailPage = () => {
    const { t } = useTranslation();
    // The id from the address, e.g. "7" in /admin/learners/7.
    const { userId } = useParams();
    const [learner, setLearner] = useState<LearnerProgress | null>(null);
    const [error, setError] = useState("");

    useEffect(() => {
        let active = true;
        getLearnerProgress(Number(userId))
            .then(data => { if (active) setLearner(data); })
            .catch((failure: unknown) => {
                if (active) setError(t(errorStatus(failure) === 404
                    ? "learnerProgress.notFound" : "learnerProgress.loadFailed"));
            });
        return () => { active = false; };
    }, [userId, t]);

    return (
        <div className="statistics">
            <p><Link to="/admin/learners">{t("learnerProgress.backToList")}</Link></p>

            {!learner && !error && <p role="status">{t("learnerProgress.loadingOne")}</p>}
            {error && <p role="alert">{error}</p>}

            {learner && <>
                <PageTitle title={learner.fullName} subtitle={learner.email} />
                <LearnerProgressCard learner={learner} />
            </>}
        </div>
    );
};

export default LearnerDetailPage;
