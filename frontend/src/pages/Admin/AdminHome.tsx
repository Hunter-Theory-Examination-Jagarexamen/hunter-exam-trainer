import { Link } from "react-router-dom";
import PageTitle from "../../components/common/PageTitle";

// Start page of the admin area (/admin). The overview with counts comes in #123.
const AdminHome = () => {
    return (
        <>
            <PageTitle title="Admin" subtitle="Choose an admin tool." />

            <ul>
                <li><Link to="/admin/questions">Questions</Link></li>
                <li><Link to="/admin/subjects">Subjects</Link></li>
                <li><Link to="/admin/learners">Learners</Link></li>
            </ul>
        </>
    );
};

export default AdminHome;
