package shadcnscalajs.ui

import com.raquo.laminar.api.L
import com.raquo.laminar.api.L.*
import com.raquo.laminar.codecs.BooleanAsAttrPresenceCodec
import org.scalajs.dom

/** shadcn/ui Questionnaire — one question at a time, with progress, choices or free text, validation, and
  * previous/skip/next/submit actions. Built for agents that ask the user to clarify before acting.
  *
  * Upstream delegates the state machine to `@shadcn/react/questionnaire`; here it is a small [[State]] holding the
  * step, the answers, and the current error. [[apply]] renders the whole flow from a list of [[Question]]s; the part
  * builders ([[choice]], [[actions]], ...) are public for callers that lay a step out by hand.
  */
object Questionnaire:

  private val kbdTag = htmlTag("kbd")

  enum Kind derives CanEqual:
    /** Radio choices — one answer. */
    case Single

    /** Checkbox choices — any number of answers. */
    case Multiple

    /** A free-text input. */
    case Text

  final case class Choice(
      value: String,
      label: String,
      description: Option[String] = None,
      shortcut: Option[String] = None,
      disabled: Boolean = false
  )

  final case class Question(
      id: String,
      title: String,
      kind: Kind = Kind.Single,
      choices: Seq[Choice] = Nil,
      description: Option[String] = None,
      required: Boolean = true,
      skippable: Boolean = false,
      placeholder: String = ""
  )

  type Answers = Map[String, Seq[String]]

  final class State(val questions: Seq[Question]):
    val step: Var[Int] = Var(0)
    val answers: Var[Answers] = Var(Map.empty)
    val error: Var[Option[String]] = Var(None)
    val submitted: Var[Boolean] = Var(false)

    def current: Signal[Question] = step.signal.map(questions(_))
    def isFirst: Signal[Boolean] = step.signal.map(_ == 0)
    def isLast: Signal[Boolean] = step.signal.map(_ == questions.size - 1)
    def answer(id: String): Signal[Seq[String]] = answers.signal.map(_.getOrElse(id, Nil))

    def set(id: String, values: Seq[String]): Unit =
      answers.update(_.updated(id, values))
      error.set(None)

    def toggle(question: Question, value: String): Unit =
      val existing = answers.now().getOrElse(question.id, Nil)
      question.kind match
        case Kind.Multiple =>
          set(question.id, if existing.contains(value) then existing.filterNot(_ == value) else existing :+ value)
        case _ => set(question.id, Seq(value))

    private def valid(question: Question): Boolean =
      !question.required || answers.now().getOrElse(question.id, Nil).exists(_.trim.nonEmpty)

    private def missing(question: Question): String =
      if question.kind == Kind.Text then "Enter an answer to continue." else "Choose an option to continue."

    def previous(): Unit =
      error.set(None)
      step.update(s => math.max(0, s - 1))

    def skip(): Unit =
      error.set(None)
      step.update(s => math.min(questions.size - 1, s + 1))

    def next(): Unit =
      val question = questions(step.now())
      if valid(question) then
        error.set(None)
        step.update(s => math.min(questions.size - 1, s + 1))
      else error.set(Some(missing(question)))

    /** Validates the current step and, when it passes, marks the flow submitted and returns the answers. */
    def submit(): Option[Answers] =
      val question = questions(step.now())
      if valid(question) then
        error.set(None)
        submitted.set(true)
        Some(answers.now())
      else
        error.set(Some(missing(question)))
        None

    def reset(): Unit =
      step.set(0)
      answers.set(Map.empty)
      error.set(None)
      submitted.set(false)

  def state(questions: Seq[Question]): State =
    require(questions.nonEmpty, "Questionnaire needs at least one question")
    new State(questions)

  /** The full flow. `done` replaces the form once submitted. */
  def apply(
      state: State,
      onSubmit: Answers => Unit = _ => (),
      done: => HtmlElement = div()
  )(mods: Modifier[HtmlElement]*): HtmlElement =
    root(
      // Number and letter shortcuts select choices on the visible step, as upstream does.
      onKeyDown --> { (ev: dom.KeyboardEvent) =>
        val tag = ev.target.asInstanceOf[dom.Element].tagName.toLowerCase
        if tag != "input" && tag != "textarea" && !ev.metaKey && !ev.ctrlKey && !ev.altKey then
          val question = state.questions(state.step.now())
          question.choices
            .find(c => !c.disabled && c.shortcut.exists(_.equalsIgnoreCase(ev.key)))
            .foreach { c =>
              ev.preventDefault()
              state.toggle(question, c.value)
            }
      },
      child <-- state.submitted.signal.combineWith(state.step.signal).map { (isDone, index) =>
        if isDone then done
        else
          val question = state.questions(index)
          div(
            cls := "contents",
            progress(s"Question ${index + 1} of ${state.questions.size}"),
            item(
              legend(
                dataAttr("slot") := "questionnaire-title",
                cls := "cn-questionnaire-title cn-font-heading float-left w-full p-0 text-base leading-snug font-medium text-pretty [&:not(:has(~[data-slot=questionnaire-description]))]:mb-4",
                question.title
              ),
              question.description.map(d => description(d)),
              question.kind match
                case Kind.Text =>
                  input(
                    placeholder := question.placeholder,
                    aria.label := question.title,
                    controlled(
                      value <-- state.answer(question.id).map(_.headOption.getOrElse("")),
                      onInput.mapToValue --> { v => state.set(question.id, Seq(v)) }
                    ),
                    onKeyDown.filter(_.key == "Enter") --> { ev =>
                      ev.preventDefault()
                      if index == state.questions.size - 1 then state.submit().foreach(onSubmit) else state.next()
                    }
                  )
                case _ =>
                  choices(question.choices.map(c => choice(state, question, c))*)
              ,
              child.maybe <-- state.error.signal.map(_.map(message => error(message)))
            ),
            actions(
              child.maybe <-- state.isFirst
                .map(first => Option.when(!first)(previous()(onClick --> { _ => state.previous() }))),
              if question.skippable && index < state.questions.size - 1 then skip()(onClick --> { _ => state.skip() })
              else emptyNode,
              if index == state.questions.size - 1 then submit()(onClick --> { _ => state.submit().foreach(onSubmit) })
              else next()(onClick --> { _ => state.next() })
            )
          )
      },
      mods
    )

  def root(mods: Modifier[HtmlElement]*): HtmlElement =
    div(dataAttr("slot") := "questionnaire", cls := "cn-questionnaire flex w-full min-w-0 flex-col gap-4", mods)

  def progress(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "questionnaire-progress",
      aria.live := "polite",
      cls := "cn-questionnaire-progress min-h-[1lh] w-fit min-w-[14ch] text-xs font-medium text-muted-foreground tabular-nums",
      mods
    )

  def item(mods: Modifier[HtmlElement]*): HtmlElement =
    fieldSet(
      dataAttr("slot") := "questionnaire-item",
      cls := "cn-questionnaire-item m-0 flex min-w-0 flex-col gap-4 border-0 p-0 outline-none",
      mods
    )

  def title(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "questionnaire-title",
      cls := "cn-questionnaire-title cn-font-heading text-base leading-snug font-medium text-pretty [&:not(:has(~[data-slot=questionnaire-description]))]:mb-4",
      mods
    )

  def description(mods: Modifier[HtmlElement]*): HtmlElement =
    p(
      dataAttr("slot") := "questionnaire-description",
      cls := "cn-questionnaire-description -mt-2 text-sm text-pretty text-muted-foreground",
      mods
    )

  def choices(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "questionnaire-choices",
      cls := "cn-questionnaire-choices group/questionnaire-choices grid min-w-0 gap-2",
      mods
    )

  /** One radio or checkbox choice, wired to `state`. */
  def choice(state: State, question: Question, option: Choice): HtmlElement =
    val inputType = if question.kind == Kind.Multiple then "checkbox" else "radio"
    val checked = state.answer(question.id).map(_.contains(option.value))
    label(
      dataAttr("slot") := "questionnaire-choice",
      dataAttr("type") := inputType,
      htmlAttr("data-checked", BooleanAsAttrPresenceCodec) <-- checked,
      option.shortcut.map(key => dataAttr("shortcut") := key),
      if option.disabled then dataAttr("disabled") := "" else emptyMod,
      cls := "cn-questionnaire-choice group/questionnaire-choice relative flex min-h-11 cursor-pointer items-start gap-2.5 rounded-lg border border-input bg-transparent px-3 py-2.5 text-start text-sm transition-colors outline-none select-none hover:bg-muted/50 has-[>input:focus-visible]:border-ring has-[>input:focus-visible]:ring-3 has-[>input:focus-visible]:ring-ring/50 data-invalid:border-destructive dark:bg-input/20 data-checked:border-primary/40 data-checked:bg-muted dark:data-checked:bg-muted data-disabled:pointer-events-none data-disabled:cursor-not-allowed data-disabled:opacity-50",
      L.input(
        typ := inputType,
        nameAttr := s"questionnaire-${question.id}",
        value := option.value,
        disabled := option.disabled,
        dataAttr("slot") := "questionnaire-choice-input",
        cls := "absolute inset-0 z-10 m-0 size-full cursor-pointer opacity-0",
        L.checked <-- checked,
        onClick --> { ev =>
          // A radio cannot be unchecked by clicking; keep the DOM in step with the state instead of the browser.
          ev.preventDefault()
          state.toggle(question, option.value)
        }
      ),
      span(
        aria.hidden := true,
        dataAttr("slot") := "questionnaire-choice-indicator",
        cls := "pointer-events-none relative flex size-4 shrink-0 translate-y-[--spacing(0.45)] items-center justify-center rounded-[4px] border border-input group-has-data-[slot=questionnaire-choice-description]/questionnaire-choice:translate-y-0.5 group-data-[type=radio]/questionnaire-choice:rounded-full group-data-checked/questionnaire-choice:border-primary group-data-checked/questionnaire-choice:bg-primary group-data-checked/questionnaire-choice:text-primary-foreground dark:bg-input/30 dark:group-data-checked/questionnaire-choice:bg-primary",
        span(
          dataAttr("slot") := "questionnaire-choice-indicator-dot",
          cls := "hidden size-2 rounded-full bg-primary-foreground group-data-[type=checkbox]/questionnaire-choice:hidden group-data-checked/questionnaire-choice:block"
        ),
        Icons.check(
          svg.cls := "hidden size-3.5! group-data-[type=radio]/questionnaire-choice:hidden group-data-checked/questionnaire-choice:block"
        )
      ),
      span(
        dataAttr("slot") := "questionnaire-choice-label",
        cls := "flex min-w-0 flex-1 flex-col gap-0.5 leading-snug",
        span(option.label),
        option.description.map(d => choiceDescription(d))
      ),
      option.shortcut.map { key =>
        kbdTag(
          dataAttr("slot") := "questionnaire-choice-shortcut",
          cls := "pointer-events-none ms-auto hidden size-5 shrink-0 translate-y-[--spacing(0.45)] items-center justify-center rounded-md border border-input bg-background font-mono text-[0.625rem] leading-none font-medium text-muted-foreground group-has-data-[slot=questionnaire-choice-description]/questionnaire-choice:translate-y-0.5 group-data-[shortcut]/questionnaire-choice:inline-flex",
          key.toUpperCase
        )
      }
    )

  def choiceDescription(mods: Modifier[HtmlElement]*): HtmlElement =
    span(dataAttr("slot") := "questionnaire-choice-description", cls := "text-muted-foreground", mods)

  def input(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "questionnaire-input-wrapper",
      cls := "group/questionnaire-input relative w-full min-w-0",
      L.input(
        dataAttr("slot") := "questionnaire-input",
        cls := "cn-questionnaire-input h-8 min-h-11 w-full min-w-0 rounded-lg border border-input bg-transparent px-2.5 py-1 text-base transition-[color,box-shadow,background-color] outline-none selection:bg-primary selection:text-primary-foreground placeholder:text-muted-foreground focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50 disabled:pointer-events-none disabled:cursor-not-allowed disabled:bg-input/50 disabled:opacity-50 aria-invalid:border-destructive aria-invalid:ring-3 aria-invalid:ring-destructive/20 sm:min-h-0 md:text-sm dark:bg-input/30 dark:disabled:bg-input/80 dark:aria-invalid:border-destructive/50 dark:aria-invalid:ring-destructive/40",
        mods
      )
    )

  def error(mods: Modifier[HtmlElement]*): HtmlElement =
    p(
      dataAttr("slot") := "questionnaire-error",
      role := "alert",
      cls := "cn-questionnaire-error -mt-2 text-sm text-destructive",
      mods
    )

  def actions(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "questionnaire-actions",
      cls := "cn-questionnaire-actions grid min-h-11 w-full grid-cols-[minmax(0,1fr)_auto_auto] items-center gap-2 sm:min-h-8",
      mods
    )

  private def actionButton(
      slot: String,
      variant: Button.Variant,
      placement: String,
      mods: Seq[Modifier[HtmlElement]]
  ): HtmlElement =
    Button(
      Button.ButtonApi.variant(variant),
      Button.ButtonApi.size(Button.Size.Default),
      dataAttr("slot") := slot,
      cls := s"row-start-1 min-h-11 sm:min-h-0 $placement",
      mods
    )

  /** The action buttons take their label as a modifier like any button; upstream's default labels are the parameter
    * defaults here: `previous()(onClick --> ...)`.
    */
  def previous(text: String = "Previous")(mods: Modifier[HtmlElement]*): HtmlElement =
    actionButton("questionnaire-previous", Button.Variant.Outline, "col-start-1 justify-self-start", text +: mods)

  def skip(text: String = "Skip")(mods: Modifier[HtmlElement]*): HtmlElement =
    actionButton("questionnaire-skip", Button.Variant.Outline, "col-start-2 justify-self-end", text +: mods)

  def next(text: String = "Next")(mods: Modifier[HtmlElement]*): HtmlElement =
    actionButton("questionnaire-next", Button.Variant.Primary, "col-start-3 justify-self-end", text +: mods)

  def submit(text: String = "Submit")(mods: Modifier[HtmlElement]*): HtmlElement =
    actionButton("questionnaire-submit", Button.Variant.Primary, "col-start-3 justify-self-end", text +: mods)
