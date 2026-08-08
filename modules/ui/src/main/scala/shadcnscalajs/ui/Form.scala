package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

/** shadcn/ui Form — upstream wires these parts through formsnap + sveltekit-superforms. There is no Scala.js equivalent
  * of that stack, so this keeps the same DOM contract (`data-slot=form-*`, `data-fs-error` on labels) and adds a small
  * [[Ctx]] that owns per-field error messages — enough to drive invalid styling and the error list without pulling in a
  * form framework.
  *
  * Prefer [[Field]] when you only need layout (orientation, separators). Prefer [[Form]] when you need named fields
  * that share a submit-time error map.
  */
object Form:

  private val fieldSetTag = htmlTag("fieldset")
  private val legendTag = htmlTag("legend")

  /** One form's validation state. Keys are field names; values are the messages shown under that field. */
  final class Ctx:
    val errors: Var[Map[String, Seq[String]]] = Var(Map.empty)

    def messagesOf(name: String): Signal[Seq[String]] =
      errors.signal.map(_.getOrElse(name, Nil))

    def invalid(name: String): Signal[Boolean] =
      messagesOf(name).map(_.nonEmpty)

    def setErrors(name: String, messages: Seq[String]): Unit =
      errors.update { map =>
        val cleaned = messages.map(_.trim).filter(_.nonEmpty)
        if cleaned.isEmpty then map - name else map.updated(name, cleaned)
      }

    def clear(name: String): Unit = setErrors(name, Nil)

    def clearAll(): Unit = errors.set(Map.empty)

    /** Replace the whole error map — typical submit handler shape: validate, then `setAll(results)`. */
    def setAll(next: Map[String, Seq[String]]): Unit =
      errors.set(next.collect { case (k, v) if v.exists(_.trim.nonEmpty) => k -> v.map(_.trim).filter(_.nonEmpty) })

    def field(name: String, mods: Modifier[HtmlElement]*): HtmlElement =
      Form.item(
        dataAttr("invalid") <-- invalid(name).map(_.toString),
        mods
      )

    def label(name: String, text: String, mods: Modifier[HtmlElement]*): HtmlElement =
      Form.label(
        text,
        // Upstream formsnap sets `data-fs-error` on the label when the field has errors.
        onMountBind { ctx =>
          invalid(name) --> { bad =>
            val el = ctx.thisNode.ref
            if bad then el.setAttribute("data-fs-error", "") else el.removeAttribute("data-fs-error")
          }
        },
        mods
      )

    def fieldErrors(name: String, mods: Modifier[HtmlElement]*): HtmlElement =
      div(
        dataAttr("slot") := "form-message",
        cls := "text-sm font-medium text-destructive",
        // Match Field.error: render nothing useful when empty, otherwise a list.
        children <-- messagesOf(name).map { msgs =>
          if msgs.isEmpty then Seq.empty[HtmlElement]
          else if msgs.length == 1 then Seq(div(role := "alert", msgs.head))
          else
            Seq(
              ul(
                role := "alert",
                cls := "ml-4 flex list-disc flex-col gap-1",
                msgs.map(msg => li(msg))
              )
            )
        },
        mods
      )

  def ctx(): Ctx = Ctx()

  def apply(mods: Modifier[HtmlElement]*): HtmlElement =
    form(
      dataAttr("slot") := "form",
      // Custom [[Ctx]] validation owns error display; native constraint validation would block submit before our
      // handler runs (e.g. `type="email"` rejecting `nope`).
      noValidate := true,
      cls := "form grid gap-6",
      mods
    )

  def item(mods: Modifier[HtmlElement]*): HtmlElement =
    div(dataAttr("slot") := "form-item", cls := "space-y-2", mods)

  def label(text: String, mods: Modifier[HtmlElement]*): HtmlElement =
    Label(
      dataAttr("slot") := "form-label",
      cls := "data-[fs-error]:text-destructive",
      mods,
      text
    )

  def description(mods: Modifier[HtmlElement]*): HtmlElement =
    p(
      dataAttr("slot") := "form-description",
      cls := "text-sm text-muted-foreground",
      mods
    )

  def message(mods: Modifier[HtmlElement]*): HtmlElement =
    fieldErrors(mods*)

  def fieldset(mods: Modifier[HtmlElement]*): HtmlElement =
    fieldSetTag(dataAttr("slot") := "form-fieldset", cls := "space-y-2", mods)

  def legend(mods: Modifier[HtmlElement]*): HtmlElement =
    legendTag(
      dataAttr("slot") := "form-legend",
      cls := "text-sm leading-none font-medium data-[fs-error]:text-destructive",
      mods
    )

  def fieldErrors(mods: Modifier[HtmlElement]*): HtmlElement =
    div(dataAttr("slot") := "form-message", cls := "text-sm font-medium text-destructive", mods)

  /** Submit button — upstream's `Form.Button` is just `Button` with `type="submit"`. */
  def button(mods: Modifier[HtmlElement]*): HtmlElement =
    Button(typ := "submit", mods)
