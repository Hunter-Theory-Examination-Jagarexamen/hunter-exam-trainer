import {useEffect, useState} from "react";
import {Link} from "react-router-dom";
import {useTranslation} from "react-i18next";
import PageTitle from "../../components/common/PageTitle";
import {adminStatsError, getAdminStats} from "../../services/adminStatsService";
import type {AdminStats} from "../../types/adminStats";
import "../../styles/admin.css";

const AdminHome = () => {
    const {t} = useTranslation();
    const [stats, setStats] = useState<AdminStats | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState("");

    const CARDS: {to: string; title: string; caption: string; count: (stats: AdminStats) => number}[] = [
        {to: "/admin/questions", title: t("admin.cards.questions.title"), caption: t("admin.cards.questions.caption"), count: stats => stats.questionCount},
        {to: "/admin/subjects", title: t("admin.cards.subjects.title"), caption: t("admin.cards.subjects.caption"), count: stats => stats.subjectCount},
        {to: "/admin/learners", title: t("admin.cards.learners.title"), caption: t("admin.cards.learners.caption"), count: stats => stats.studentCount},
    ];

    useEffect(() => {
        let active = true;
        getAdminStats().then(data => {
            if (active) {
                setStats(data);
                setIsLoading(false);
            }
        }).catch((failure: unknown) => {
            if (active) {
                setError(adminStatsError(failure));
                setIsLoading(false);
            }
        });
        return () => { active = false; };
    }, []);

    return (
        <div className="admin-overview">
            <PageTitle title={t("admin.title")} subtitle={t("admin.subtitle")} />

            {isLoading && <p role="status">{t("admin.loadingOverview")}</p>}

            {!isLoading && error && <p role="alert">{error}</p>}

            {!isLoading && !error && stats && (
                <ul className="admin-overview-cards">
                    {CARDS.map(card => (
                        <li key={card.to}>
                            <Link className="card admin-overview-card" to={card.to}>
                                <h2 className="card-title">{card.title}</h2>
                                <span className="admin-overview-card-count">{card.count(stats)}</span>
                                <span className="admin-overview-card-caption">{card.caption}</span>
                            </Link>
                        </li>
                    ))}
                </ul>
            )}
        </div>
    );
};

export default AdminHome;
