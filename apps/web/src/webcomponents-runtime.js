// Shared, idempotent loader for the standalone custom-element artifact used by docs and the playground.
const assetBase = "/";
const packBase = "/styles";

function ensureBases() {
  document.documentElement.dataset.scAssetsBase = assetBase;
  document.documentElement.dataset.scPackBase = packBase;
}

function ensureCss() {
  if (document.querySelector('link[data-sc-components-css]')) return;
  const link = document.createElement("link");
  link.rel = "stylesheet";
  link.href = `${assetBase}sc-components.css`;
  link.dataset.scComponentsCss = "true";
  document.head.append(link);
}

async function ensureScript() {
  if (window.customElements?.get("sc-button")) return true;
  ensureCss();
  await import(/* @vite-ignore */ `${assetBase}sc-components.js`);
  return true;
}

const runtime = (window.ScComponentsRuntime ??= {
  promise: null,
  loadScComponents(tags = []) {
    ensureBases();
    // Keep working if a caller detaches this method (`fn = runtime.loadScComponents; fn()`).
    const state = this && this !== globalThis ? this : runtime;
    if (!state.promise) state.promise = ensureScript();
    return state.promise.then(async () => {
      const list = Array.from(tags || []).filter(Boolean);
      if (list.length) await Promise.all(list.map((tag) => customElements.whenDefined(tag)));
      return true;
    });
  },
});

ensureBases();
runtime.loadScComponents();
export const loadScComponents = (...args) => runtime.loadScComponents(...args);
