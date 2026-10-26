import {useEffect, useState} from "react";
import {Link} from "react-router-dom";
import PageTitle from "../../components/common/PageTitle";
import {adminStatsError, getAdminStats} from "../../services/adminStatsService";
import type {AdminStats} from "../../types/adminStats";
import "../../styles/admin.css";

// The three admin tools, each rendered as a large card on /admin.
const CARDS: {to: string; title: string; caption: string; count: (stats: AdminStats) => number}[] = [
    {to: "/admin/questions", title: "Questions", caption: "Open the question bank", count: stats => stats.questionCount},
    {to: "/admin/subjects", title: "Subjects", caption: "Open subject management", count: stats => stats.subjectCount},
    {to: "/admin/learners", title: "Learners", caption: "Open learner progress", count: stats => stats.studentCount},
];

// Start page of the admin area (/admin): overview cards with the backend counts.
const AdminHome = () => {
    const [stats, setStats] = useState<AdminStats | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState("");

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
            <PageTitle title="Admin" subtitle="Choose an admin tool." />

            {isLoading && <p role="status">Loading admin overview...</p>}

            {/* No cards while counts are unknown, so a failed request never shows a fabricated 0. */}
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
