export const UserRole = {
    STUDENT: "STUDENT",
    ADMIN: "ADMIN",
} as const;

export interface CurrentUserState {
    user: User | null;
    isLoading: boolean;
    error: string;
}

export interface User {

    id: number;
    fullName: string;
    email: string;
    role: typeof UserRole[keyof typeof UserRole];
    createdAt: string;
}
