import {ArrowRight, BookOpen} from "lucide-react";
import "../../styles/practice.css";
import {useNavigate} from "react-router-dom";
import {useEffect, useState} from "react";
import {useTranslation} from "react-i18next";
import type { Subject } from "../../types/subject.ts";
import {subjectIcons} from "../../types/subjects.ts";
import apiClient from "../../api/apiClient.ts";

const SubjectCards = () => {
    const { t } = useTranslation();
    const navigate = useNavigate();

    const [subjects, setSubjects] = useState<Subject[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {

        const fetchSubjects = async () => {

            try {
                setIsLoading(true);
                setError("");

                const data = await apiClient("/api/subjects");
                setSubjects(data);
            }
            catch (error) {

                console.error("Failed to fetch subjects: ", error);
                setError(t("practice.loadSubjectsError", { defaultValue: "Kunde inte ladda ämnen. Försök igen." }));
            }
            finally {
                setIsLoading(false);
            }
        };
        fetchSubjects();

    }, [t]);

    if (isLoading) {
        return <p>{t("practice.loadingSubjects", { defaultValue: "Laddar ämnen..." })}</p>;
    }

    if (error) {
        return <p>{error}</p>;
    }

    return (

        <section className="subject-cards">

            {subjects.map((subject) => {

                const Icon = subjectIcons[subject.name] ?? BookOpen;

                return (

                    <div className="subject-card" key={subject.id}>
                        <div className="subject-card-header">
                            <div className="subject-icon">
                                <Icon size={24} />
                            </div>
                            <div>
                                <h3>{subject.name}</h3>
                                <p>{subject.description}</p>
                            </div>
                        </div>

                        <div className="subject-card-footer">
                            <span>
                                {subject.questionCount} {t("practice.questionsCountLabel", { defaultValue: "frågor" })}
                            </span>
                            <button
                                onClick={() => navigate(`/practice/subjects/${subject.id}`)}
                            >
                                {t("practice.startPracticeButton", { defaultValue: "Börja öva" })}
                                <ArrowRight size={16} />
                            </button>
                        </div>
                    </div>
                );
            })}
        </section>
    );
};

export default SubjectCards;
