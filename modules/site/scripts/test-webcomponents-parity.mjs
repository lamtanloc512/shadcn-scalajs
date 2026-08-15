import assert from "node:assert/strict";
import { chromium } from "playwright";

const baseUrl = process.env.SC_WC_TEST_URL ?? "http://127.0.0.1:4173";
const browser = await chromium.launch();
const page = await browser.newPage();

try {
  await page.goto(`${baseUrl}/wc-parity-fixture.html`, { waitUntil: "networkidle" });
  await page.waitForFunction(() => customElements.get("sc-tooltip") !== undefined);

  const result = await page.evaluate(() => {
    const button = document.querySelector("sc-button");
    const badge = document.querySelector("sc-badge");
    const tooltip = document.querySelector("sc-tooltip");
    const card = document.querySelector("sc-card");
    const buttonEl = button?.shadowRoot?.querySelector("button");
    const badgeEl = badge?.shadowRoot?.querySelector("span");
    const tooltipTrigger = tooltip?.shadowRoot?.querySelector("[data-slot=tooltip-trigger]");
    return {
      buttonClass: buttonEl?.className ?? "",
      buttonSlot: buttonEl?.getAttribute("data-slot"),
      buttonColor: buttonEl ? getComputedStyle(buttonEl).backgroundColor : "",
      badgeClass: badgeEl?.className ?? "",
      badgeSlot: badgeEl?.getAttribute("data-slot"),
      cardDisplay: card ? getComputedStyle(card).display : "",
      tooltipTriggerExists: Boolean(tooltipTrigger)
    };
  });

  assert.match(result.buttonClass, /cn-button/);
  assert.equal(result.buttonSlot, "button");
  assert.notEqual(result.buttonColor, "rgba(0, 0, 0, 0)");
  assert.match(result.badgeClass, /cn-badge/);
  assert.equal(result.badgeSlot, "badge");
  assert.equal(result.cardDisplay, "flex");
  assert.equal(result.tooltipTriggerExists, true);

  await page.locator("sc-tooltip").evaluate(element => {
    const trigger = element.shadowRoot?.querySelector("[data-slot=tooltip-trigger]");
    if (!trigger) throw new Error("tooltip trigger not found");
    trigger.dispatchEvent(new PointerEvent("pointerenter", { bubbles: true, composed: true }));
  });
  await page.waitForFunction(() => {
    const content = document.querySelector("sc-tooltip")?.shadowRoot?.querySelector("[data-slot=tooltip-content]");
    return content && getComputedStyle(content).display !== "none";
  });
  assert.equal(
    await page.evaluate(() => document.querySelector("sc-tooltip")?.shadowRoot?.querySelector("[data-slot=tooltip-content]")?.textContent),
    "Helpful context"
  );
} finally {
  await browser.close();
}

console.log("Web component visual and hover parity checks passed");
