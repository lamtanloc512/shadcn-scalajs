import hljs from "highlight.js/lib/core";
import scala from "highlight.js/lib/languages/scala";
import xml from "highlight.js/lib/languages/xml";
import bash from "highlight.js/lib/languages/bash";
import javascript from "highlight.js/lib/languages/javascript";

hljs.registerLanguage("scala", scala);
hljs.registerLanguage("xml", xml);
hljs.registerLanguage("html", xml);
hljs.registerLanguage("bash", bash);
hljs.registerLanguage("shell", bash);
hljs.registerLanguage("javascript", javascript);

const aliases = {
  html: "xml",
  shell: "bash",
  sh: "bash",
  js: "javascript",
};

window.ScDocsHighlight = {
  highlight(node, language, source) {
    const lang = aliases[language] || language;
    try {
      node.innerHTML = hljs.highlight(source, { language: lang, ignoreIllegals: true }).value;
    } catch {
      node.textContent = source;
    }
    node.classList.add("hljs");
    node.dataset.scHighlighted = "true";
  },
};
