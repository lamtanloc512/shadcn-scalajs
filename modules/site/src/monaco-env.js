import editorWorker from "monaco-editor/editor/editor.worker?worker";
import htmlWorker from "monaco-editor/language/html/html.worker?worker";
import cssWorker from "monaco-editor/language/css/css.worker?worker";
import jsonWorker from "monaco-editor/language/json/json.worker?worker";
import tsWorker from "monaco-editor/language/typescript/ts.worker?worker";

// Must be set before `monaco-editor` loads. The 0.56 bundle registers HTML/CSS/JSON/TS
// language features that RPC into these workers (`doValidation`, `findDocumentColors`).
// If they land on `editor.worker`, the worker throws "Missing requestHandler or method".
globalThis.MonacoEnvironment = {
  getWorker(_workerId, label) {
    if (label === "html" || label === "handlebars" || label === "razor") return new htmlWorker();
    if (label === "css" || label === "scss" || label === "less") return new cssWorker();
    if (label === "json") return new jsonWorker();
    if (label === "typescript" || label === "javascript") return new tsWorker();
    return new editorWorker();
  },
};
