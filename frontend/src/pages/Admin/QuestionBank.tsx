import {useEffect, useState} from "react";
import apiClient from "../../api/apiClient";
import PageTitle from "../../components/common/PageTitle";
import type {Subject} from "../../types/subject";
import type {Question} from "../../types/question";
import "../../styles/admin.css";

const PAGE_SIZE = 20;

const SubjectQuestions = ({subjectId}: {subjectId: number}) => {
    const [questions, setQuestions] = useState<Question[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState("");
    const [search, setSearch] = useState("");
    const [page, setPage] = useState(1);

    useEffect(() => {
        let active = true;
        apiClient(`/api/questions?subjectId=${subjectId}`).then((data: Question[]) => {
            if (active) {
                setQuestions(data);
                setIsLoading(false);
            }
        }).catch(() => {
            if (active) {
                setError("Failed to load questions. Please try again.");
                setIsLoading(false);
            }
        });
        return () => { active = false; };
    }, [subjectId]);

    if (isLoading) return <p role="status">Loading questions...</p>;
    if (error) return <p role="alert">{error}</p>;
    if (questions.length === 0) return <p>No questions available for this subject.</p>;

    const term = search.trim().toLocaleLowerCase();
    const filtered = questions.filter(question => [
        question.id.toString(), question.questionText,
        question.optionA, question.optionB, question.optionC, question.optionD,
        question.explanation,
    ].some(value => value?.toLocaleLowerCase().includes(term)));
    const pageCount = Math.ceil(filtered.length / PAGE_SIZE);

    return <>
        <div className="question-bank-field">
            <label htmlFor="question-search">Search questions</label>
            <input id="question-search" type="search" value={search}
                placeholder="Search by ID, question, answer or explanation"
                onChange={event => { setSearch(event.target.value); setPage(1); }} />
        </div>
        <p role="status">{filtered.length} of {questions.length} questions</p>
        {filtered.length === 0 && <p>No questions match your search.</p>}
        <div className="question-bank-list">
            {filtered.slice((page - 1) * PAGE_SIZE, page * PAGE_SIZE).map(question => (
                <article className="card" key={question.id}>
                    <h2 className="card-title">#{question.id}: {question.questionText}</h2>
                    <ol type="A">
                        {[question.optionA, question.optionB, question.optionC, question.optionD].map((option, index) => (
                            <li key={index}>{option}</li>
                        ))}
                    </ol>
                    <p><strong>Correct answer:</strong> {question.correctAnswer}</p>
                    {question.explanation && <p><strong>Explanation:</strong> {question.explanation}</p>}
                </article>
            ))}
        </div>
        {pageCount > 1 && <nav className="question-bank-pagination" aria-label="Question pages">
            <button type="button" disabled={page === 1} onClick={() => setPage(page - 1)}>Previous</button>
            <span>Page {page} of {pageCount}</span>
            <button type="button" disabled={page === pageCount} onClick={() => setPage(page + 1)}>Next</button>
        </nav>}
    </>;
};

const QuestionBank = () => {
    const [subjects, setSubjects] = useState<Subject[]>([]);
    const [subjectId, setSubjectId] = useState<number | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        let active = true;
        apiClient("/api/subjects").then((data: Subject[]) => {
            if (active) {
                setSubjects(data);
                setSubjectId(data[0]?.id ?? null);
                setIsLoading(false);
            }
        }).catch(() => {
            if (active) {
                setError("Failed to load subjects. Please try again.");
                setIsLoading(false);
            }
        });
        return () => { active = false; };
    }, []);

    return <div className="question-bank">
        <PageTitle title="Admin Question Bank" subtitle="Browse existing questions by subject. Search within the selected subject." />
        {isLoading ? <p role="status">Loading subjects...</p> : error ? <p role="alert">{error}</p> :
            subjects.length === 0 ? <p>No subjects available.</p> : <>
                <div className="question-bank-field">
                    <label htmlFor="question-subject">Subject</label>
                    <select id="question-subject" value={subjectId ?? ""}
                        onChange={event => setSubjectId(Number(event.target.value))}>
                        {subjects.map(subject => <option key={subject.id} value={subject.id}>
                            {subject.name} ({subject.questionCount})
                        </option>)}
                    </select>
                </div>
                {subjectId !== null && <SubjectQuestions key={subjectId} subjectId={subjectId} />}
            </>}
    </div>;
};

export default QuestionBank;
