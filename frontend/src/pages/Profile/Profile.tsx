import { useTranslation } from "react-i18next";
import PageTitle from "../../components/common/PageTitle";
import "../../styles/profile.css";
import UserInformation from "../../components/profile/UserInformation";
import AccountSettings from "../../components/profile/AccountSettings";
import LegalLinks from "../../components/legal/LegalLinks";

const Profile = () => {
    const { t } = useTranslation();

    return (
        <div className="profile">
            <PageTitle
                title={t("profile.title")}
                subtitle={t("profile.subtitle")}
            />
            <UserInformation />
            <AccountSettings />
            <LegalLinks />
        </div>
    );
};

export default Profile;
