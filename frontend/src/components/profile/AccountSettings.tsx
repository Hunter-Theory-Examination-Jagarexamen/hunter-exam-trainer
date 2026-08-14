import "../../styles/profile.css";
import {Lock} from "lucide-react";

const AccountSettings = () => {
    return (
        <section className="profile-section">
            <h2>Account Settings</h2>

            <div className="account-settings">
                <button>
                    <Lock size={16}/>
                    Change Password
                </button>
            </div>
        </section>
    );
};

export default AccountSettings;