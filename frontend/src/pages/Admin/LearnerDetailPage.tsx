import { useEffect, useState } from "react";
import { Link, useParams, useOutletContext } from "react-router-dom";
import { useTranslation } from "react-i18next";
import PageTitle from "../../components/common/PageTitle";
import LearnerProgressCard from "../../components/admin/LearnerProgressCard";
import DangerZone from "../../components/admin/DangerZone";
import RoleChangeSection from "../../components/admin/RoleChangeSection";
import { getLearnerProgress } from "../../services/adminLearnerService";
import { errorStatus } from "../../services/apiErrors";
import type { CurrentUserState } from "../../types/user";
import type { LearnerProgress } from "../../types/learnerProgress";

const LearnerDetailPage = () => {
    const { t } = useTranslation();
    const { userId } = useParams();
    const { user: currentUser } = useOutletContext<CurrentUserState>();
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

    const handleRoleChanged = (role: LearnerProgress["role"]) => {
        setLearner(previous => previous ? { ...previous, role } : previous);
    };

    return (
        <div className="statistics">
            <p><Link to="/admin/learners">{t("learnerProgress.backToList")}</Link></p>

            {!learner && !error && <p role="status">{t("learnerProgress.loadingOne")}</p>}
            {error && <p role="alert">{error}</p>}

            {learner && <>
                <PageTitle title={learner.fullName} subtitle={learner.email} />
                <LearnerProgressCard learner={learner} />
                <RoleChangeSection
                    userId={learner.userId}
                    userName={learner.fullName}
                    role={learner.role}
                    isSelf={currentUser?.id === learner.userId}
                    onRoleChanged={handleRoleChanged}
                />
                {learner.role === "STUDENT" && (
                    <DangerZone userId={learner.userId} userName={learner.fullName} />
                )}
            </>}
        </div>
    );
};

export default LearnerDetailPage;
