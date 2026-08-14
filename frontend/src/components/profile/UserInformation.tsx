import "../../styles/profile.css";

const user = {
    name: "John Joseph",
    email: "john.joseph@example.com",
    role: "Student",
    memberSince: "August 2026"
}

const UserInformation = () => {
    return (
        <section className="profile-section">
            <h2>User Information</h2>

            <div className="profile-card">
                <div className="profile-row">
                    <span>Name</span>
                    <strong>{user.name}</strong>
                </div>
                <div className="profile-row">
                    <span>Email</span>
                    <strong>{user.email}</strong>
                </div>
                <div className="profile-row">
                    <span>Role</span>
                    <strong>{user.role}</strong>
                </div>
                <div className="profile-row">
                    <span>Member Since</span>
                    <strong>{user.memberSince}</strong>
                </div>
            </div>
        </section>
    );
};

export default UserInformation;