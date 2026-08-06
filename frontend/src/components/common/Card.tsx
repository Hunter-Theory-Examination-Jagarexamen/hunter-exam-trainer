import * as React from "react";
import "../../styles/card.css";

interface CardProps {
    title: string;
    children: React.ReactNode;
}

const Card = ({ title, children }: CardProps) => {
    return (
        <div className="card">
            <h3 className="card-title">
                {title}
            </h3>

            <div className="card-content">
                {children}
            </div>
        </div>
    );
};

export default Card;