package shadcnscalajs.ui

import com.raquo.laminar.api.L.*
import org.scalajs.dom

object Tabs:

  enum ListVariant derives CanEqual:
    case Default, Line

  final case class Tab(
      value: String,
      label: String,
      panel: HtmlElement,
      triggerMods: Seq[Modifier[HtmlElement]] = Nil
  )

  private val rootClasses =
    "cn-tabs group/tabs flex data-[orientation=horizontal]:flex-col gap-2"

  private val listBaseClasses =
    "cn-tabs-list group/tabs-list inline-flex w-fit items-center justify-center text-muted-foreground group-data-[orientation=vertical]/tabs:h-fit group-data-[orientation=vertical]/tabs:flex-col"

  private val listVariantClasses: Map[ListVariant, String] = Map(
    ListVariant.Default -> "cn-tabs-list-variant-default bg-muted",
    ListVariant.Line -> "cn-tabs-list-variant-line gap-1 bg-transparent"
  )

  private val triggerClasses =
    "cn-tabs-trigger relative inline-flex h-[calc(100%-1px)] flex-1 items-center justify-center whitespace-nowrap text-foreground/60 transition-all group-data-[orientation=vertical]/tabs:w-full group-data-[orientation=vertical]/tabs:justify-start hover:text-foreground focus-visible:border-ring focus-visible:ring-[3px] focus-visible:ring-ring/50 focus-visible:outline-1 focus-visible:outline-ring disabled:pointer-events-none disabled:opacity-50 dark:text-muted-foreground dark:hover:text-foreground [&_svg]:pointer-events-none [&_svg]:shrink-0 group-data-[variant=line]/tabs-list:bg-transparent group-data-[variant=line]/tabs-list:data-active:bg-transparent dark:group-data-[variant=line]/tabs-list:data-active:border-transparent dark:group-data-[variant=line]/tabs-list:data-active:bg-transparent data-active:bg-background data-active:text-foreground dark:data-active:border-input dark:data-active:bg-input/30 dark:data-active:text-foreground after:absolute after:bg-foreground after:opacity-0 after:transition-opacity group-data-[orientation=horizontal]/tabs:after:inset-x-0 group-data-[orientation=horizontal]/tabs:after:bottom-[-5px] group-data-[orientation=horizontal]/tabs:after:h-0.5 group-data-[orientation=vertical]/tabs:after:inset-y-0 group-data-[orientation=vertical]/tabs:after:-right-1 group-data-[orientation=vertical]/tabs:after:w-0.5 group-data-[variant=line]/tabs-list:data-active:after:opacity-100"

  private val contentClasses = "cn-tabs-content flex-1 outline-none"

  private def listVariantData(variant: ListVariant): String = variant match
    case ListVariant.Default => "default"
    case ListVariant.Line    => "line"

  def apply(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "tabs",
      dataAttr("orientation") := "horizontal",
      cls := rootClasses,
      mods
    )

  def list(variant: ListVariant = ListVariant.Default, mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      role := "tablist",
      dataAttr("slot") := "tabs-list",
      dataAttr("variant") := listVariantData(variant),
      cls := s"$listBaseClasses ${listVariantClasses(variant)}",
      mods
    )

  def trigger(mods: Modifier[HtmlElement]*): HtmlElement = button(
    typ := "button",
    role := "tab",
    dataAttr("slot") := "tabs-trigger",
    cls := triggerClasses,
    mods
  )

  def content(mods: Modifier[HtmlElement]*): HtmlElement =
    div(role := "tabpanel", dataAttr("slot") := "tabs-content", cls := contentClasses, mods)

  def stateful(selected: Var[String])(tabs: (String, String, HtmlElement)*): HtmlElement =
    stateful(selected, ListVariant.Default)(tabs.map { case (value, label, panel) => Tab(value, label, panel) }*)

  def stateful(
      selected: Var[String],
      listVariant: ListVariant = ListVariant.Default,
      listMods: Modifier[HtmlElement]*
  )(tabs: Tab*): HtmlElement =
    val tabValues = tabs.map(_.value).toList

    def focusTab(tablistEl: dom.Element, value: String): Unit =
      tablistEl
        .querySelector(s"""button[role="tab"][data-value="$value"]""")
        .asInstanceOf[dom.html.Button]
        .focus()

    div(
      dataAttr("slot") := "tabs",
      dataAttr("orientation") := "horizontal",
      cls := rootClasses,
      div(
        role := "tablist",
        dataAttr("slot") := "tabs-list",
        dataAttr("variant") := listVariantData(listVariant),
        cls := s"$listBaseClasses ${listVariantClasses(listVariant)}",
        listMods,
        onKeyDown --> { (ev: dom.KeyboardEvent) =>
          val currentIdx = tabValues.indexOf(selected.now())
          if currentIdx >= 0 then
            ev.key match
              case "ArrowRight" =>
                ev.preventDefault()
                val next = tabValues((currentIdx + 1) % tabValues.length)
                selected.set(next)
                focusTab(ev.currentTarget.asInstanceOf[dom.Element], next)
              case "ArrowLeft" =>
                ev.preventDefault()
                val next = tabValues((currentIdx - 1 + tabValues.length) % tabValues.length)
                selected.set(next)
                focusTab(ev.currentTarget.asInstanceOf[dom.Element], next)
              case "Home" =>
                ev.preventDefault()
                val next = tabValues.head
                selected.set(next)
                focusTab(ev.currentTarget.asInstanceOf[dom.Element], next)
              case "End" =>
                ev.preventDefault()
                val next = tabValues.last
                selected.set(next)
                focusTab(ev.currentTarget.asInstanceOf[dom.Element], next)
              case _ => ()
        },
        tabs.toList.map { tab =>
          button(
            typ := "button",
            role := "tab",
            dataAttr("slot") := "tabs-trigger",
            dataAttr("value") := tab.value,
            cls := triggerClasses,
            tabIndex <-- selected.signal.map(v => if v == tab.value then 0 else -1),
            inContext { thisNode =>
              selected.signal --> { v =>
                if v == tab.value then thisNode.ref.setAttribute("data-active", "true")
                else thisNode.ref.removeAttribute("data-active")
              }
            },
            aria.selected <-- selected.signal.map(_ == tab.value),
            onClick --> { _ => selected.set(tab.value) },
            tab.triggerMods,
            tab.label
          )
        }
      ),
      tabs.toList.map { tab =>
        div(
          role := "tabpanel",
          dataAttr("slot") := "tabs-content",
          cls := contentClasses,
          display <-- selected.signal.map(v => if v == tab.value then "block" else "none"),
          tab.panel
        )
      }
    )
