import "../../styles/practice.css";
import {practiceCards} from "../../types/practiceCards.ts";
import {Check} from "lucide-react";
import { useNavigate } from "react-router-dom";

const PracticeCards = () => {

    const navigate = useNavigate();

    return (
        <section className="practice-cards">

            {practiceCards.map((card) => (
                <div className="practice-card" key={card.title}>

                    <div className={`icon-circle icon-circle--${card.iconVariant}`}>
                        <card.icon size={28} />
                    </div>

                    <div className="practice-title">
                        <h3>{card.title}</h3>
                        <p>{card.description}</p>
                    </div>

                    <ul className="practice-features">
                        {card.features.map((feature) => (
                            <li key={feature}>
                                <Check size={16} className="check-icon" />
                                <span>{feature}</span>
                            </li>
                        ))}
                    </ul>

                    <button
                        className="practice-button"
                        onClick={() => navigate(card.route)}
                    >
                        {card.buttonLabel}
                    </button>

                </div>
            ))}
        </section>
    );
};

export default PracticeCards;