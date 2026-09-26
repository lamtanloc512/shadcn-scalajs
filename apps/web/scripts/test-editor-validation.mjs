import assert from "node:assert/strict";
import { validateHtml } from "../src/html-validation.js";

const malformed = validateHtml('<sc-button variant="primary"');
assert.equal(malformed.length, 1);
assert.equal(malformed[0].severity, 8);
assert.equal(malformed[0].message, "Unexpected EOF in tag.");
assert.equal(malformed[0].startLineNumber, 1);
assert.equal(malformed[0].startColumn, 29);

assert.deepEqual(validateHtml('<sc-button variant="primary"></sc-button>'), []);
console.log("editor HTML validation: malformed marker and corrected source checks passed");
