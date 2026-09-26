package shadcnscalajs.blocks.signup01

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.*

/** Port of shadcn/ui new-york-v4 `blocks/signup-01/components/signup-form.tsx`. */
object SignupForm:

  def apply(mods: Modifier[HtmlElement]*): HtmlElement =
    Card(
      mods,
      Card.header(
        Card.title("Create an account"),
        Card.description("Enter your information below to create your account")
      ),
      Card.content(
        form(
          Field.group(
            Field(
              Field.label("Full Name", forId := "name"),
              Input(idAttr := "name", typ := "text", placeholder := "John Doe", required := true)
            ),
            Field(
              Field.label("Email", forId := "email"),
              Input(idAttr := "email", typ := "email", placeholder := "m@example.com", required := true),
              Field.description("We'll use this to contact you. We will not share your email with anyone else.")
            ),
            Field(
              Field.label("Password", forId := "password"),
              Input(idAttr := "password", typ := "password", required := true),
              Field.description("Must be at least 8 characters long.")
            ),
            Field(
              Field.label("Confirm Password", forId := "confirm-password"),
              Input(idAttr := "confirm-password", typ := "password", required := true),
              Field.description("Please confirm your password.")
            ),
            Field.group(
              Field(
                Button.of(
                  _.variant(Button.Variant.Primary),
                  _.size(Button.Size.Default),
                  _ => typ := "submit",
                  _ => "Create Account"
                ),
                Button.of(
                  _.variant(Button.Variant.Outline),
                  _.size(Button.Size.Default),
                  _ => typ := "button",
                  _ => "Sign up with Google"
                ),
                Field.description(
                  cls := "px-6 text-center",
                  "Already have an account? ",
                  a(href := "#", "Sign in")
                )
              )
            )
          )
        )
      )
    )
