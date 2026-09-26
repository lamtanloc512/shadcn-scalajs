package shadcnscalajs.ui

import com.raquo.laminar.api.L.*
import org.scalajs.dom

object Checkbox:
  private val baseClasses =
    "input cn-checkbox peer size-4 shrink-0 rounded-[4px] border border-primary shadow-xs outline-none transition-shadow focus-visible:border-ring focus-visible:ring-[3px] focus-visible:ring-ring/50 disabled:cursor-not-allowed disabled:opacity-50 checked:bg-primary checked:text-primary-foreground"

  /** Style packs select the checked fill via `[data-checked]` presence, which a native input never emits on its own.
    * The attribute must be absent (not `"false"`) when unchecked.
    */
  private def reflectChecked(isChecked: Signal[Boolean]): Modifier[HtmlElement] =
    onMountBind { ctx =>
      isChecked --> { checked =>
        if checked then ctx.thisNode.ref.setAttribute("data-checked", "")
        else ctx.thisNode.ref.removeAttribute("data-checked")
      }
    }

  def apply(mods: Modifier[HtmlElement]*): HtmlElement =
    val selfManaged = Var(false)
    input(
      typ := "checkbox",
      dataAttr("slot") := "checkbox",
      cls := baseClasses,
      mods,
      onMountCallback(ctx => selfManaged.set(ctx.thisNode.ref.checked)),
      onClick.mapToChecked --> selfManaged,
      reflectChecked(selfManaged.signal)
    )

  def apply(checkedVar: Var[Boolean], mods: Modifier[HtmlElement]*): HtmlElement =
    input(
      typ := "checkbox",
      dataAttr("slot") := "checkbox",
      cls := baseClasses,
      controlled(checked <-- checkedVar.signal, onClick.mapToChecked --> checkedVar),
      reflectChecked(checkedVar.signal),
      mods
    )

  def apply(checkedVar: Var[Boolean], indeterminate: Signal[Boolean], mods: Modifier[HtmlElement]*): HtmlElement =
    input(
      typ := "checkbox",
      dataAttr("slot") := "checkbox",
      cls := baseClasses,
      controlled(checked <-- checkedVar.signal, onClick.mapToChecked --> checkedVar),
      reflectChecked(checkedVar.signal),
      onMountBind { ctx =>
        indeterminate --> { indet =>
          ctx.thisNode.ref.asInstanceOf[dom.html.Input].indeterminate = indet
        }
      },
      mods
    )
