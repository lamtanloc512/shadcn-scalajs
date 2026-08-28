import { autocompletion, closeBrackets, completeFromList } from "@codemirror/autocomplete";
import { defaultKeymap, history, historyKeymap, indentWithTab } from "@codemirror/commands";
import { html } from "@codemirror/lang-html";
import { bracketMatching, indentOnInput, HighlightStyle, syntaxHighlighting } from "@codemirror/language";
import { searchKeymap } from "@codemirror/search";
import { Compartment, EditorState } from "@codemirror/state";
import { tags } from "@lezer/highlight";
import { drawSelection, EditorView, highlightActiveLine, keymap, lineNumbers } from "@codemirror/view";
import { oneDark } from "@codemirror/theme-one-dark";

const tagsList = ["sc-accordion", "sc-badge", "sc-button", "sc-calendar", "sc-card", "sc-checkbox", "sc-combobox", "sc-dialog", "sc-dropdown-menu", "sc-progress", "sc-radio-group", "sc-select", "sc-separator", "sc-slider", "sc-spinner", "sc-switch", "sc-tabs", "sc-toggle-group", "sc-tooltip"];
const attrs = ["variant", "size", "disabled", "checked", "indeterminate", "value", "min", "max", "step", "placeholder", "options", "items", "name", "type", "open", "slot"];
const completions = completeFromList([...tagsList.map(label => ({ label, type: "class", detail: "shadcn-scalajs element" })), ...attrs.map(label => ({ label, type: "property", detail: "Web Component attribute" }))]);
const lightSyntax = syntaxHighlighting(HighlightStyle.define([
  { tag: tags.tagName, color: "#9c36b5" }, { tag: tags.attributeName, color: "#1c6b9c" }, { tag: tags.string, color: "#946200" },
  { tag: tags.comment, color: "#65737e" }, { tag: tags.content, color: "#202124" },
]));
const themeCompartment = new Compartment();
const lightTheme = EditorView.theme({
  "&": { backgroundColor: "#ffffff", color: "#202124" }, ".cm-scroller": { fontFamily: "ui-monospace, SFMono-Regular, Menlo, monospace" },
  ".cm-content": { caretColor: "#2563eb" }, ".cm-gutters": { backgroundColor: "#f5f7fa", color: "#697386", border: "0", borderRight: "1px solid #e5e7eb" },
  ".cm-activeLineGutter": { backgroundColor: "#e8eef8" }, ".cm-activeLine": { backgroundColor: "#f5f8ff" },
  ".cm-selectionBackground, .cm-content ::selection": { backgroundColor: "#cfe0ff" }, ".cm-tooltip": { backgroundColor: "#ffffff", color: "#202124", border: "1px solid #d5d9e2" },
  ".cm-tooltip-autocomplete ul li[aria-selected]": { backgroundColor: "#dbeafe", color: "#111827" }, ".cm-searchMatch": { backgroundColor: "#fde68a" },
  ".cm-searchMatch-selected": { backgroundColor: "#f59e0b" },
});
const themeFor = dark => dark ? [oneDark] : [lightTheme, lightSyntax];

window.ScPlaygroundEditor = {
  mount(container, value, onChange, dark = false) {
    const state = EditorState.create({
      doc: value,
      extensions: [lineNumbers(), history(), drawSelection(), bracketMatching(), closeBrackets(), indentOnInput(), highlightActiveLine(), html(),
        autocompletion({ override: [completions], activateOnTyping: true }), keymap.of([...defaultKeymap, ...historyKeymap, ...searchKeymap, indentWithTab]),
        themeCompartment.of(themeFor(dark)), EditorView.updateListener.of(update => { if (update.docChanged) onChange(update.state.doc.toString()); }),
      ],
    });
    const view = new EditorView({ state, parent: container });
    return {
      setValue(next) { if (next !== view.state.doc.toString()) view.dispatch({ changes: { from: 0, to: view.state.doc.length, insert: next } }); },
      setTheme(nextDark) { if (nextDark !== dark) { dark = nextDark; view.dispatch({ effects: themeCompartment.reconfigure(themeFor(dark)) }); } },
      focus() { view.focus(); }, destroy() { view.destroy(); },
    };
  },
};
