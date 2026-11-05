import {useState} from "react";
import "../../styles/question-image.css";
import {isQuestionImageUrl} from "../../utils/questionImageUrl";

function LoadableImage({url, questionText}: {url: string; questionText: string}) {
    const [failed, setFailed] = useState(false);
    if (failed) return <p className="question-image-unavailable" role="status">Question image unavailable.</p>;
    return <img className="question-image" src={url} alt={`Image for question: ${questionText}`}
        style={{ maxWidth: "100%", height: "auto", objectFit: "contain" }}
        onError={() => setFailed(true)} />;
}

export default function QuestionImage({imageUrl, questionText}: {
    imageUrl?: string | null; questionText: string;
}) {
    const url = imageUrl?.trim();
    if (!url || !isQuestionImageUrl(url)) return null;
    return <LoadableImage key={url} url={url} questionText={questionText} />;
}
