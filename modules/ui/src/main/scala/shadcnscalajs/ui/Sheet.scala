package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

/** shadcn/ui Sheet — a side panel, built on the same native `<dialog>` mechanism as Dialog/Drawer (`Dialog.element`),
  * positioned to a side via Tailwind classes instead of Radix's animated `data-state` slide transitions.
  */
object Sheet:

  enum Side derives CanEqual:
    case Top, Right, Bottom, Left

  private val sideClasses: Map[Side, String] = Map(
    Side.Right -> "inset-y-0 right-0 left-auto m-0 h-full w-3/4 max-w-sm border-l",
    Side.Left -> "inset-y-0 left-0 right-auto m-0 h-full w-3/4 max-w-sm border-r",
    Side.Top -> "inset-x-0 top-0 bottom-auto m-0 w-full max-h-[80vh] border-b",
    Side.Bottom -> "inset-x-0 bottom-0 top-auto m-0 w-full max-h-[80vh] border-t"
  )

  def apply(isOpenVar: Var[Boolean], side: Side = Side.Right)(mods: Modifier[HtmlElement]*): HtmlElement =
    Dialog.element(
      isOpenVar,
      s"sheet fixed z-50 flex flex-col gap-4 bg-background p-6 shadow-lg ${sideClasses(side)}"
    )(mods*)

  def header(mods: Modifier[HtmlElement]*): HtmlElement = div(cls := "flex flex-col gap-1.5", mods)

  def footer(mods: Modifier[HtmlElement]*): HtmlElement = div(cls := "mt-auto flex flex-col gap-2", mods)
