package shadcnscalajs.ui

import com.raquo.laminar.api.L
import com.raquo.laminar.api.L.*
import org.scalajs.dom

/** shadcn/ui InputOTP — upstream wraps bits-ui's `PinInput`, itself a port of the `input-otp` package; this reproduces
  * its design directly in Laminar since there is no Scala.js equivalent to wrap.
  *
  * The design worth copying is that there is exactly one real `<input>`, stretched invisibly across the whole widget,
  * and the slots are plain divs that render what it holds. Everything a text field already does correctly — paste and
  * paste-splitting, select-all, arrow keys, undo, mobile keyboards, `one-time-code` autofill — then works without being
  * reimplemented, and the price is only that the caret has to be drawn, since the real one is hidden.
  *
  * A row of one-character inputs, which is the obvious alternative, gets all of that wrong.
  */
object InputOTP:

  /** Digits only, matching the `numeric` input mode. */
  val digits: Char => Boolean = _.isDigit

  val alphanumeric: Char => Boolean = c => c.isLetterOrDigit

  private val slotClasses: String =
    "relative flex size-9 items-center justify-center border-y border-r border-input text-center text-sm shadow-xs transition-all outline-none first:rounded-l-md first:border-l last:rounded-r-md dark:bg-input/30 data-[active=true]:z-10 data-[active=true]:border-ring data-[active=true]:ring-[3px] data-[active=true]:ring-ring/50"

  /** What one slot renders: its character, whether the (hidden) selection covers it, and whether the drawn caret sits
    * in it.
    */
  private final case class Cell(char: Option[Char], active: Boolean, caret: Boolean)

  /** One OTP field: the value plus the hidden input's selection, which the slots read to place the caret. */
  final class Ctx private[ui] (val length: Int, val value: Var[String], private val allowed: Char => Boolean):
    private val selectionStart = Var(0)
    private val selectionEnd = Var(0)
    private val focused = Var(false)

    private def cellOf(index: Int): Signal[Cell] =
      value.signal
        .combineWith(selectionStart.signal, selectionEnd.signal, focused.signal)
        .map { (code, start, end, hasFocus) =>
          // A collapsed caret sitting past the last slot belongs to that slot, or a full value would light up nothing.
          val (from, to) =
            if start == end && start == length && code.length == length then (length - 1, length) else (start, end)
          val char = Option.when(index < code.length)(code.charAt(index))
          val active = hasFocus && (if from == to then index == from else index >= from && index < to)
          Cell(char, active, active && char.isEmpty)
        }

    def slot(index: Int, mods: Modifier[HtmlElement]*): HtmlElement =
      val cell = cellOf(index)
      val caret =
        div(
          cls := "cn-input-otp-caret pointer-events-none absolute inset-0 flex items-center justify-center",
          div(cls := "cn-input-otp-caret-line h-4 w-px animate-caret-blink bg-foreground duration-1000")
        )

      div(
        dataAttr("slot") := "input-otp-slot",
        cls := s"cn-input-otp-slot $slotClasses",
        dataAttr("active") <-- cell.map(_.active.toString),
        child.text <-- cell.map(_.char.fold("")(_.toString)),
        child.maybe <-- cell.map(c => Option.when(c.caret)(caret)),
        mods
      )

    /** The field itself, stretched over the slots so a click anywhere lands in it. Transparent rather than `visibility:
      * hidden` or off-screen, both of which would cost the on-screen keyboard and autofill.
      */
    private[ui] def field(mods: Modifier[HtmlElement]*): HtmlElement =
      def mirror(el: dom.html.Input): Unit =
        selectionStart.set(el.selectionStart)
        selectionEnd.set(el.selectionEnd)

      input(
        typ := "text",
        inputMode := "numeric",
        autoComplete := "one-time-code",
        spellCheck := false,
        maxLength := length,
        dataAttr("slot") := "input-otp-input",
        cls := "absolute inset-0 z-10 size-full opacity-0 outline-none",
        // `value` here is the field's own state, which shadows Laminar's prop of the same name.
        L.value <-- value.signal,
        onInput --> { ev =>
          val el = ev.target.asInstanceOf[dom.html.Input]
          val cleaned = el.value.filter(allowed).take(length)
          // Rejecting a character has to be written back, or the input keeps showing what the value no longer holds.
          if cleaned != el.value then el.value = cleaned
          value.set(cleaned)
          mirror(el)
        },
        // The selection moves for reasons beyond typing — arrows, clicks, select-all — and each needs mirroring.
        onKeyUp --> { ev => mirror(ev.target.asInstanceOf[dom.html.Input]) },
        onClick --> { ev => mirror(ev.target.asInstanceOf[dom.html.Input]) },
        onSelect --> { ev => mirror(ev.target.asInstanceOf[dom.html.Input]) },
        onFocus --> { ev =>
          val el = ev.target.asInstanceOf[dom.html.Input]
          focused.set(true)
          // Land on the first empty slot rather than wherever the click happened: the slots are not laid out where the
          // input's own text is, so the browser's hit-testing means nothing here.
          val caretAt = el.value.length
          el.setSelectionRange(caretAt, caretAt)
          mirror(el)
        },
        onBlur --> { _ => focused.set(false) },
        mods
      )

  def group(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "input-otp-group",
      cls := "cn-input-otp-group flex items-center",
      mods
    )

  /** Upstream defaults the separator's content to a minus icon. */
  def separator(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "input-otp-separator",
      role := "separator",
      cls := "cn-input-otp-separator flex items-center [&_svg:not([class*='size-'])]:size-4",
      if mods.isEmpty then Icons.minus() else mods
    )

  /** Creates an OTP field's state, which its slots read. */
  def ctx(value: Var[String], length: Int = 6, allowed: Char => Boolean = digits): Ctx =
    Ctx(length, value, allowed)

  /** The widget, upstream's `InputOTP.Root` — `mods` are the groups and slots built from `otp`. */
  def root(otp: Ctx)(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "input-otp",
      cls := "cn-input-otp-input cn-input-otp relative flex items-center gap-2 disabled:cursor-not-allowed has-disabled:opacity-50",
      otp.field(),
      mods
    )

  /** Flat form: one group of `length` slots. */
  def apply(codeVar: Var[String], length: Int = 6): HtmlElement =
    val otp = ctx(codeVar, length)
    root(otp)(group((0 until length).map(otp.slot(_))*))
