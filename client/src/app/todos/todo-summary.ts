export interface TodoSummary {
    percentToDosComplete: number;
    categoriesPercentComplete: Map<string, number>;
    ownersPercentComplete: Map<string, number>;
}