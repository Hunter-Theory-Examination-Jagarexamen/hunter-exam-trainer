import "../../styles/practice.css";
import {practiceCards} from "../../types/practiceCards.ts";
import {Check} from "lucide-react";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";

const PracticeCards = () => {
    const { t } = useTranslation();
    const navigate = useNavigate();

    return (
        <section className="practice-cards">

            {practiceCards.map((card) => (
                <div className="practice-card" key={card.title}>

                    <div className={`icon-circle icon-circle--${card.iconVariant}`}>
                        <card.icon size={28} />
                    </div>

                    <div className="practice-title">
                        <h3>{t(card.title, { defaultValue: card.title })}</h3>
                        <p>{t(card.description, { defaultValue: card.description })}</p>
                    </div>

                    <ul className="practice-features">
                        {card.features.map((feature) => (
                            <li key={feature}>
                                <Check size={16} className="check-icon" />
                                <span>{t(feature, { defaultValue: feature })}</span>
                            </li>
                        ))}
                    </ul>

                    <button
                        className="practice-button"
                        onClick={() => navigate(card.route)}
                    >
                        {t(card.buttonLabel, { defaultValue: card.buttonLabel })}
                    </button>

                </div>
            ))}
        </section>
    );
};

export default PracticeCards;
