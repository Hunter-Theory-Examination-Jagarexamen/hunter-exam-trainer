import {afterEach, describe, expect, it, vi} from "vitest";
import {createSubject, deleteSubject, listSubjects, subjectError, updateSubject} from "../../services/adminSubjectService";

const request = {name: "Wildlife", description: "Animals"};
const subject = {...request, id: 7, questionCount: 0};
afterEach(() => { vi.unstubAllGlobals(); localStorage.clear(); });

describe("Subject HTTP contract", () => {
    it.each(["list", "create", "update", "delete"] as const)("uses authenticated client and backend contract for %s", async mode => {
        localStorage.setItem("token", "admin-token");
        const response = mode === "list" ? [subject] : subject;
        const fetchMock = vi.fn().mockResolvedValue(mode === "delete" ? new Response(null, {status: 204}) :
            new Response(JSON.stringify(response), {status: mode === "create" ? 201 : 200, headers: {"Content-Type": "application/json"}}));
        vi.stubGlobal("fetch", fetchMock);
        const result = mode === "list" ? await listSubjects() : mode === "create" ? await createSubject(request) :
            mode === "update" ? await updateSubject(7, request) : await deleteSubject(7);
        const [url, options] = fetchMock.mock.calls[0];
        const base = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";
        const path = mode === "list" ? "/api/subjects" : `/api/admin/subjects${mode === "create" ? "" : "/7"}`;
        expect(url).toBe(`${base}${path}`);
        expect(options.method).toBe(mode === "list" ? undefined : mode === "create" ? "POST" : mode === "update" ? "PUT" : "DELETE");
        expect(options.headers.get("Authorization")).toBe("Bearer admin-token");
        expect(options.headers.get("Content-Type")).toBe("application/json");
        expect(result).toEqual(mode === "delete" ? undefined : response);
        if (mode === "create" || mode === "update") expect(JSON.parse(options.body)).toEqual(request);
        else expect(options.body).toBeUndefined();
    });
    it.each([401, 403, 404])("retains HTTP %s even when the response body has no useful message", async status => {
        vi.stubGlobal("fetch", vi.fn().mockResolvedValue(new Response('{"message":"Request rejected"}', {status})));
        try {
            await updateSubject(7, request);
            expect.fail("Expected request to fail");
        } catch (error) {
            expect(subjectError(error, "save")).toContain(status === 404 ? "finns inte längre" : "behörighet");
        }
    });
    it("does not classify unknown server failures as blocked deletion", () => {
        expect(subjectError(new Error("Internal server error"), "delete")).toContain("Försök igen");
        expect(subjectError(new Error("Internal server error"), "delete")).not.toContain("in use");
    });
});
