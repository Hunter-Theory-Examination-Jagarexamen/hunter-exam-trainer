export interface ProgressPoint {
    date: string;
    percentage: number;
    mastered: number;
    total: number;
    /** The student answered a counted question that day (weekly point: that week). */
    practised: boolean;
}
