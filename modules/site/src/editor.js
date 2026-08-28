import * as monaco from "monaco-editor";
// Package exports map `monaco-editor/<path>` -> `esm/vs/<path>`; pull features + HTML language via that graph.
import "monaco-editor/editor/editor.main.js";
import "monaco-editor/languages/definitions/html/register.js";
import "monaco-editor/language/html/monaco.contribution.js";
import editorWorker from "monaco-editor/editor/editor.worker?worker";
import htmlWorker from "monaco-editor/language/html/html.worker?worker";

// Vite-friendly Monaco workers (same approach as many Monaco + Vite apps / Vue playground-style hosts).
self.MonacoEnvironment = {
  getWorker(_workerId, label) {
    if (label === "html" || label === "handlebars" || label === "razor") return new htmlWorker();
    return new editorWorker();
  },
};

const scTags = [
  "sc-accordion", "sc-alert", "sc-alert-title", "sc-alert-description", "sc-avatar", "sc-badge", "sc-breadcrumb",
  "sc-button", "sc-button-group", "sc-calendar", "sc-card", "sc-card-header", "sc-card-title", "sc-card-description",
  "sc-card-content", "sc-card-footer", "sc-chart", "sc-checkbox", "sc-collapsible", "sc-combobox", "sc-command",
  "sc-dialog", "sc-dropdown-menu", "sc-empty", "sc-field", "sc-form", "sc-input", "sc-input-group", "sc-item", "sc-kbd",
  "sc-label", "sc-native-select", "sc-popover", "sc-progress", "sc-radio", "sc-radio-group", "sc-range", "sc-scrollbar",
  "sc-select", "sc-separator", "sc-sidebar", "sc-skeleton", "sc-slider", "sc-spinner", "sc-switch", "sc-table", "sc-tabs",
  "sc-textarea", "sc-toast", "sc-toggle-group", "sc-tooltip",
];
const scAttrs = [
  "variant", "size", "disabled", "checked", "indeterminate", "value", "min", "max", "step", "placeholder", "options",
  "items", "sections", "menus", "data", "name", "type", "open", "slot", "text", "orientation",
];

monaco.languages.registerCompletionItemProvider("html", {
  triggerCharacters: ["<", " ", "-", '"', "'"],
  provideCompletionItems(model, position) {
    const word = model.getWordUntilPosition(position);
    const range = {
      startLineNumber: position.lineNumber,
      endLineNumber: position.lineNumber,
      startColumn: word.startColumn,
      endColumn: word.endColumn,
    };
    const textUntil = model.getValueInRange({
      startLineNumber: position.lineNumber,
      startColumn: 1,
      endLineNumber: position.lineNumber,
      endColumn: position.column,
    });
    const suggestions = [];
    if (/<\s*[\w-]*$/.test(textUntil)) {
      for (const label of scTags) {
        suggestions.push({
          label,
          kind: monaco.languages.CompletionItemKind.Class,
          insertText: label,
          detail: "shadcn-scalajs element",
          range,
        });
      }
    } else {
      for (const label of scAttrs) {
        suggestions.push({
          label,
          kind: monaco.languages.CompletionItemKind.Property,
          insertText: `${label}="$1"`,
          insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
          detail: "Web Component attribute",
          range,
        });
      }
    }
    return { suggestions };
  },
});

function applyTheme(dark) {
  monaco.editor.setTheme(dark ? "vs-dark" : "vs");
}

window.ScPlaygroundEditor = {
  mount(container, value, onChange, dark = false) {
    container.replaceChildren();
    container.style.height = "100%";
    container.style.minHeight = "0";
    applyTheme(dark);
    let suppress = false;
    const editor = monaco.editor.create(container, {
      value: value ?? "",
      language: "html",
      theme: dark ? "vs-dark" : "vs",
      automaticLayout: true,
      minimap: { enabled: false },
      fontSize: 13,
      lineNumbers: "on",
      wordWrap: "on",
      scrollBeyondLastLine: false,
      tabSize: 2,
      renderLineHighlight: "line",
      bracketPairColorization: { enabled: true },
      matchBrackets: "always",
      multiCursorModifier: "alt",
      find: { addExtraSpaceOnTop: false },
      padding: { top: 8, bottom: 8 },
      fixedOverflowWidgets: true,
      ariaLabel: "Web Component HTML source",
    });
    editor.onDidChangeModelContent(() => {
      if (suppress) return;
      onChange(editor.getValue());
    });
    return {
      setValue(next) {
        const current = editor.getValue();
        if (next === current) return;
        suppress = true;
        editor.setValue(next ?? "");
        suppress = false;
      },
      setTheme(nextDark) {
        dark = !!nextDark;
        applyTheme(dark);
      },
      focus() {
        editor.focus();
      },
      destroy() {
        editor.dispose();
        container.replaceChildren();
      },
    };
  },
};

// Static docs code remains a lightweight read-only highlighter (not full Monaco per docs page).
const escapeHtml = (text) =>
  text.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;");
const highlightDocs = (node, language, source) => {
  const escaped = escapeHtml(source);
  const pattern =
    language === "scala"
      ? /(\/\/[^\n]*|"(?:\\.|[^"\\])*"|'(?:\\.|[^'\\])*'|\b(?:val|var|def|class|object|case|match|if|else|true|false|import|private|final|extends|new)\b|\b\d+(?:\.\d+)?\b)/g
      : /(&lt;!--[\s\S]*?--&gt;|&lt;\/?[\w-]+|\b[\w-]+(?=\s*=)|"[^"\n]*"|\b(?:const|let|var|function|return|true|false|import|from)\b|\b\d+\b)/g;
  node.innerHTML = escaped.replace(pattern, (token) => {
    const kind =
      token.startsWith("//") || token.startsWith("<!--") || token.startsWith("&lt;!--")
        ? "comment"
        : token.startsWith('"') || token.startsWith("'")
          ? "string"
          : /^\d/.test(token)
            ? "number"
            : /^(&lt;|&lt;\/)/.test(token)
              ? "tag"
              : "keyword";
    return `<span class="sc-doc-token-${kind}">${token}</span>`;
  });
  node.dataset.scHighlighted = "true";
  if (!node.parentElement.querySelector("[data-sc-copy]")) {
    const copy = document.createElement("button");
    copy.type = "button";
    copy.dataset.scCopy = "true";
    copy.textContent = "Copy";
    copy.className = "absolute right-2 top-1 rounded px-2 py-1 text-xs text-muted-foreground hover:bg-muted";
    copy.addEventListener("click", () => navigator.clipboard?.writeText(source));
    node.parentElement.classList.add("relative");
    node.parentElement.append(copy);
  }
};
window.ScDocsHighlight = { highlight: highlightDocs };
