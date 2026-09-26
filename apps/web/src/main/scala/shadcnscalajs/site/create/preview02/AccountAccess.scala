package shadcnscalajs.site.create.preview02

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.*

/** Port of shadcn-svelte preview-02 `cards/account-access.svelte`. */
object AccountAccess:
  def apply(): HtmlElement =
    Card(
      dataAttr("card") := "account-access",
      Card.header(
        Card.title("Account Access"),
        Card.description("Update your credentials or re-authenticate.")
      ),
      Card.content(
        Field.group(
          Field(
            Field.label("Email Address", forId := "email-address"),
            Input(idAttr := "email-address", typ := "email", value := "artist@studio.inc")
          ),
          Field(
            div(
              cls := "flex items-center justify-between",
              Field.label("Current Password", forId := "current-password"),
              a(
                href := "#/",
                cls := "text-xs font-medium tracking-wider text-muted-foreground uppercase hover:text-foreground",
                "Forgot?"
              )
            ),
            Input(idAttr := "current-password", typ := "password", value := "password123")
          )
        )
      ),
      Card.footer(
        cls := "flex-col gap-4",
        Button.of(
          _ => cls := "w-full",
          _ => Icons.lockKeyhole(),
          _ => "Update Security"
        ),
        a(
          href := "#/",
          dataAttr("slot") := "item",
          dataAttr("variant") := "muted",
          cls := s"${Item.baseClass} cn-item-variant-muted ${Item.variantClass(Item.Variant.Muted)} cn-item-size-default ${Item.sizeClass(Item.Size.Default)}",
          Item.media(Item.MediaVariant.Icon, Icons.alertCircle(svg.cls := "text-destructive")),
          Item.content(
            Item.title("Danger Zone"),
            Item.description(cls := "line-clamp-1", "Archive account and remove catalog")
          ),
          Icons.arrowRight(svg.cls := "size-4")
        )
      )
    )
