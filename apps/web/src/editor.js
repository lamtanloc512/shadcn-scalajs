import "./monaco-env.js";
import * as monaco from "monaco-editor";
import tailwindBrowserUrl from "@tailwindcss/browser?url";
import { validateHtml } from "./html-validation.js";

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

let nextMarkerOwner = 1;

window.ScPlaygroundEditor = {
  // Vite turns this into a hashed, local asset rather than a CDN dependency.
  tailwindBrowserUrl,
  mount(container, htmlValue, cssValue, onHtmlChange, onCssChange, dark = false, onDiagnostics = () => {}) {
    container.replaceChildren();
    container.style.height = "100%";
    container.style.minHeight = "0";
    applyTheme(dark);
    let suppress = false;
    let activeFile = "html";
    const viewStates = {};
    const htmlModel = monaco.editor.createModel(
      htmlValue ?? "",
      "html",
      monaco.Uri.parse(`inmemory://sc-playground/index-${nextMarkerOwner}.html`),
    );
    const cssModel = monaco.editor.createModel(
      cssValue ?? "",
      "css",
      monaco.Uri.parse(`inmemory://sc-playground/tailwind-${nextMarkerOwner}.css`),
    );
    const editor = monaco.editor.create(container, {
      model: htmlModel,
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
      colorDecorators: false,
      ariaLabel: "Web Component HTML source",
    });
    const markerOwner = `sc-playground-html-${nextMarkerOwner++}`;
    let validationTimer = null;
    const reportDiagnostics = () => {
      const errors = monaco.editor.getModelMarkers({ resource: htmlModel.uri, owner: markerOwner })
        .filter((marker) => marker.severity === monaco.MarkerSeverity.Error)
        .sort((a, b) => a.startLineNumber - b.startLineNumber || a.startColumn - b.startColumn);
      const first = errors[0];
      onDiagnostics(first ? { line: first.startLineNumber, column: first.startColumn, message: first.message } : null);
    };
    const markerDisposable = monaco.editor.onDidChangeMarkers((uris) => {
      if (uris.some((uri) => uri.toString() === htmlModel.uri.toString())) reportDiagnostics();
    });
    const validate = () => {
      validationTimer = null;
      monaco.editor.setModelMarkers(
        htmlModel,
        markerOwner,
        validateHtml(htmlModel.getValue()).map((marker) => ({
          ...marker,
          severity: monaco.MarkerSeverity.Error,
        })),
      );
    };
    const scheduleValidation = () => {
      if (validationTimer !== null) window.clearTimeout(validationTimer);
      validationTimer = window.setTimeout(validate, 100);
    };
    const contentDisposable = editor.onDidChangeModelContent(() => {
      if (suppress) return;
      if (editor.getModel() === htmlModel) {
        onHtmlChange(editor.getValue());
        scheduleValidation();
      } else {
        onCssChange(editor.getValue());
      }
    });
    validate();
    reportDiagnostics();
    return {
      setHtmlValue(next) {
        const value = next ?? "";
        if (value === htmlModel.getValue()) return;
        suppress = true;
        htmlModel.setValue(value);
        suppress = false;
        scheduleValidation();
      },
      setCssValue(next) {
        const value = next ?? "";
        if (value === cssModel.getValue()) return;
        suppress = true;
        cssModel.setValue(value);
        suppress = false;
      },
      setActive(next) {
        if (next === activeFile) return;
        viewStates[activeFile] = editor.saveViewState();
        activeFile = next === "css" ? "css" : "html";
        editor.setModel(activeFile === "css" ? cssModel : htmlModel);
        if (viewStates[activeFile]) editor.restoreViewState(viewStates[activeFile]);
        editor.updateOptions({
          ariaLabel: activeFile === "css" ? "Tailwind CSS configuration" : "Web Component HTML source",
        });
        editor.focus();
      },
      setTheme(nextDark) { dark = !!nextDark; applyTheme(dark); },
      focus() { editor.focus(); },
      destroy() {
        if (validationTimer !== null) window.clearTimeout(validationTimer);
        monaco.editor.setModelMarkers(htmlModel, markerOwner, []);
        markerDisposable.dispose();
        contentDisposable.dispose();
        editor.dispose();
        htmlModel.dispose();
        cssModel.dispose();
        container.replaceChildren();
      },
    };
  },
};


