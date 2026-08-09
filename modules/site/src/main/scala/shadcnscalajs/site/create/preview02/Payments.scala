package shadcnscalajs.site.create.preview02

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.*

/** preview-02 mosaic card — port of
  * `/Users/elam/Personal/shadcn-svelte/docs/src/lib/registry/examples/create/preview-02/cards/payments.svelte`
  */
object Payments:

  private def paymentItem(title: String, description: String, icon: => SvgElement): HtmlElement =
    a(
      href := "#/",
      dataAttr("slot") := "item",
      dataAttr("variant") := "muted",
      dataAttr("size") := "default",
      cls := s"${Item.baseClass} ${Item.variantClass(Item.Variant.Muted)} ${Item.sizeClass(Item.Size.Default)} [&>svg:last-child]:size-4 [&>svg:last-child]:shrink-0 [&>svg:last-child]:text-muted-foreground",
      Item.media(Item.MediaVariant.Icon, icon),
      Item.content(
        Item.title(title),
        Item.description(description)
      ),
      Icons.chevronRight()
    )

  def apply(): HtmlElement =
    Card(
      dataAttr("card") := "payments",
      Card.header(
        cls := "flex flex-col gap-3",
        Breadcrumb(
          Breadcrumb.list(
            Breadcrumb.item(Breadcrumb.link("#/", "Home")),
            Breadcrumb.separator(),
            Breadcrumb.item(
              DropdownMenu.withTrigger(DropdownMenu.ghostIconTrigger)(
                Icons.moreHorizontal(),
                span(cls := "sr-only", "Account options")
              )(
                DropdownMenu.Item("Profile", () => ()),
                DropdownMenu.Item("Statements", () => ()),
                DropdownMenu.Item("Documents", () => ())
              )
            ),
            Breadcrumb.separator(),
            Breadcrumb.item(span(cls := "cn-breadcrumb-page font-normal text-foreground", "Payments"))
          )
        )
      ),
      Card.content(
        Item.group(
          paymentItem(
            "Change transfer limit",
            "Adjust how much you can send from your balance.",
            Icons.gauge()
          ),
          paymentItem(
            "Scheduled transfers",
            "Set up a transfer to send at a later date.",
            Icons.calendar()
          ),
          paymentItem(
            "Direct Debits",
            "Set up and manage regular payments.",
            Icons.repeat()
          ),
          paymentItem(
            "Recurring card payments",
            "Manage your repeated card transactions.",
            Icons.refreshCw()
          )
        )
      )
    )
