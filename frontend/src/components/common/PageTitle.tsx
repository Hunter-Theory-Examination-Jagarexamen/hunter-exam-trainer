
interface PageTitleProps {
    title: string;
    subtitle?: string;
}
const PageTitle = ({ title, subtitle }: PageTitleProps) => {
    return (
        <div>
            <h1>{title}</h1>

            {subtitle && (
                <p>{subtitle}</p>
            )}
        </div>
    );
};

export default PageTitle;