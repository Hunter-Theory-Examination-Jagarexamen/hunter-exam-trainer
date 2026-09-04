import {ArrowRight, BookOpen} from "lucide-react";
import "../../styles/practice.css";
import {useNavigate} from "react-router-dom";
import {useEffect, useState} from "react";
import type { Subject } from "../../types/subject.ts";
import {subjectIcons} from "../../types/subjects.ts";
import apiClient from "../../api/apiClient.ts";

const SubjectCards = () => {

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
                setError("Failed to load subjects. Please try again");
            }
            finally {
                setIsLoading(false);
            }
        };
        fetchSubjects();

    }, []);

    if (isLoading) {
        return <p>Loading Subjects...</p>;
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
                                Start practicing
                            </span>
                            <button
                                onClick={() => navigate(`/practice/subjects/${subject.id}`)}
                            >
                                Start Practice
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