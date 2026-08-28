import { parseFragment } from "parse5";

const messages = {
  "eof-in-tag": "Unexpected EOF in tag.",
  "eof-in-element-that-can-contain-only-text": "Unexpected EOF in text-only element.",
  "missing-end-tag-name": "Missing end tag name.",
  "missing-doctype-name": "Missing doctype name.",
  "unexpected-character-in-attribute-name": "Unexpected character in attribute name.",
  "unexpected-character-in-unquoted-attribute-value": "Unexpected character in unquoted attribute value.",
  "unexpected-equals-sign-before-attribute-name": "Unexpected equals sign before attribute name.",
  "duplicate-attribute": "Duplicate attribute.",
  "unexpected-solidus-in-tag": "Unexpected slash in tag.",
  "unexpected-question-mark-instead-of-tag-name": "Unexpected question mark instead of tag name.",
  "unexpected-end-tag": "Unexpected end tag.",
};

const messageFor = (code) => messages[code] || code.replaceAll("-", " ").replace(/(^| )([a-z])/g, (_, prefix, letter) => `${prefix}${letter.toUpperCase()}`) + ".";

/** Return parse5 syntax errors in the marker shape used by Monaco. */
export function validateHtml(source) {
  const errors = [];
  parseFragment(source, {
    sourceCodeLocationInfo: true,
    onParseError: (error) => errors.push(error),
  });
  return errors.map((error) => ({
    severity: 8, // monaco.MarkerSeverity.Error (kept numeric so this module stays testable in Node)
    message: messageFor(error.code),
    startLineNumber: error.startLine || 1,
    startColumn: error.startCol || 1,
    endLineNumber: error.endLine || error.startLine || 1,
    endColumn: Math.max(error.endCol || error.startCol || 1, (error.startCol || 1) + 1),
  }));
}
