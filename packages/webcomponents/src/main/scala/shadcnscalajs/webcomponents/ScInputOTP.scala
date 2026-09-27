package shadcnscalajs.webcomponents

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.InputOTP

import scala.scalajs.js

/** A single-input OTP field with a configurable number of visible slots. */
class ScInputOTP extends ScElementBase:

  private val codeVar = Var("")
  private val lengthVar = Var(6)
  private val patternVar = Var("numeric")
  private var lastValue = ""

  private def normalizeCode(raw: String): String =
    val allowed = if patternVar.now() == "alphanumeric" then InputOTP.alphanumeric else InputOTP.digits
    raw.filter(allowed).take(lengthVar.now())

  private def updateCode(raw: String): Unit =
    val cleaned = normalizeCode(raw)
    lastValue = cleaned
    if codeVar.now() != cleaned then codeVar.set(cleaned)
    if this.hasAttribute("value") && this.getAttribute("value") != cleaned then this.setAttribute("value", cleaned)

  observeAttribute("length") { v =>
    val parsed = v.flatMap(_.toIntOption).getOrElse(6).max(1).min(128)
    if lengthVar.now() != parsed then lengthVar.set(parsed)
    updateCode(codeVar.now())
  }
  observeAttribute("pattern") { v =>
    val parsed = if v.contains("alphanumeric") then "alphanumeric" else "numeric"
    if patternVar.now() != parsed then patternVar.set(parsed)
    updateCode(codeVar.now())
  }
  observeAttribute("value")(v => updateCode(v.getOrElse("")))
  stringProperty("length")
  stringProperty("pattern")
  js.Dynamic.global.Object.defineProperty(
    this,
    "value",
    js.Dynamic.literal(
      configurable = true,
      get = (() => codeVar.now()): js.Function0[String],
      set = (
          (v: js.Any) => if v == null then removeAttribute("value") else setAttribute("value", v.toString)
      ): js.Function1[js.Any, Unit]
    )
  )

  mount(
    ScInputOTP.view(
      codeVar,
      lengthVar,
      patternVar,
      code => {
        if code != lastValue then
          lastValue = code
          setAttribute("value", code)
          emit("sc-change", code)
      }
    )
  )

object ScInputOTP:
  def register(): Unit =
    ScElements.define("sc-input-otp", js.constructorOf[ScInputOTP], "value", "length", "pattern")

  private def view(
      codeVar: Var[String],
      lengthVar: Var[Int],
      patternVar: Var[String],
      onChange: String => Unit
  ): HtmlElement =
    div(
      children <-- lengthVar.signal.combineWith(patternVar.signal).map { (length, pattern) =>
        val allowed = if pattern == "alphanumeric" then InputOTP.alphanumeric else InputOTP.digits
        val otp = InputOTP.ctx(codeVar, length, allowed)
        List(
          InputOTP
            .root(otp)(
              InputOTP.group((0 until length).map(otp.slot(_))*)
            )
            .amend(
              onInput --> Observer { _ =>
                onChange(codeVar.now())
              }
            )
        )
      }
    )
