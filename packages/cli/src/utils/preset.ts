/** Minimal preset decoder for the style-pack field.
 *
 * Must stay aligned with `modules/site/.../create/Preset.scala` field order/bit widths.
 */
const BASE62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

export const STYLE_PACKS = [
  "nova",
  "vega",
  "maia",
  "lyra",
  "mira",
  "luma",
  "sera",
  "rhea"
] as const;

export type StylePack = (typeof STYLE_PACKS)[number];

const FIELDS_V1 = [
  { key: "menuColor", bits: 3 },
  { key: "menuAccent", bits: 3 },
  { key: "radius", bits: 4 },
  { key: "font", bits: 6 },
  { key: "iconLibrary", bits: 6 },
  { key: "theme", bits: 6 },
  { key: "baseColor", bits: 6 },
  { key: "style", bits: 6 }
] as const;

const FIELDS_V2 = [
  ...FIELDS_V1,
  { key: "chartColor", bits: 6 },
  { key: "fontHeading", bits: 5 }
] as const;

function fromBase62(value: string): number {
  let n = 0;
  for (const ch of value) {
    const idx = BASE62.indexOf(ch);
    if (idx < 0) return -1;
    n = n * 62 + idx;
  }
  return n;
}

export function isStylePack(value: string): value is StylePack {
  return (STYLE_PACKS as readonly string[]).includes(value);
}

/** Extract the style pack encoded in a customizer preset code (`b...` / `a...`). */
export function stylePackFromPreset(code: string | undefined): StylePack | undefined {
  if (!code || code.length < 2) return undefined;
  const version = code[0];
  if (version !== "a" && version !== "b") return undefined;
  const fields = version === "a" ? FIELDS_V1 : FIELDS_V2;
  const bits = fromBase62(code.slice(1));
  if (bits < 0) return undefined;

  let offset = 0;
  for (const field of fields) {
    const idx = Math.floor(bits / 2 ** offset) % 2 ** field.bits;
    if (field.key === "style") {
      return STYLE_PACKS[Math.min(idx, STYLE_PACKS.length - 1)] ?? "nova";
    }
    offset += field.bits;
  }
  return undefined;
}
