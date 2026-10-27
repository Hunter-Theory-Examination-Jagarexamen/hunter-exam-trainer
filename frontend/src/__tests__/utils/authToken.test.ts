import {afterEach, describe, expect, it} from "vitest";
import {getTokenStatus} from "../../utils/authToken";
import {fakeToken} from "../fakeToken";

afterEach(() => localStorage.clear());

describe("getTokenStatus", () => {
    it("is missing when no token is saved", () => {
        expect(getTokenStatus()).toBe("missing");
    });

    it("is valid when the token expires in the future", () => {
        localStorage.setItem("token", fakeToken(3600));
        expect(getTokenStatus()).toBe("valid");
    });

    it("is expired when the token's expiry time has passed", () => {
        localStorage.setItem("token", fakeToken(-60));
        expect(getTokenStatus()).toBe("expired");
    });

    it("is expired days later, using the time it is checked", () => {
        localStorage.setItem("token", fakeToken(3600));
        const threeDaysLater = Date.now() + 3 * 24 * 60 * 60 * 1000;
        expect(getTokenStatus(threeDaysLater)).toBe("expired");
    });

    it("is invalid when the token is not a JWT", () => {
        localStorage.setItem("token", "not-a-jwt");
        expect(getTokenStatus()).toBe("invalid");
    });

    it("is invalid when the payload is not JSON", () => {
        localStorage.setItem("token", "aaa.bm90IGpzb24.ccc");
        expect(getTokenStatus()).toBe("invalid");
    });
});
