// Configures i18next for frontend localization
import i18n from 'i18next';
import { initReactI18next } from "react-i18next";
import sv from './locales/sv.json'
import en from './locales/en.json'
// Long texts live in their own namespace files ("legal" for Terms of Use and
// Privacy Policy, #212); pages read them with useTranslation("legal").
import svLegal from './locales/legal/sv.json'
import enLegal from './locales/legal/en.json'

// Initialize i18next with default Swedish language
i18n.use(initReactI18next).init({
    resources: {
        sv: { translation: sv, legal: svLegal },
        en: { translation: en, legal: enLegal },
    },
    // Låt alltid språket vara svenska, även i testmiljö
    lng: 'sv',
    fallbackLng: 'sv',
    interpolation: {
        escapeValue: false,
    },
})

export default i18n;
