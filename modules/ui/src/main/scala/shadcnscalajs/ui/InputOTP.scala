package shadcnscalajs.ui

import com.raquo.laminar.api.L.*
import org.scalajs.dom

/** shadcn/ui InputOTP — a row of single-character inputs bound to a `Var[String]`, with auto-advance on input and
  * backspace-to-previous. shadcn/ui wraps the `input-otp` npm package for masking/paste-splitting; this hand-rolls the
  * same UX directly in Laminar since there's no Scala.js equivalent to wrap.
  */
object InputOTP:

  def apply(codeVar: Var[String], length: Int = 6): HtmlElement =
    val boxRefs = scala.collection.mutable.ArrayBuffer.fill[Option[dom.html.Input]](length)(None)

    def charAt(s: String, i: Int): String = if i < s.length then s.charAt(i).toString else ""

    def setCharAt(i: Int, c: String): Unit =
      val padded = codeVar.now().padTo(length, ' ')
      val updated = padded.updated(i, if c.isEmpty then ' ' else c.charAt(0))
      codeVar.set(updated.replace(" ", ""))

    div(
      cls := "flex items-center gap-2",
      (0 until length).map { i =>
        input(
          typ := "text",
          inputMode := "numeric",
          maxLength := 1,
          cls := "size-9 rounded-md border text-center text-sm outline-none focus-visible:border-ring focus-visible:ring-[3px] focus-visible:ring-ring/50",
          value <-- codeVar.signal.map(s => charAt(s, i)),
          onMountCallback { ctx => boxRefs(i) = Some(ctx.thisNode.ref) },
          onInput --> { ev =>
            val v = ev.target.asInstanceOf[dom.html.Input].value
            setCharAt(i, v)
            if v.nonEmpty && i < length - 1 then boxRefs(i + 1).foreach(_.focus())
          },
          onKeyDown --> { (ev: dom.KeyboardEvent) =>
            if ev.key == "Backspace" && charAt(codeVar.now(), i).isEmpty && i > 0 then boxRefs(i - 1).foreach(_.focus())
          }
        )
      }
    )
