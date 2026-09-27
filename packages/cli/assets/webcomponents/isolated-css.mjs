// Build the document stylesheet used by Web Component bundles. The full Tailwind sheet stays in Shadow DOM;
// only component light-DOM trees receive these scoped rules. Pass the caller's PostCSS instance.
export function isolateDocumentCss(source, postcss) {
  const css = postcss.parse(source);
  const publicTokens = new Set(`radius background foreground card card-foreground popover popover-foreground primary primary-foreground secondary secondary-foreground muted muted-foreground accent accent-foreground destructive destructive-foreground border input ring chart-1 chart-2 chart-3 chart-4 chart-5 sidebar sidebar-foreground sidebar-primary sidebar-primary-foreground sidebar-accent sidebar-accent-foreground sidebar-border sidebar-ring`.split(" ").map(name => `--${name}`));
  const tokenSelector = /^(?::root|\.dark|\[data-(?:base|theme)-color="[\w-]+"\]|\.dark\[data-(?:base|theme)-color="[\w-]+"\])$/;
  const defaults = postcss.atRule({ name: "layer", params: "defaults" });
  const animations = postcss.root();

  function isolate(container) {
    const result = container.clone({ nodes: [] });
    for (const node of container.nodes ?? []) {
      if (node.type === "comment") continue;
      if (node.type === "rule") {
        const selectors = node.selectors.filter(selector => tokenSelector.test(selector.trim()));
        if (selectors.length) {
          const tokens = node.clone({ selector: selectors.join(", "), nodes: [] });
          for (const decl of node.nodes) {
            if (decl.type === "decl" && publicTokens.has(decl.prop)) tokens.append(decl.clone());
          }
          if (tokens.nodes.length) defaults.append(tokens);
        }
        if (selectors.length === node.selectors.length) continue;
        const remaining = node.selectors.filter(selector => !selectors.includes(selector));
        // @scope excludes its root unless selected with :scope. Light-DOM hosts carry utility and cn-* classes.
        if (node.parent.type !== "rule") {
          remaining.push(...remaining.filter(selector => !selector.includes("::") && !selector.includes("&"))
            .map(selector => `:scope:is(${selector})`));
        }
        result.append(node.clone({ selector: remaining.join(", ") }));
      } else if (node.type === "atrule" && node.nodes && !["property", "keyframes"].includes(node.name)) {
        const nested = isolate(node);
        if (nested.nodes.length) result.append(nested);
      } else if (node.type === "atrule" && node.name === "keyframes") {
        animations.append(node.clone());
      }
    }
    return result;
  }

  const scoped = isolate(css);
  const theme = css.nodes.find(node => node.type === "atrule" && node.name === "layer" && node.params === "theme");
  const themeRoot = theme?.nodes.find(node => node.type === "rule" && node.selectors.includes(":root"));
  if (!themeRoot) throw new Error("Generated CSS is missing the Tailwind theme tokens");
  const localTheme = themeRoot.clone({ selector: ":scope" });
  localTheme.walkDecls(decl => { if (publicTokens.has(decl.prop)) decl.remove(); });
  const documentCss = postcss.root();
  documentCss.append(defaults);
  const scope = postcss.atRule({ name: "scope", params: "([data-sc-component])" });
  scope.append(animations.nodes);
  scope.append(postcss.atRule({ name: "layer", params: "theme", nodes: [localTheme] }));
  scope.append(scoped.nodes);
  documentCss.append(scope);
  return documentCss.toString();
}
