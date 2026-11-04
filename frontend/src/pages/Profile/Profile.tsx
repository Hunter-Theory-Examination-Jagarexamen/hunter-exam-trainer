import PageTitle from "../../components/common/PageTitle";
import "../../styles/profile.css";
import UserInformation from "../../components/profile/UserInformation";
import AccountSettings from "../../components/profile/AccountSettings";

const Profile = () => {
    return (
        <div className="profile">
            <PageTitle
                title="Profil"
                subtitle="Hantera din kontoinformation och inställningar."
            />
            <UserInformation />
            <AccountSettings />
        </div>
    );
};

export default Profile;
