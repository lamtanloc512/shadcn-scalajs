---
name: laminar-best-practices
description: Guides idiomatic Laminar and Airstream patterns for Scala.js reactive UIs. Use when writing, reviewing, or refactoring Laminar components, Airstream signals/streams, Var state management, or Scala.js frontend code.
---

## Contents (Priority Order)

1. [Flattening Observables](#flattening-observables) - `flatMapSwitch` vs `flatMapMerge` (critical)
2. [Signal.changes Gotcha](#signalchanges-gotcha) - creates new stream each call
3. [Derived Signals](#derived-signals) - `.not`, `.distinct`, lambda shorthand
4. [Conditional Rendering](#conditional-rendering) - `child <--`, `child.maybe <--`, `splitBoolean`/`splitOption`
5. [Rendering Lists with split](#rendering-lists-with-split) - efficient list rendering with memoization
6. [Component Structure](#component-structure) - class-based reusable components
7. [Combining Signals](#combining-signals) - `combineWithFn`, `compose`
7. [Sampling Signal Values](#sampling-signal-values) - `withCurrentValueOf` vs `now()`
8. [Var Updates](#var-updates) - `update()`, `tryUpdater`, `Var.set()` batching
9. [Error Handling](#error-handling) - `recover`, `recoverToTry`
10. [Var Signal Extraction](#var-signal-extraction-project-convention) - extract `var.signal` to named val
11. [Observer Subscriptions](#observer-subscriptions-project-convention) - explicit `Observer(...)`

## Flattening Observables

**Always use explicit flattening strategy.** Generic `flatMap` is intentionally disabled.

```scala
// Good - explicit intent
stream.flatMapSwitch(x => fetchData(x))  // Switch: cancel previous, use latest
stream.flatMapMerge(x => fetchData(x))   // Merge: run all concurrently

// Bad - won't compile (by design)
stream.flatMap(x => fetchData(x))
```

**When to use which:**
- `flatMapSwitch` - User input, search queries, navigation (cancel stale requests)
- `flatMapMerge` - Independent operations that should all complete

## Signal.changes Gotcha

`.changes` is a `def`, not a `lazy val`. Each call creates a **new stream instance**.

```scala
// Bad - different stream instances!
signal.changes.foreach(f1)
signal.changes.foreach(f2)  // Won't see same events as above

// Good - capture once
val changes = signal.changes
changes.foreach(f1)
changes.foreach(f2)
```

## Derived Signals

Use `.not` to invert `Signal[Boolean]`:

```scala
// Good
val isHidden: Signal[Boolean] = isVisible.not.distinct

// Bad
val isHidden: Signal[Boolean] = isVisible.map(!_)
```

When chaining, use `.not` early rather than negating inside lambda:

```scala
// Good
child.maybe <-- isFetching.not.map(Option.when(_)(renderContent))

// Bad
child.maybe <-- isFetching.map(Option.when(!_)(renderContent))
```

Always use `.distinct` when deriving `Signal[Primitive]` (String, Boolean, Int, Double):

```scala
// Good - prevents redundant updates
val isValid: Signal[Boolean] = formData.map(_.isValid).distinct
val userName: Signal[String] = user.map(_.name).distinct

// Bad - emits on every parent update even if value unchanged
val isValid: Signal[Boolean] = formData.map(_.isValid)
```

### Lambda Shorthand

Prefer `_` placeholder over named parameters for simple lambdas:

```scala
// Good
child.maybe <-- isFetching.map(Option.when(_)(renderContent))

// Bad
child.maybe <-- isFetching.map { isFetching => Option.when(isFetching)(renderContent) }
```

## Conditional Rendering

Render based on `Signal[Boolean]`:

```scala
child <-- isVisible.map(if (_) renderContent else emptyNode)
```

For dynamic text, use `text <--` (not `child <--`):

```scala
// Good
text <-- isLoading.map(if (_) "Loading..." else "Load More")

// Bad - child <-- is not for text
child <-- isLoading.map { case true => "Loading..." case false => "Load More" }
```

For `Signal[Option[A]]`:

```scala
child.maybe <-- maybeUser.map(_.map(renderUser))
```

### Expensive Subtrees

Only use `splitBoolean`/`splitOption` for expensive renders (large trees, heavy computation):

```scala
// Good - expensive dashboard
child <-- isVisible.splitBoolean(
  whenTrue = _ => renderExpensiveDashboard,
  whenFalse = _ => emptyNode
)

// Bad - overkill for simple button
child <-- isVisible.splitBoolean(whenTrue = _ => button("Click"), whenFalse = _ => emptyNode)
```

## Rendering Lists with split

Use `split` for efficient list rendering. The `project` function is **called only once per key** - subsequent updates to that item emit on the provided signal.

```scala
case class Todo(id: String, text: String, done: Boolean)

// Good - split with unique key, project called once per id
children <-- todosSignal.split(_.id) { (id, initialTodo, todoSignal) =>
  renderTodo(id, todoSignal)  // todoSignal emits when THIS todo changes
}

// Bad - recreates all elements on every list change
children <-- todosSignal.map(_.map(todo => renderTodo(todo.id, todo)))
```

### Key Function Requirements

Keys must be **unique** and **stable**:

```scala
// Good - stable unique identifier
items.split(_.id)(...)

// Bad - index changes when items reorder (use splitByIndex if you need indices)
items.split(items.indexOf(_))(...)

// Bad - non-unique keys cause warnings and undefined behavior
items.split(_.category)(...)  // Multiple items may share category
```

### Signal vs Var split

Signal split gives read-only `Signal[Input]`. Var split gives writable `Var[Input]`:

```scala
// Signal.split - read-only child signal
todosSignal.split(_.id) { (id, initial, todoSignal: Signal[Todo]) =>
  // Can only read todoSignal
}

// Var.split - writable child var (updates propagate to parent)
todosVar.split(_.id) { (id, initial, todoVar: Var[Todo]) =>
  // Can write to todoVar, changes update parent list
  checkbox(checked <-- todoVar.signal.map(_.done),
           onClick --> Observer(_ => todoVar.update(_.copy(done = !_.done))))
}
```

### splitByIndex

When items lack natural keys, use index (but reordering recreates elements):

```scala
itemsSignal.splitByIndex { (index, initialItem, itemSignal) =>
  div(s"Item $index: ", text <-- itemSignal.map(_.name))
}
```

### Gotchas

1. **Duplicate keys** trigger warnings and cause undefined behavior
2. **Default `distinctCompose`** filters updates - child signal only fires when that specific item changes
3. **Memoization lifecycle** - when item removed from list, its memoized state is discarded

## Component Structure

Define reusable components as classes extending a base trait with a `render()` method:

```scala
trait LaminarComponent {
  def render(): HtmlElement
}

class UserCard(user: User) extends LaminarComponent {
  def render(): HtmlElement = div(
    cls := "user-card",
    renderAvatar,
    renderInfo,
    renderActions
  )

  private def renderAvatar: HtmlElement = img(src := user.avatarUrl)

  private def renderInfo: HtmlElement = div(
    h3(user.name),
    span(user.email)
  )

  private def renderActions: HtmlElement = div(
    button("Follow"),
    button("Message")
  )
}

// Usage
div(UserCard(user).render())
```

### With Reactive State

For components with internal state, define Vars and extract signals:

```scala
class Counter(initial: Int = 0) extends LaminarComponent {
  private val countVar = Var(initial)
  private val countSignal = countVar.signal

  def render(): HtmlElement = div(
    renderDisplay,
    renderControls
  )

  private def renderDisplay: HtmlElement =
    span(text <-- countSignal.map(_.toString))

  private def renderControls: HtmlElement = div(
    button("-", onClick --> Observer(_ => countVar.update(_ - 1))),
    button("+", onClick --> Observer(_ => countVar.update(_ + 1)))
  )
}
```

### Exposing Component State

When parent needs access to component state, expose as public vals:

```scala
class Toggle(initial: Boolean = false) extends LaminarComponent {
  private val checkedVar = Var(initial)
  val checkedSignal: Signal[Boolean] = checkedVar.signal  // public read access

  def render(): HtmlElement = input(
    typ := "checkbox",
    checked <-- checkedSignal,
    onClick.mapToChecked --> checkedVar.writer
  )
}

// Parent can observe toggle state
val toggle = Toggle()
div(
  toggle.render(),
  child <-- toggle.checkedSignal.map(if (_) span("ON") else span("OFF"))
)
```

### Avoid Passing Var as Props

Pass `Signal` and `Observer` separately instead of `Var`. This enforces clear read/write boundaries:

```scala
// Good - separate read (Signal) and write (Observer) concerns
class TextInput(
  value: Signal[String],
  onInput: Observer[String]
) extends LaminarComponent {
  def render(): HtmlElement = input(
    controlled(
      value <-- value,
      onInput.mapToValue --> onInput
    )
  )
}

// Usage - parent controls the state
val textVar = Var("")
TextInput(textVar.signal, textVar.writer).render()

// Bad - passing Var couples component to specific state implementation
class TextInput(valueVar: Var[String]) extends LaminarComponent {
  def render(): HtmlElement = input(
    controlled(
      value <-- valueVar.signal,
      onInput.mapToValue --> valueVar.writer
    )
  )
}
```

**Why**:
- Component doesn't need write access? Pass only `Signal`
- Parent can transform/filter writes via custom `Observer`
- Easier to test and reuse

## Combining Signals

Prefer `combineWithFn` over `combineWith(...).map(...)`:

```scala
// Good
val fullName: Signal[String] = firstName.combineWithFn(lastName)((f, l) => s"$f $l")

// Bad - intermediate tuple overhead
val fullName: Signal[String] = firstName.combineWith(lastName).map { case (f, l) => s"$f $l" }
```

Use `compose` for chained transformations:

```scala
// Good - type-safe composition
signal.compose(_.map(...).filter(...).distinct)
signal.composeChanges(_.delay(100))  // Transform changes stream only
```

## Sampling Signal Values

Prefer `withCurrentValueOf` over `now()` in reactive pipelines:

```scala
// Good - reactive: samples count when name changes
val greeting: Signal[String] = nameSignal
  .withCurrentValueOf(countSignal)
  .map { case (name, count) => s"$name clicked $count times" }

// Bad - breaks reactive paradigm, hidden dependency
val greeting: Signal[String] = nameSignal.map { name =>
  s"$name clicked ${countVar.now()} times"
}
```

**Why**: `now()` inside combinators creates hidden dependencies Airstream can't track.

## Var Updates

Prefer `update()` over `now()` + `set()`:

```scala
// Good - atomic
button.events(onClick) --> Observer(_ => countVar.update(_ + 1))

// Bad - two operations
button.events(onClick) --> Observer { _ =>
  countVar.set(countVar.now() + 1)
}
```

Use `tryUpdater` when Var may be in failed state:

```scala
// Good - handles failed Var
myVar.tryUpdater { (currTry, next) =>
  currTry.map(curr => curr ++ next)
}

// Bad - throws if Var failed
myVar.updater((curr, next) => curr ++ next)
```

Batch multiple Var updates with `Var.set()`:

```scala
// Good - single transaction, no intermediate states
Var.set(
  itemsVar -> List.empty,
  pageVar -> 0
)

// Bad - separate updates
itemsVar.set(List.empty)
pageVar.set(0)
```

## Error Handling

Use `recover` for graceful error handling:

```scala
// Recover with fallback value
stream.recover { case _: NetworkError => Some(cachedValue) }

// Capture errors as data
stream.recoverToTry      // Signal[Try[A]]
stream.recoverToEither   // Signal[Either[Throwable, A]]
```

For `Signal[Try[A]]` use extension methods:

```scala
trySignal.mapSuccess(transform)           // Map Success only
trySignal.mapFailure(transformErr)        // Map Failure only
trySignal.foldTry(onError, onSuccess)     // Handle both
```

## Var Signal Extraction (Project Convention)

When using a Var's signal in multiple places, extract it to a named val:

```scala
// Good - extract signal once, use named val
val countVar = Var(0)
val countSignal = countVar.signal

div(
  text <-- countSignal.map(_.toString),
  cls <-- countSignal.map(c => if (c > 10) "high" else "low"),
  button(onClick --> Observer(_ => countVar.update(_ + 1)))
)

// Bad - repeated .signal calls scattered through code
val countVar = Var(0)

div(
  text <-- countVar.signal.map(_.toString),
  cls <-- countVar.signal.map(c => if (c > 10) "high" else "low"),
  button(onClick --> Observer(_ => countVar.update(_ + 1)))
)
```

**Why**: Clearer separation between read (signal) and write (var) operations. Makes dependencies explicit.

## Observer Subscriptions (Project Convention)

Use explicit `Observer(...)` on the right-hand side of `-->`. Type parameter optional:

```scala
// Good - explicit Observer wrapper
filterSignal --> Observer(filter => fetchData(filter))
filterSignal --> Observer[Filter](filter => fetchData(filter))  // type optional

// Bad - bare function overload, less explicit
filterSignal --> (filter => fetchData(filter))
```
