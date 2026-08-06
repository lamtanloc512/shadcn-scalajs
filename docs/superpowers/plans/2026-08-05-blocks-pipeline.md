# Blocks Pipeline Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a blocks pipeline to shadcn-scalajs — a `modules/blocks` module, registry support for multi-file block items, `/blocks` routes with iframe previews, and four working blocks (`login-01`, `signup-01`, `otp-01`, `calendar-01`).

**Architecture:** Blocks are authored as Laminar compositions in a new `modules/blocks` sbt module, one package-legal directory per block plus a `*.registry.json` sidecar. `build-registry.mjs` grows a second source root so block items land in `public/registry` next to component items; the CLI needs no changes because it already walks `registryDependencies` and writes nested targets. The docs site gains three routes in their own new files, and the Code tab reads source from the generated registry JSON rather than hand-maintained string literals.

**Tech Stack:** Scala 3.5.2, Scala.js, Laminar 17.2.1, Airstream `Var`, Tailwind CSS v4, Vite 6, Node (plain `.mjs` scripts), TypeScript CLI (Commander).

## Global Constraints

- Spec: `docs/superpowers/specs/2026-08-05-blocks-pipeline-design.md`. Read it before starting.
- `sbt` needs `export PATH="$PATH:$HOME/Library/Application Support/Coursier/bin"` first.
- Tailwind class strings are copied **verbatim** from the upstream file named in each task. Do not reformat, reorder, or "improve" them.
- Every component in `modules/ui` must be self-sufficient Tailwind: it must not depend on `basecoat.generated.css` or `shadcn-presets.generated.css`, because CLI consumers get neither. Keep existing `cn-*` hook classes (style packs target them) — see `.franky/memory/decisions.log`, 2026-08-05.
- Laminar tag collisions: `sectionTag`, `headerTag`, `footerTag`, `navTag`, `mainTag`, `articleTag`, `asideTag`, `dialogTag`, `detailsTag`, `summaryTag`, `timeTag`, `progressTag`, `menuTag`, `commandTag`. Bare `div`, `span`, `button`, `ul`, `li`, `ol`, `p`, `a`, `form`, `label`, `input` are fine.
- Never name a `Var` or parameter `value`, `children`, `content`, or `label` — they shadow Laminar keys.
- Run `sbt scalafmtAll` before finishing any task that touches `.scala`.
- Do **not** run `git commit` or `git push`. This repo's rules require explicit user approval for commits; the "Commit" steps below are written as `git add` staging only.
- There is no Scala test framework in this repo. "Test" for Scala work means: it compiles, `scalafmtCheckAll` passes, and the stated browser/CLI observation holds.

---

### Task 1: `Field.group` plus Field fidelity pass

`login-01` and `signup-01` both use `FieldGroup`, which does not exist in our `Field`. This task adds it and aligns the three existing helpers with upstream class strings.

**Files:**
- Modify: `modules/ui/src/main/scala/shadcnscalajs/ui/Field.scala` (entire file, currently 12 lines)
- Reference (read-only): `/Users/elam/Projects/ui/apps/v4/registry/new-york-v4/ui/field.tsx`

**Interfaces:**
- Consumes: nothing.
- Produces: `Field.apply(mods: Modifier[HtmlElement]*): HtmlElement`, `Field.group(mods: Modifier[HtmlElement]*): HtmlElement`, `Field.label(text: String, mods: Modifier[HtmlElement]*): HtmlElement`, `Field.description(mods: Modifier[HtmlElement]*): HtmlElement`, `Field.error(mods: Modifier[HtmlElement]*): HtmlElement`. Tasks 3 and 4 call `Field.apply`, `Field.group`, `Field.label`, `Field.description`.

- [ ] **Step 1: Replace the body of `Field.scala` with this exact content**

```scala
package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

/** shadcn/ui Field primitives — Tailwind utilities copied from the canonical new-york-v4 field.tsx.
  *
  * Only the parts the shipped blocks use are ported: Field, FieldGroup, FieldLabel, FieldDescription (plus the
  * pre-existing error helper). FieldSet/FieldLegend/FieldSeparator/FieldContent/FieldTitle are not ported yet.
  */
object Field:
  private val labelTag = htmlTag("label")

  /** field.tsx `fieldVariants`, vertical orientation (the default). */
  def apply(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      role := "group",
      dataAttr("slot") := "field",
      dataAttr("orientation") := "vertical",
      cls := "field group/field flex w-full gap-3 data-[invalid=true]:text-destructive flex-col [&>*]:w-full [&>.sr-only]:w-auto",
      mods
    )

  /** field.tsx `FieldGroup`. */
  def group(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "field-group",
      cls := "group/field-group @container/field-group flex w-full flex-col gap-7 data-[slot=checkbox-group]:gap-3 [&>[data-slot=field-group]]:gap-4",
      mods
    )

  /** field.tsx `FieldLabel` — wraps Label's classes plus the field-label additions. */
  def label(text: String, mods: Modifier[HtmlElement]*): HtmlElement =
    labelTag(
      dataAttr("slot") := "field-label",
      cls := "flex items-center gap-2 text-sm leading-none font-medium select-none group/field-label peer/field-label w-fit leading-snug group-data-[disabled=true]/field:opacity-50",
      mods,
      text
    )

  /** field.tsx `FieldDescription`. */
  def description(mods: Modifier[HtmlElement]*): HtmlElement =
    p(
      dataAttr("slot") := "field-description",
      cls := "text-sm leading-normal font-normal text-muted-foreground last:mt-0 [&>a]:underline [&>a]:underline-offset-4 [&>a:hover]:text-primary",
      mods
    )

  def error(mods: Modifier[HtmlElement]*): HtmlElement = p(cls := "text-sm font-medium text-destructive", mods)
```

- [ ] **Step 2: Compile and format**

Run:
```bash
export PATH="$PATH:$HOME/Library/Application Support/Coursier/bin"
sbt -batch "ui/compile" "scalafmtAll" "scalafmtCheckAll"
```
Expected: all `[success]`. If `dataAttr` is unresolved, the `L.*` import is missing.

- [ ] **Step 3: Confirm no existing caller broke**

Run: `grep -rn "Field\." --include="*.scala" modules/ | grep -v "/target/" | grep -v "ui/Field.scala"`
Expected: every hit uses only `apply`, `label`, `description`, or `error`. If a caller passes a `String` where `label` now expects `(text, mods*)`, that call already matched this signature — the signature is unchanged.

- [ ] **Step 4: Stage**

```bash
git add modules/ui/src/main/scala/shadcnscalajs/ui/Field.scala
```

---

### Task 2: `modules/blocks` sbt module

**Files:**
- Modify: `build.sbt`
- Modify: `.franky/scripts/build`
- Create: `modules/blocks/src/main/scala/shadcnscalajs/blocks/.gitkeep` (so the module directory exists before Task 3)

**Interfaces:**
- Consumes: nothing.
- Produces: an sbt project named `blocks` whose sources live at `modules/blocks/src/main/scala/shadcnscalajs/blocks/`, depended on by `site`. Tasks 3–5 put files there.

- [ ] **Step 1: Add the module to `build.sbt`**

Insert immediately after the `ui` project definition (before the `webcomponents` comment block):

```scala
// Blocks — multi-file page/section compositions built from `ui`, served to
// consumers as copy-paste-owned .scala files exactly like components.
lazy val blocks = project
  .in(file("modules/blocks"))
  .enablePlugins(ScalaJSPlugin)
  .settings(jsSettings)
  .settings(noPublish)
  .dependsOn(ui)
```

- [ ] **Step 2: Register it in the aggregate and in `site`**

Change:
```scala
  .aggregate(core, ui, webcomponents, site)
```
to:
```scala
  .aggregate(core, ui, blocks, webcomponents, site)
```

Change `site`'s dependency line from:
```scala
  .dependsOn(ui, webcomponents)
```
to:
```scala
  .dependsOn(ui, blocks, webcomponents)
```

- [ ] **Step 3: Add `blocks` to the franky build script**

In `.franky/scripts/build`, change:
```bash
echo "build: sbt compile (core, ui, webcomponents, site) + Scala.js link"
sbt "core/compile" "ui/compile" "webcomponents/compile" "site/compile" \
    "ui/fastLinkJS" "webcomponents/fastLinkJS" "site/fastLinkJS"
```
to:
```bash
echo "build: sbt compile (core, ui, blocks, webcomponents, site) + Scala.js link"
sbt "core/compile" "ui/compile" "blocks/compile" "webcomponents/compile" "site/compile" \
    "ui/fastLinkJS" "webcomponents/fastLinkJS" "site/fastLinkJS"
```
Do **not** edit `scripts/build` in the repo root — it is a generated wrapper that delegates here.

- [ ] **Step 4: Create the source directory**

```bash
mkdir -p modules/blocks/src/main/scala/shadcnscalajs/blocks
touch modules/blocks/src/main/scala/shadcnscalajs/blocks/.gitkeep
```

- [ ] **Step 5: Verify the module resolves and compiles empty**

Run:
```bash
export PATH="$PATH:$HOME/Library/Application Support/Coursier/bin"
sbt -batch "blocks/compile" "site/compile"
```
Expected: both `[success]`. An empty module compiles fine.

- [ ] **Step 6: Stage**

```bash
git add build.sbt .franky/scripts/build modules/blocks/src/main/scala/shadcnscalajs/blocks/.gitkeep
```

---

### Task 3: `login-01` and `signup-01`

Both are ports. Reproduce structure and class strings exactly; the only translation is React → Laminar.

**Files:**
- Create: `modules/blocks/src/main/scala/shadcnscalajs/blocks/login01/Login01.scala`
- Create: `modules/blocks/src/main/scala/shadcnscalajs/blocks/login01/LoginForm.scala`
- Create: `modules/blocks/src/main/scala/shadcnscalajs/blocks/login01/login-01.registry.json`
- Create: `modules/blocks/src/main/scala/shadcnscalajs/blocks/signup01/Signup01.scala`
- Create: `modules/blocks/src/main/scala/shadcnscalajs/blocks/signup01/SignupForm.scala`
- Create: `modules/blocks/src/main/scala/shadcnscalajs/blocks/signup01/signup-01.registry.json`
- Reference (read-only): `/Users/elam/Projects/ui/apps/v4/registry/new-york-v4/blocks/login-01/` and `signup-01/`

**Interfaces:**
- Consumes: `Field.apply/group/label/description` (Task 1); the `blocks` module (Task 2); existing `Card.apply/header/title/description/content`, `Input.apply`, `Button.apply`, `Button.of`, `Button.ButtonApi.variant`.
- Produces: `shadcnscalajs.blocks.login01.Login01.apply(): HtmlElement`, `shadcnscalajs.blocks.login01.LoginForm.apply(mods: Modifier[HtmlElement]*): HtmlElement`, `shadcnscalajs.blocks.signup01.Signup01.apply(): HtmlElement`, `shadcnscalajs.blocks.signup01.SignupForm.apply(mods: Modifier[HtmlElement]*): HtmlElement`. Task 6 calls the two `Login01.apply()`/`Signup01.apply()` page functions.

- [ ] **Step 1: Write `login01/LoginForm.scala`**

```scala
package shadcnscalajs.blocks.login01

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.*

/** Port of shadcn/ui new-york-v4 `blocks/login-01/components/login-form.tsx`. */
object LoginForm:

  def apply(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      cls := "flex flex-col gap-6",
      mods,
      Card(
        Card.header(
          Card.title("Login to your account"),
          Card.description("Enter your email below to login to your account")
        ),
        Card.content(
          form(
            Field.group(
              Field(
                Field.label("Email", forId := "email"),
                Input(idAttr := "email", typ := "email", placeholder := "m@example.com", required := true)
              ),
              Field(
                div(
                  cls := "flex items-center",
                  Field.label("Password", forId := "password"),
                  a(
                    href := "#",
                    cls := "ml-auto inline-block text-sm underline-offset-4 hover:underline",
                    "Forgot your password?"
                  )
                ),
                Input(idAttr := "password", typ := "password", required := true)
              ),
              Field(
                Button(typ := "submit", "Login"),
                Button.of(_.variant(Button.Variant.Outline), _ => typ := "button", _ => "Login with Google"),
                Field.description(cls := "text-center", "Don't have an account? ", a(href := "#", "Sign up"))
              )
            )
          )
        )
      )
    )
```

- [ ] **Step 2: Write `login01/Login01.scala`**

```scala
package shadcnscalajs.blocks.login01

import com.raquo.laminar.api.L.*

/** Port of shadcn/ui new-york-v4 `blocks/login-01/page.tsx`.
  *
  * Upstream ships this as a file-routed page (`target: app/login/page.tsx`). Laminar has no file-based routing, so it
  * is a plain composition the consumer mounts wherever their own router wants it.
  */
object Login01:

  def apply(): HtmlElement =
    div(
      cls := "flex min-h-svh w-full items-center justify-center p-6 md:p-10",
      div(cls := "w-full max-w-sm", LoginForm())
    )
```

- [ ] **Step 3: Write `login01/login-01.registry.json`**

```json
{
  "name": "login-01",
  "title": "Login 01",
  "type": "scala:block",
  "description": "A simple login form.",
  "categories": ["authentication", "login"],
  "registryDependencies": ["button", "card", "input", "field"],
  "scalaDependencies": [],
  "files": [
    { "path": "login01/Login01.scala", "type": "scala:page" },
    { "path": "login01/LoginForm.scala", "type": "scala:component" }
  ]
}
```

- [ ] **Step 4: Write `signup01/SignupForm.scala`**

```scala
package shadcnscalajs.blocks.signup01

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.*

/** Port of shadcn/ui new-york-v4 `blocks/signup-01/components/signup-form.tsx`. */
object SignupForm:

  def apply(mods: Modifier[HtmlElement]*): HtmlElement =
    Card(
      mods,
      Card.header(
        Card.title("Create an account"),
        Card.description("Enter your information below to create your account")
      ),
      Card.content(
        form(
          Field.group(
            Field(
              Field.label("Full Name", forId := "name"),
              Input(idAttr := "name", typ := "text", placeholder := "John Doe", required := true)
            ),
            Field(
              Field.label("Email", forId := "email"),
              Input(idAttr := "email", typ := "email", placeholder := "m@example.com", required := true),
              Field.description("We'll use this to contact you. We will not share your email with anyone else.")
            ),
            Field(
              Field.label("Password", forId := "password"),
              Input(idAttr := "password", typ := "password", required := true),
              Field.description("Must be at least 8 characters long.")
            ),
            Field(
              Field.label("Confirm Password", forId := "confirm-password"),
              Input(idAttr := "confirm-password", typ := "password", required := true),
              Field.description("Please confirm your password.")
            ),
            Field.group(
              Field(
                Button(typ := "submit", "Create Account"),
                Button.of(_.variant(Button.Variant.Outline), _ => typ := "button", _ => "Sign up with Google"),
                Field.description(
                  cls := "px-6 text-center",
                  "Already have an account? ",
                  a(href := "#", "Sign in")
                )
              )
            )
          )
        )
      )
    )
```

- [ ] **Step 5: Write `signup01/Signup01.scala`**

```scala
package shadcnscalajs.blocks.signup01

import com.raquo.laminar.api.L.*

/** Port of shadcn/ui new-york-v4 `blocks/signup-01/page.tsx`. See Login01's note on file-based routing. */
object Signup01:

  def apply(): HtmlElement =
    div(
      cls := "flex min-h-svh w-full items-center justify-center p-6 md:p-10",
      div(cls := "w-full max-w-sm", SignupForm())
    )
```

- [ ] **Step 6: Write `signup01/signup-01.registry.json`**

```json
{
  "name": "signup-01",
  "title": "Signup 01",
  "type": "scala:block",
  "description": "A signup form with name, email and password fields.",
  "categories": ["authentication", "signup"],
  "registryDependencies": ["button", "card", "input", "field"],
  "scalaDependencies": [],
  "files": [
    { "path": "signup01/Signup01.scala", "type": "scala:page" },
    { "path": "signup01/SignupForm.scala", "type": "scala:component" }
  ]
}
```

- [ ] **Step 7: Compile and format**

Run:
```bash
export PATH="$PATH:$HOME/Library/Application Support/Coursier/bin"
sbt -batch "blocks/compile" "scalafmtAll" "scalafmtCheckAll"
```
Expected: `[success]`. Likely failure modes: `forId` unresolved (it is Laminar's `for` attribute — correct name is `forId`); `Button.of`'s lambda arity (each element is `ButtonApi.type => Modifier[HtmlElement]`, so a constant modifier needs `_ => modifier`).

- [ ] **Step 8: Stage**

```bash
git add modules/blocks/src/main/scala/shadcnscalajs/blocks/login01 modules/blocks/src/main/scala/shadcnscalajs/blocks/signup01
```

---

### Task 4: `otp-01` (authored) and `calendar-01` (port)

`otp-01` has no upstream counterpart — it is authored on `login-01`'s structure. `calendar-01` is a port of the single-file upstream block.

**Files:**
- Create: `modules/blocks/src/main/scala/shadcnscalajs/blocks/otp01/Otp01.scala`
- Create: `modules/blocks/src/main/scala/shadcnscalajs/blocks/otp01/OtpForm.scala`
- Create: `modules/blocks/src/main/scala/shadcnscalajs/blocks/otp01/otp-01.registry.json`
- Create: `modules/blocks/src/main/scala/shadcnscalajs/blocks/calendar01/Calendar01.scala`
- Create: `modules/blocks/src/main/scala/shadcnscalajs/blocks/calendar01/calendar-01.registry.json`

**Interfaces:**
- Consumes: `Field` (Task 1); existing `InputOTP.apply(codeVar: Var[String], length: Int = 6)`, `Calendar.apply(selected: Var[Option[js.Date]], mods: Modifier[HtmlElement]*)`, `Card.*`, `Button`.
- Produces: `shadcnscalajs.blocks.otp01.Otp01.apply(): HtmlElement`, `shadcnscalajs.blocks.otp01.OtpForm.apply(mods: Modifier[HtmlElement]*): HtmlElement`, `shadcnscalajs.blocks.calendar01.Calendar01.apply(): HtmlElement`. Task 6 calls the two page functions.

- [ ] **Step 1: Write `otp01/OtpForm.scala`**

Note the `codeVar` name: a `Var` must not be called `value`, which is also Laminar's input value prop (this is why `InputOTP.scala` uses `codeVar`).

```scala
package shadcnscalajs.blocks.otp01

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.*

/** Authored for shadcn-scalajs — there is no upstream `otp-01` block. Structure follows `login-01`'s card + Field
  * layout, with InputOTP as the input.
  */
object OtpForm:

  def apply(mods: Modifier[HtmlElement]*): HtmlElement =
    val codeVar = Var("")
    div(
      cls := "flex flex-col gap-6",
      mods,
      Card(
        Card.header(
          Card.title("Enter your verification code"),
          Card.description("We sent a 6-digit code to m@example.com")
        ),
        Card.content(
          form(
            Field.group(
              Field(
                cls := "items-center",
                InputOTP(codeVar)
              ),
              Field(
                Button(typ := "submit", "Verify"),
                Field.description(
                  cls := "text-center",
                  "Didn't get a code? ",
                  a(href := "#", "Resend")
                )
              )
            )
          )
        )
      )
    )
```

- [ ] **Step 2: Write `otp01/Otp01.scala`**

```scala
package shadcnscalajs.blocks.otp01

import com.raquo.laminar.api.L.*

/** Authored page composition for the OTP block. See Login01's note on file-based routing. */
object Otp01:

  def apply(): HtmlElement =
    div(
      cls := "flex min-h-svh w-full items-center justify-center p-6 md:p-10",
      div(cls := "w-full max-w-sm", OtpForm())
    )
```

- [ ] **Step 3: Write `otp01/otp-01.registry.json`**

```json
{
  "name": "otp-01",
  "title": "OTP 01",
  "type": "scala:block",
  "description": "A one-time-password verification form.",
  "categories": ["authentication", "otp"],
  "registryDependencies": ["button", "card", "field", "input-otp"],
  "scalaDependencies": [],
  "files": [
    { "path": "otp01/Otp01.scala", "type": "scala:page" },
    { "path": "otp01/OtpForm.scala", "type": "scala:component" }
  ]
}
```

Before staging, confirm the InputOTP registry item is really named `input-otp`:
`ls modules/ui/src/main/scala/shadcnscalajs/ui/ | grep -i otp` and read the `name` field of its `.registry.json`. Use whatever that file declares.

- [ ] **Step 4: Write `calendar01/Calendar01.scala`**

Upstream `calendar-01` is a single default-exported component rendering one Calendar, so there is no separate page file. `js.Date`'s constructor takes `Int` while its getters return `Double` — construct with literals here, and never pass a getter result without `.toInt`.

```scala
package shadcnscalajs.blocks.calendar01

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.*

import scala.scalajs.js

/** Port of shadcn/ui `blocks/calendar-01.tsx` — a single date picker with a bordered container. */
object Calendar01:

  def apply(): HtmlElement =
    val selected = Var(Option(new js.Date(2025, 5, 12)))
    div(
      cls := "flex min-h-svh w-full items-center justify-center p-6 md:p-10",
      Calendar(selected, cls := "rounded-lg border shadow-sm")
    )
```

- [ ] **Step 5: Write `calendar01/calendar-01.registry.json`**

```json
{
  "name": "calendar-01",
  "title": "Calendar 01",
  "type": "scala:block",
  "description": "A single date picker.",
  "categories": ["calendar", "date"],
  "registryDependencies": ["calendar"],
  "scalaDependencies": [],
  "files": [
    { "path": "calendar01/Calendar01.scala", "type": "scala:page" }
  ]
}
```

- [ ] **Step 6: Compile and format**

Run:
```bash
export PATH="$PATH:$HOME/Library/Application Support/Coursier/bin"
sbt -batch "blocks/compile" "scalafmtAll" "scalafmtCheckAll"
```
Expected: `[success]`. If `Calendar(selected, ...)` does not typecheck, read `modules/ui/src/main/scala/shadcnscalajs/ui/Calendar.scala`'s `apply` signature and match it exactly rather than guessing.

- [ ] **Step 7: Stage**

```bash
git add modules/blocks/src/main/scala/shadcnscalajs/blocks/otp01 modules/blocks/src/main/scala/shadcnscalajs/blocks/calendar01
```

---

### Task 5: Registry generator — two roots

**Files:**
- Modify: `modules/site/scripts/build-registry.mjs`

**Interfaces:**
- Consumes: the four sidecars from Tasks 3–4.
- Produces: `modules/site/public/registry/{login-01,signup-01,otp-01,calendar-01}.json`, each with `name`, `title`, `type: "scala:block"`, `description`, `categories`, `registryDependencies`, `scalaDependencies`, and `files[]` where every entry has `content`, `type`, and `target` (prefixed `blocks/`). `index.json` gains those four entries with `description` and `categories`. Task 6 fetches these files; Task 7 installs them.

- [ ] **Step 1: Capture the current output as a baseline**

```bash
cd modules/site && node scripts/build-registry.mjs && cp -r public/registry /tmp/registry-baseline
```
Expected: `Built registry: 60 items -> public/registry`.

- [ ] **Step 2: Rewrite the generator's scan to iterate roots**

Replace the `uiDir` constant and the body of `main()` with:

```js
const repoRoot = path.resolve(__dirname, "../../..");
const outDir = path.resolve(__dirname, "../public/registry");

const roots = [
  {
    dir: path.join(repoRoot, "modules/ui/src/main/scala/shadcnscalajs/ui"),
    type: "scala:ui",
    targetPrefix: "ui/",
    recursive: false
  },
  {
    dir: path.join(repoRoot, "modules/blocks/src/main/scala/shadcnscalajs/blocks"),
    type: "scala:block",
    targetPrefix: "blocks/",
    recursive: true
  }
];

/** Sidecar paths relative to `dir`. Non-recursive roots keep the original flat readdir behaviour. */
async function findSidecars(dir, recursive) {
  let entries;
  try {
    entries = await readdir(dir, { withFileTypes: true });
  } catch (err) {
    if (err.code === "ENOENT") return []; // root not created yet
    throw err;
  }
  const found = [];
  for (const entry of entries) {
    if (entry.isDirectory()) {
      if (!recursive) continue;
      const nested = await findSidecars(path.join(dir, entry.name), recursive);
      found.push(...nested.map((p) => path.join(entry.name, p)));
    } else if (entry.name.endsWith(".registry.json")) {
      found.push(entry.name);
    }
  }
  return found;
}

async function main() {
  const indexItems = [];
  await mkdir(outDir, { recursive: true });

  for (const root of roots) {
    const sidecars = await findSidecars(root.dir, root.recursive);

    for (const rel of sidecars) {
      const sidecar = JSON.parse(await readFile(path.join(root.dir, rel), "utf-8"));

      const files = await Promise.all(
        sidecar.files.map(async (file) => {
          const content = await readFile(path.join(root.dir, file.path), "utf-8");
          return { content, type: file.type ?? root.type, target: `${root.targetPrefix}${file.path}` };
        })
      );

      const registryItem = {
        $schema: "https://shadcn-scalajs.dev/schema/registry-item.json",
        name: sidecar.name,
        title: sidecar.title,
        type: sidecar.type ?? root.type,
        registryDependencies: sidecar.registryDependencies ?? [],
        scalaDependencies: sidecar.scalaDependencies ?? [],
        files
      };
      if (sidecar.description) registryItem.description = sidecar.description;
      if (sidecar.categories) registryItem.categories = sidecar.categories;

      await writeFile(path.join(outDir, `${sidecar.name}.json`), JSON.stringify(registryItem, null, 2));

      const indexEntry = {
        name: sidecar.name,
        title: sidecar.title,
        type: sidecar.type ?? root.type,
        registryDependencies: sidecar.registryDependencies ?? []
      };
      if (sidecar.description) indexEntry.description = sidecar.description;
      if (sidecar.categories) indexEntry.categories = sidecar.categories;
      indexItems.push(indexEntry);
    }
  }

  await writeFile(path.join(outDir, "index.json"), JSON.stringify(indexItems, null, 2));

  console.log(`Built registry: ${indexItems.length} items -> ${path.relative(process.cwd(), outDir)}`);
}
```

Keep the existing imports, the `__dirname` definition, and the trailing `main().catch(...)` exactly as they are.

Note: component sidecars declare `path` values with no directory (e.g. `Button.scala`), and per-file `type` (`scala:ui`); block sidecars declare `path` values like `login01/Login01.scala`. Since `path.join(root.dir, file.path)` handles both, one code path serves both roots.

- [ ] **Step 3: Regenerate and prove component output is unchanged**

```bash
cd modules/site && node scripts/build-registry.mjs
for f in /tmp/registry-baseline/*.json; do
  n=$(basename "$f")
  [ "$n" = "index.json" ] && continue
  diff -q "$f" "public/registry/$n" || echo "CHANGED: $n"
done
```
Expected: `Built registry: 64 items`, and **no** `CHANGED:` lines. If any component item changed, the root config is wrong — fix it rather than accepting the diff.

- [ ] **Step 4: Verify one block item**

```bash
cd modules/site && node -e "
const j=require('./public/registry/login-01.json');
console.log(j.type, j.description, JSON.stringify(j.categories));
console.log(j.files.map(f=>f.target+' ('+f.type+') '+f.content.length+'b').join('\n'));
"
```
Expected: `scala:block A simple login form. ["authentication","login"]`, then two files with targets `blocks/login01/Login01.scala` and `blocks/login01/LoginForm.scala`, each with non-zero content length.

- [ ] **Step 5: Stage**

```bash
git add modules/site/scripts/build-registry.mjs modules/site/public/registry
```

---

### Task 6: Site — block list, three routes, nav link

**Files:**
- Create: `modules/site/src/main/scala/shadcnscalajs/site/Blocks.scala`
- Create: `modules/site/src/main/scala/shadcnscalajs/site/BlocksIndexPage.scala`
- Create: `modules/site/src/main/scala/shadcnscalajs/site/BlockDocsPage.scala`
- Create: `modules/site/src/main/scala/shadcnscalajs/site/BlockPreviewPage.scala`
- Modify: `modules/site/src/main/scala/shadcnscalajs/site/Main.scala` (router near line 38; the three page headers' nav link lists)

**Interfaces:**
- Consumes: `Login01.apply()`, `Signup01.apply()`, `Otp01.apply()`, `Calendar01.apply()` (Tasks 3–4); the registry JSON from Task 5.
- Produces: `Blocks.all: List[Blocks.Meta]` where `Meta(name: String, title: String, description: String, categories: List[String])`; `Blocks.render(name: String): Option[HtmlElement]`; `BlocksIndexPage()`, `BlockDocsPage(name: String)`, `BlockPreviewPage(name: String)` all returning `HtmlElement`.

- [ ] **Step 1: Write `Blocks.scala`**

```scala
package shadcnscalajs.site

import com.raquo.laminar.api.L.*
import shadcnscalajs.blocks.calendar01.Calendar01
import shadcnscalajs.blocks.login01.Login01
import shadcnscalajs.blocks.otp01.Otp01
import shadcnscalajs.blocks.signup01.Signup01

/** The docs site's view of the block catalog.
  *
  * Hand-maintained, matching `componentNavList`'s house style. It can drift from the `*.registry.json` sidecars in
  * `modules/blocks`; generating it from those sidecars is a tracked follow-up.
  */
object Blocks:

  final case class Meta(name: String, title: String, description: String, categories: List[String])

  val all: List[Meta] = List(
    Meta("login-01", "Login 01", "A simple login form.", List("authentication", "login")),
    Meta("signup-01", "Signup 01", "A signup form with name, email and password fields.", List("authentication", "signup")),
    Meta("otp-01", "OTP 01", "A one-time-password verification form.", List("authentication", "otp")),
    Meta("calendar-01", "Calendar 01", "A single date picker.", List("calendar", "date"))
  )

  def find(name: String): Option[Meta] = all.find(_.name == name)

  /** The live block itself, for the preview route. */
  def render(name: String): Option[HtmlElement] = name match
    case "login-01"    => Some(Login01())
    case "signup-01"   => Some(Signup01())
    case "otp-01"      => Some(Otp01())
    case "calendar-01" => Some(Calendar01())
    case _             => None

  /** Categories in first-seen order, each with its blocks. */
  def byCategory: List[(String, List[Meta])] =
    all.flatMap(m => m.categories.headOption.map(_ -> m)).groupBy(_._1).toList.map { case (c, pairs) =>
      c -> pairs.map(_._2)
    }.sortBy(_._1)
```

- [ ] **Step 2: Write `BlockPreviewPage.scala`**

```scala
package shadcnscalajs.site

import com.raquo.laminar.api.L.*

/** `/blocks/<name>/preview` — chrome-less: mounts only the block, so the docs page can embed it in an iframe at a real
  * viewport without the docs header/sidebar interfering.
  */
object BlockPreviewPage:

  def apply(name: String): HtmlElement =
    Blocks
      .render(name)
      .getOrElse(
        div(cls := "flex min-h-svh items-center justify-center text-sm text-muted-foreground", s"Unknown block: $name")
      )
```

- [ ] **Step 3: Write `BlocksIndexPage.scala`**

Use only bare `div`/`a`/`h1`/`h2`/`p`/`span` plus `sectionTag` if a section wrapper is wanted — do not use bare `section`, `header`, `nav`, or `main`.

```scala
package shadcnscalajs.site

import com.raquo.laminar.api.L.*

/** `/blocks` — category-grouped directory of every block. */
object BlocksIndexPage:

  def apply(): HtmlElement =
    div(
      cls := "mx-auto w-full max-w-5xl px-6 py-12",
      div(
        cls := "border-b pb-8",
        p(cls := "mb-2 text-sm font-medium text-primary", "Laminar block library"),
        h1(cls := "text-4xl font-semibold tracking-tight", "Blocks"),
        p(
          cls := "mt-3 text-lg text-muted-foreground",
          "Ready-made page and section compositions built from shadcn-scalajs components. Install one with the CLI and own every line."
        )
      ),
      div(
        cls := "mt-10 flex flex-col gap-10",
        Blocks.byCategory.map { case (category, metas) =>
          div(
            h2(cls := "text-sm font-medium tracking-wide text-muted-foreground uppercase", category),
            div(
              cls := "mt-4 grid grid-cols-1 gap-4 sm:grid-cols-2",
              metas.map { meta =>
                a(
                  href := s"/blocks/${meta.name}",
                  cls := "flex flex-col gap-1 rounded-lg border bg-card p-4 transition-colors hover:border-primary",
                  span(cls := "text-sm font-medium text-foreground", meta.title),
                  span(cls := "text-sm text-muted-foreground", meta.description)
                )
              }
            )
          )
        }
      )
    )
```

- [ ] **Step 4: Write `BlockDocsPage.scala`**

The Code tab reads the generated registry JSON so the shown source cannot drift from the real files. Note the explicit type parameter on the first `.then` — Scala's inference does not widen `js.Promise[String]` on its own here.

```scala
package shadcnscalajs.site

import com.raquo.laminar.api.L.*
import org.scalajs.dom

import scala.scalajs.js
import scala.scalajs.js.JSON

/** `/blocks/<name>` — Preview/Code tabs, file tree, install command. */
object BlockDocsPage:

  private final case class SourceFile(target: String, content: String)

  def apply(name: String): HtmlElement =
    val meta = Blocks.find(name)
    val files = Var(List.empty[SourceFile])
    val showCode = Var(false)
    val iframeKey = Var(0)

    fetchFiles(name, files)

    div(
      cls := "mx-auto w-full max-w-5xl px-6 py-12",
      h1(cls := "text-3xl font-semibold tracking-tight", meta.map(_.title).getOrElse(name)),
      p(cls := "mt-2 text-base text-muted-foreground", meta.map(_.description).getOrElse("")),
      div(
        cls := "mt-6 flex items-center gap-2",
        button(
          typ := "button",
          cls := "inline-flex h-8 items-center rounded-md border px-3 text-sm font-medium",
          cls.toggle("bg-accent text-accent-foreground") <-- showCode.signal.map(!_),
          onClick --> { _ => showCode.set(false) },
          "Preview"
        ),
        button(
          typ := "button",
          cls := "inline-flex h-8 items-center rounded-md border px-3 text-sm font-medium",
          cls.toggle("bg-accent text-accent-foreground") <-- showCode.signal,
          onClick --> { _ => showCode.set(true) },
          "Code"
        ),
        button(
          typ := "button",
          cls := "ml-auto inline-flex h-8 items-center rounded-md border px-3 text-sm font-medium",
          onClick --> { _ => iframeKey.update(_ + 1) },
          "Refresh"
        ),
        a(
          href := s"/blocks/$name/preview",
          target := "_blank",
          rel := "noopener",
          cls := "inline-flex h-8 items-center rounded-md border px-3 text-sm font-medium",
          "Open in New Tab"
        )
      ),
      div(
        cls := "mt-4 overflow-hidden rounded-lg border",
        cls.toggle("hidden") <-- showCode.signal,
        child <-- iframeKey.signal.map { key =>
          iframe(
            cls := "h-[640px] w-full bg-background",
            title := s"$name preview",
            src := s"/blocks/$name/preview?r=$key"
          )
        }
      ),
      div(
        cls := "mt-4 flex flex-col gap-4",
        cls.toggle("hidden") <-- showCode.signal.map(!_),
        children <-- files.signal.map(_.map { f =>
          div(
            cls := "overflow-hidden rounded-lg border",
            div(cls := "border-b bg-muted px-4 py-2 font-mono text-xs text-muted-foreground", f.target),
            pre(cls := "overflow-x-auto p-4 text-sm", code(f.content))
          )
        })
      ),
      div(
        cls := "mt-10",
        h2(cls := "text-lg font-semibold", "Installation"),
        pre(
          cls := "mt-3 overflow-x-auto rounded-lg border bg-muted p-4 text-sm",
          code(s"npx shadcn-scalajs add $name")
        )
      )
    )

  private def fetchFiles(name: String, into: Var[List[SourceFile]]): Unit =
    dom
      .fetch(s"/registry/$name.json")
      .`then`[String](_.text())
      .`then`[Unit] { text =>
        val parsed = JSON.parse(text)
        val arr = parsed.files.asInstanceOf[js.Array[js.Dynamic]]
        into.set(arr.toList.map(f => SourceFile(f.target.asInstanceOf[String], f.content.asInstanceOf[String])))
      }
```

`iframe` is a bare Laminar tag (verified against the 17.2.1 sources: `lazy val iframe: HtmlTag[dom.HTMLIFrameElement]`), so use it directly — do **not** add a `Tag` suffix. `form` and `label` are likewise bare.

- [ ] **Step 5: Wire the router in `Main.scala`**

Find the routing block near line 38 (`if pathname == "/components" || pathname == "/components/" then componentsGalleryPage()`). Add these branches **before** the final `else app()`, ordering the `/preview` check first so it is not shadowed:

```scala
      else if pathname.startsWith("/blocks/") && pathname.endsWith("/preview") then
        BlockPreviewPage(pathname.stripPrefix("/blocks/").stripSuffix("/preview"))
      else if pathname == "/blocks" || pathname == "/blocks/" then BlocksIndexPage()
      else if pathname.startsWith("/blocks/") then BlockDocsPage(pathname.stripPrefix("/blocks/").stripSuffix("/"))
```

- [ ] **Step 6: Add the nav link to all three headers**

In `Main.scala` there are three separate headers, each containing `a(cls := btnGhost, href := "/components", "Components")`. After **each** of those three occurrences add:

```scala
              a(cls := btnGhost, href := "/blocks", "Blocks"),
```

Match the surrounding indentation. Verify with `grep -c 'href := "/blocks", "Blocks"' modules/site/src/main/scala/shadcnscalajs/site/Main.scala` — expected `3`.

- [ ] **Step 7: Compile, format, and link**

```bash
export PATH="$PATH:$HOME/Library/Application Support/Coursier/bin"
sbt -batch "site/compile" "scalafmtAll" "scalafmtCheckAll" "site/fastLinkJS"
```
Expected: `[success]`.

- [ ] **Step 8: Browser check**

```bash
cd modules/site && npm run dev
```
Then confirm by hand or with the playwright harness in `/tmp/alert-verify`:
- `/blocks` lists four blocks under their categories, every link works.
- `/blocks/login-01` shows Preview (iframe renders the centred login card), Refresh reloads it, "Open in New Tab" opens the bare route, Code shows two files with real Scala source and the correct `blocks/login01/...` targets.
- `/blocks/login-01/preview` renders the block alone, no docs chrome.
- Repeat for `signup-01`, `otp-01`, `calendar-01`.
- Toggle dark mode on one block page.

- [ ] **Step 9: Stage**

```bash
git add modules/site/src/main/scala/shadcnscalajs/site
```

---

### Task 7: `scripts/test` covers a block install

`./scripts/test` currently only smoke-tests a component `add`. A block install exercises nested targets plus transitive component resolution, which is the part most likely to regress.

**Files:**
- Modify: `.franky/scripts/test`

**Interfaces:**
- Consumes: the registry output from Task 5.
- Produces: a failing exit code if `add login-01` stops writing the block's own files or its transitive component files.

- [ ] **Step 1: Read the existing script**

Run: `cat .franky/scripts/test`
Identify how it creates its temp directory, runs `init`, runs `add`, and asserts files exist. Reuse those exact mechanisms — do not restructure the script.

- [ ] **Step 2: Add a block case using the script's existing style**

After the existing component assertions, add an `add login-01` run against the same temp project and assert **all** of:

```bash
blocks/login01/Login01.scala
blocks/login01/LoginForm.scala
ui/Button.scala
ui/Card.scala
ui/Input.scala
ui/Field.scala
```

(paths relative to the configured `sourceDir`). The four `ui/*.scala` files are the point of the test: they prove `registryDependencies` resolution pulled components in transitively, not just the block's own two files. Use the same assertion helper/idiom the script already uses for components, and make a missing file exit non-zero.

- [ ] **Step 3: Run it**

Run: `./.franky/scripts/test`
Expected: exit 0, with the new block assertions visible in the output. If `ui/Field.scala` is missing, `login-01.registry.json`'s `registryDependencies` is wrong — fix the sidecar, re-run `node modules/site/scripts/build-registry.mjs`, then re-run.

- [ ] **Step 4: Stage**

```bash
git add .franky/scripts/test
```

---

### Task 8: Full verification and memory update

**Files:**
- Modify: `.franky/memory/PROGRESS.md`
- Modify: `.franky/memory/decisions.log`
- Modify: `AGENTS.md` (Layout section; new-block checklist)

**Interfaces:**
- Consumes: everything above.
- Produces: a green `franky verify` and updated project memory.

- [ ] **Step 1: Run the full gate**

```bash
franky verify
```
Expected: `verify PASSED`, and `passed: true` in `.franky/verify-report.json`. On failure, read `steps[].excerpt` and fix the code — do not weaken the check.

- [ ] **Step 2: Prove the blocks do not depend on pack or basecoat CSS**

With the dev server running, load `/blocks/login-01/preview`, remove `data-style-pack` from every element that has it, and confirm the card still renders with correct spacing, border radius and muted description colour. Script it with the playwright harness in `/tmp/alert-verify` (system Chrome via `channel: 'chrome'`). Anything that collapses without the pack is a component fidelity bug — fix it or record it as a follow-up.

- [ ] **Step 3: Update `AGENTS.md`**

Add `modules/blocks/` to the Layout block with a one-line description, and add a "New block checklist" mirroring the existing new-component checklist: directory + sidecar in `modules/blocks`, entry in `Blocks.all`, case in `Blocks.render`, regenerate the registry, browser-check all three routes.

- [ ] **Step 4: Update `.franky/memory/PROGRESS.md`**

Add a Done entry for the blocks pipeline and the four blocks. Add to Next: the Sidebar rebuild sub-project (needed before `sidebar-01`), generating `Blocks.all` from the sidecars, and `dashboard-01`. Keep the file under ~5,000 characters — consolidate stale Done entries if it would exceed that.

- [ ] **Step 5: Append to `.franky/memory/decisions.log`**

Record: blocks live in their own module because they are multi-file and `modules/ui` means components; block page files are mountable compositions rather than routed pages because Laminar has no file-based routing; the Code tab reads generated registry JSON instead of hand-maintained literals; `sidebar-01` was cut because Sidebar is a 19-line stub against upstream's 726 lines.

- [ ] **Step 6: Report, do not commit**

Summarise changed files, verification results, and what was staged. Leave committing to the user.

---

## Self-Review

**Spec coverage:** Goal 1 → Task 2. Goal 2 → Task 5. Goal 3 → Task 6. Goal 4 → Tasks 3, 4 (plus Task 1, which the spec calls out as in-scope). Goal 5 → Task 7. Spec's verification section → Task 8. Spec's fidelity-risk policy → Task 8 Step 2.

**Placeholders:** none — every code step carries literal content. Task 7 Step 2 deliberately says "reuse the script's existing idiom" instead of inlining bash, because the file's assertion style must be read first; the exact file list it must assert is given.

**Type consistency:** `Blocks.Meta(name, title, description, categories)` is used consistently by `BlocksIndexPage` (`meta.title`, `meta.description`, `meta.name`) and `BlockDocsPage` (`meta.map(_.title)`). `Blocks.render` returns `Option[HtmlElement]`, consumed by `BlockPreviewPage` via `getOrElse`. `Field.group` is defined in Task 1 and called in Tasks 3 and 4. Page objects expose `apply(): HtmlElement` and are called as `Login01()` etc. in `Blocks.render`. Registry item names (`login-01`, `signup-01`, `otp-01`, `calendar-01`) match between sidecars, `Blocks.all`, `Blocks.render`, and Task 7's assertions.
