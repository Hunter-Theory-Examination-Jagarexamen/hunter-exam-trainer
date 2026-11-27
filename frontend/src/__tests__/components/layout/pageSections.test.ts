// Tests for src/components/layout/pageSections.ts

import { describe, it, expect } from "vitest";
import { sectionForPath } from "../../../components/layout/pageSections";

describe("sectionForPath", () => {
    it("maps each section's main route", () => {
        expect(sectionForPath("/practice/subjects")).toBe("quiz");
        expect(sectionForPath("/practice/random")).toBe("random");
        expect(sectionForPath("/mockexam")).toBe("exam");
        expect(sectionForPath("/exam")).toBe("exam");
        expect(sectionForPath("/result")).toBe("exam");
        expect(sectionForPath("/statistics")).toBe("results");
        expect(sectionForPath("/profile")).toBe("profile");
        expect(sectionForPath("/admin")).toBe("admin");
    });

    it("maps routes below a section to the same section", () => {
        expect(sectionForPath("/practice/subjects/3")).toBe("quiz");
        expect(sectionForPath("/admin/learners/5")).toBe("admin");
    });

    it("gives no section to pages that keep the green cards", () => {
        expect(sectionForPath("/dashboard")).toBeUndefined();
        expect(sectionForPath("/feedback")).toBeUndefined();
        expect(sectionForPath("/practice")).toBeUndefined();
    });

    it("does not match a route that only starts with the same letters", () => {
        expect(sectionForPath("/examples")).toBeUndefined();
        expect(sectionForPath("/administration")).toBeUndefined();
    });
});
