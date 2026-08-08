package shadcnscalajs.ui

import com.raquo.laminar.api.L.*
import org.scalajs.dom

/** shadcn/ui InputOTP — a row of single-character inputs bound to a `Var[String]`, with auto-advance on input and
  * backspace-to-previous. shadcn/ui wraps the `input-otp` npm package for masking/paste-splitting; this hand-rolls the
  * same UX directly in Laminar since there's no Scala.js equivalent to wrap.
  */
object InputOTP:

  private val slotStandaloneClasses =
    "relative flex size-9 items-center justify-center border-y border-r border-input text-center text-sm shadow-xs outline-none transition-all first:rounded-l-md first:border-l last:rounded-r-md dark:bg-input/30 focus-visible:z-10 focus-visible:border-ring focus-visible:ring-[3px] focus-visible:ring-ring/50 data-[active=true]:z-10 data-[active=true]:border-ring data-[active=true]:ring-[3px] data-[active=true]:ring-ring/50"

  def apply(codeVar: Var[String], length: Int = 6): HtmlElement =
    val boxRefs = scala.collection.mutable.ArrayBuffer.fill[Option[dom.html.Input]](length)(None)
    root(group((0 until length).map(i => slotAt(codeVar, length, i, boxRefs))*))

  def root(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "input-otp",
      cls := "cn-input-otp-input cn-input-otp flex items-center gap-2 disabled:cursor-not-allowed has-disabled:opacity-50",
      mods
    )

  def group(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "input-otp-group",
      cls := "cn-input-otp-group flex items-center",
      mods
    )

  def separator(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "input-otp-separator",
      role := "separator",
      cls := "cn-input-otp-separator flex items-center",
      mods
    )

  def slot(
      codeVar: Var[String],
      length: Int,
      index: Int,
      mods: Modifier[HtmlElement]*
  ): HtmlElement =
    val boxRefs = scala.collection.mutable.ArrayBuffer.fill[Option[dom.html.Input]](length)(None)
    slotAt(codeVar, length, index, boxRefs, mods*)

  private def slotAt(
      codeVar: Var[String],
      length: Int,
      index: Int,
      boxRefs: scala.collection.mutable.ArrayBuffer[Option[dom.html.Input]],
      mods: Modifier[HtmlElement]*
  ): HtmlElement =
    def charAt(s: String, i: Int): String = if i < s.length then s.charAt(i).toString else ""

    def setCharAt(i: Int, c: String): Unit =
      val padded = codeVar.now().padTo(length, ' ')
      val updated = padded.updated(i, if c.isEmpty then ' ' else c.charAt(0))
      codeVar.set(updated.replace(" ", ""))

    input(
      typ := "text",
      inputMode := "numeric",
      maxLength := 1,
      dataAttr("slot") := "input-otp-slot",
      cls := s"cn-input-otp-slot $slotStandaloneClasses",
      value <-- codeVar.signal.map(s => charAt(s, index)),
      onMountCallback { ctx => boxRefs(index) = Some(ctx.thisNode.ref) },
      onInput --> { ev =>
        val v = ev.target.asInstanceOf[dom.html.Input].value
        setCharAt(index, v)
        if v.nonEmpty && index < length - 1 then boxRefs(index + 1).foreach(_.focus())
      },
      onKeyDown --> { (ev: dom.KeyboardEvent) =>
        if ev.key == "Backspace" && charAt(codeVar.now(), index).isEmpty && index > 0 then
          boxRefs(index - 1).foreach(_.focus())
      },
      mods
    )
