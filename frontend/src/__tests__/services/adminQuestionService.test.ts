import {afterEach, describe, expect, it, vi} from "vitest";
import {createQuestion, updateQuestion, deleteQuestion, type QuestionRequest} from "../../services/adminQuestionService";

const request: QuestionRequest = {questionText: "Which animal?", optionA: "Moose", optionB: "Deer",
    optionC: "Fox", optionD: "Bear", correctAnswer: "Fox", explanation: "", subjectId: 1};

afterEach(() => {
    vi.unstubAllGlobals();
    localStorage.clear();
});

describe("Admin question service HTTP contract", () => {
    it.each(["create", "update", "delete"] as const)("uses the existing authenticated HTTP client for %s", async mode => {
        localStorage.setItem("token", "admin-token");
        const response = {...request, id: 7, subject: {id: 1, name: "Wildlife", description: ""}};
        const fetchMock = vi.fn().mockResolvedValue(mode === "delete" ? new Response(null, {status: 204}) :
            new Response(JSON.stringify(response), {status: mode === "create" ? 201 : 200,
                headers: {"Content-Type": "application/json"}}));
        vi.stubGlobal("fetch", fetchMock);
        const result = mode === "create" ? await createQuestion(request) : mode === "update"
            ? await updateQuestion(7, request) : await deleteQuestion(7);
        expect(fetchMock).toHaveBeenCalledTimes(1);
        const [url, options] = fetchMock.mock.calls[0];
        expect(url).toBe(`${import.meta.env.VITE_API_BASE_URL || "http://localhost:8080"}/api/admin/questions${mode === "create" ? "" : "/7"}`);
        expect(options.method).toBe(mode === "create" ? "POST" : mode === "update" ? "PUT" : "DELETE");
        expect(options.headers.get("Authorization")).toBe("Bearer admin-token");
        if (mode === "delete") {
            expect(options.body).toBeUndefined();
            expect(result).toBeUndefined();
        } else {
            expect(JSON.parse(options.body)).toEqual(request);
            expect(result).toEqual(response);
        }
    });
});
