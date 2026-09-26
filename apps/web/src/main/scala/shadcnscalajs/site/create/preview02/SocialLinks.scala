package shadcnscalajs.site.create.preview02

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.*

/** preview-02 mosaic card — ported from
  * /Users/elam/Personal/shadcn-svelte/docs/src/lib/registry/examples/create/preview-02/cards/social-links.svelte
  */
object SocialLinks:

  def apply(): HtmlElement =
    Card(
      dataAttr("card") := "social-links",
      Card.header(Card.title("Social Links")),
      Card.content(
        Field.group(
          Field(
            Field.label("Spotify Artist URL", forId := "spotify-url"),
            InputGroup(
              InputGroup.addon(InputGroup.AddonAlign.InlineStart, Icons.circlePlus()),
              InputGroup.input(idAttr := "spotify-url", value := "spotify.com/artist/3j...2k")
            )
          ),
          Field(
            Field.label("Instagram Handle", forId := "instagram-handle"),
            InputGroup(
              InputGroup.addon(InputGroup.AddonAlign.InlineStart, Icons.camera()),
              InputGroup.input(idAttr := "instagram-handle", value := "@julianduryea_music")
            )
          ),
          Field(
            Field.label("SoundCloud URL", forId := "soundcloud-url"),
            InputGroup(
              InputGroup.addon(InputGroup.AddonAlign.InlineStart, Icons.cloud()),
              InputGroup.input(idAttr := "soundcloud-url", placeholder := "soundcloud.com/username")
            )
          ),
          Field(
            Field.label("Website", forId := "website-url"),
            InputGroup(
              InputGroup.addon(InputGroup.AddonAlign.InlineStart, Icons.globe()),
              InputGroup.input(idAttr := "website-url", placeholder := "https://yoursite.com")
            )
          )
        )
      ),
      Card.footer(
        cls := "justify-end gap-2",
        Button.of(_.variant(Button.Variant.Secondary), _ => "Discard"),
        Button.of(_ => "Save Changes")
      )
    )
