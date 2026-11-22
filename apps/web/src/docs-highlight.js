import hljs from "highlight.js/lib/core";
import scala from "highlight.js/lib/languages/scala";
import xml from "highlight.js/lib/languages/xml";
import bash from "highlight.js/lib/languages/bash";
import javascript from "highlight.js/lib/languages/javascript";
import json from "highlight.js/lib/languages/json";
import css from "highlight.js/lib/languages/css";

hljs.registerLanguage("scala", scala);
hljs.registerLanguage("xml", xml);
hljs.registerLanguage("html", xml);
hljs.registerLanguage("bash", bash);
hljs.registerLanguage("shell", bash);
hljs.registerLanguage("javascript", javascript);
hljs.registerLanguage("json", json);
hljs.registerLanguage("css", css);

const aliases = {
  html: "xml",
  shell: "bash",
  sh: "bash",
  js: "javascript",
};

const resolve = (language) => aliases[language] || language;

// ── Scala layout ────────────────────────────────────────────────────────────────────────────────
// Many docs examples are written as one long call expression. Shown as-is they scroll sideways for
// hundreds of characters, so long lines are re-laid-out the way scalafmt would: a call that fits
// stays on one line, one that does not puts each argument on its own indented line. Only bracket
// structure is touched; string literals are opaque, and a line whose brackets do not balance (an
// expression continued on the next line) is left exactly as written.

const OPEN = { "(": ")", "[": "]", "{": "}" };
const CLOSE = new Set([")", "]", "}"]);

/** Splits a line into text runs and bracket groups. Returns null when brackets do not balance. */
function parseLine(text) {
  const root = { parts: [] };
  const stack = [{ group: null, items: [root] }];
  const current = () => {
    const top = stack[stack.length - 1];
    return top.items[top.items.length - 1].parts;
  };
  const pushText = (s) => {
    const parts = current();
    if (parts.length && typeof parts[parts.length - 1] === "string") parts[parts.length - 1] += s;
    else parts.push(s);
  };

  let i = 0;
  while (i < text.length) {
    const ch = text[i];
    if (text.startsWith('"""', i)) {
      const end = text.indexOf('"""', i + 3);
      if (end < 0) return null;
      pushText(text.slice(i, end + 3));
      i = end + 3;
      continue;
    }
    if (ch === '"') {
      let j = i + 1;
      while (j < text.length && text[j] !== '"') j += text[j] === "\\" ? 2 : 1;
      if (j >= text.length) return null;
      pushText(text.slice(i, j + 1));
      i = j + 1;
      continue;
    }
    if (ch === "'" && text[i + 2] === "'") {
      pushText(text.slice(i, i + 3));
      i += 3;
      continue;
    }
    if (text.startsWith("//", i)) {
      pushText(text.slice(i));
      break;
    }
    if (OPEN[ch]) {
      const group = { open: ch, close: OPEN[ch], items: [{ parts: [] }] };
      current().push(group);
      stack.push({ group, items: group.items });
      i += 1;
      continue;
    }
    if (CLOSE.has(ch)) {
      const top = stack.pop();
      if (!top.group || top.group.close !== ch) return null;
      i += 1;
      continue;
    }
    if (ch === "," && stack.length > 1 && stack[stack.length - 1].group.open !== "{") {
      stack[stack.length - 1].items.push({ parts: [] });
      i += 1;
      continue;
    }
    pushText(ch);
    i += 1;
  }
  return stack.length === 1 ? root.parts : null;
}

function flatParts(parts) {
  return parts.map((p) => (typeof p === "string" ? p : flatGroup(p))).join("");
}

function flatGroup(group) {
  const items = group.items.map((item) => flatParts(item.parts).trim());
  if (items.length === 1 && items[0] === "") return group.open + group.close;
  const body = items.join(", ");
  return group.open === "{" ? `{ ${body} }` : group.open + body + group.close;
}

/** Text that must stay glued to a group's closing bracket: `.map`, `)`, `:= x` up to the next group. */
function trailingWidth(parts, index) {
  let width = 0;
  for (let k = index + 1; k < parts.length; k++) {
    if (typeof parts[k] !== "string") break;
    width += parts[k].length;
  }
  return width;
}

function renderParts(parts, indent, col, width) {
  let out = "";
  parts.forEach((part, index) => {
    if (typeof part === "string") {
      out += part;
      col += part.length;
      return;
    }
    const flat = flatGroup(part);
    if (col + flat.length + trailingWidth(parts, index) <= width || flat.length <= 2) {
      out += flat;
      col += flat.length;
      return;
    }
    const inner = indent + 2;
    const pad = " ".repeat(inner);
    const items = part.items.filter((item, k) => k < part.items.length - 1 || flatParts(item.parts).trim() !== "");
    out += part.open + "\n";
    items.forEach((item, k) => {
      const trimmed = item.parts.slice();
      if (typeof trimmed[0] === "string") trimmed[0] = trimmed[0].replace(/^\s+/, "");
      const last = trimmed.length - 1;
      if (typeof trimmed[last] === "string") trimmed[last] = trimmed[last].replace(/\s+$/, "");
      const rendered = renderParts(trimmed, inner, inner, width);
      out += pad + rendered.text + (k < items.length - 1 && part.open !== "{" ? "," : "") + "\n";
    });
    out += " ".repeat(indent) + part.close;
    col = indent + 1;
  });
  return { text: out, col };
}

function formatScalaLine(line, width) {
  if (line.length <= width) return line;
  const indent = line.match(/^\s*/)[0].length;
  const parts = parseLine(line.slice(indent));
  if (!parts) return line;
  return " ".repeat(indent) + renderParts(parts, indent, indent, width).text;
}

function formatScala(source, width = 80) {
  // Triple-quoted strings can span lines; leave any source that contains one untouched.
  if (source.includes('"""')) return source;
  return source
    .split("\n")
    .map((line) => formatScalaLine(line, width))
    .join("\n");
}

// ── Highlighting ────────────────────────────────────────────────────────────────────────────────

function highlightHtml(language, source) {
  try {
    return hljs.highlight(source, { language: resolve(language), ignoreIllegals: true }).value;
  } catch {
    return source.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
  }
}

/** hljs output split per line, with any span open across a newline closed and reopened. */
function splitLines(html) {
  const lines = [];
  const stack = [];
  let current = "";
  for (const match of html.matchAll(/(<span[^>]*>)|(<\/span>)|(\n)|([^<\n]+)/g)) {
    if (match[1]) {
      stack.push(match[1]);
      current += match[1];
    } else if (match[2]) {
      stack.pop();
      current += match[2];
    } else if (match[3]) {
      lines.push(current + "</span>".repeat(stack.length));
      current = stack.join("");
    } else {
      current += match[0];
    }
  }
  lines.push(current);
  return lines;
}

window.ScDocsHighlight = {
  highlight(node, language, source) {
    node.innerHTML = highlightHtml(language, source);
    node.classList.add("hljs");
    node.dataset.scHighlighted = "true";
  },

  /** Source as it should be shown and copied: Scala is re-laid-out, everything else is untouched. */
  format(language, source) {
    const trimmed = source.replace(/\s+$/, "");
    return resolve(language) === "scala" ? formatScala(trimmed) : trimmed;
  },

  /** Highlighted HTML, one string per line of `source`. */
  lines(language, source) {
    return splitLines(highlightHtml(language, source));
  },
};

export { formatScala, splitLines };
