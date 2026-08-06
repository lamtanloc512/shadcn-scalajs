package shadcnscalajs.blocks.login01

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.*

/** Port of shadcn/ui new-york-v4 `blocks/login-01/components/login-form.tsx`. */
object LoginForm:

  def apply(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      cls := "flex flex-col gap-6",
      mods,
      Card(
        Card.header(
          Card.title("Login to your account"),
          Card.description("Enter your email below to login to your account")
        ),
        Card.content(
          form(
            Field.group(
              Field(
                Field.label("Email", forId := "email"),
                Input(idAttr := "email", typ := "email", placeholder := "m@example.com", required := true)
              ),
              Field(
                div(
                  cls := "flex items-center",
                  Field.label("Password", forId := "password"),
                  a(
                    href := "#",
                    cls := "ml-auto inline-block text-sm underline-offset-4 hover:underline",
                    "Forgot your password?"
                  )
                ),
                Input(idAttr := "password", typ := "password", required := true)
              ),
              Field(
                Button.of(
                  _.variant(Button.Variant.Primary),
                  _.size(Button.Size.Default),
                  _ => typ := "submit",
                  _ => "Login"
                ),
                Button.of(
                  _.variant(Button.Variant.Outline),
                  _.size(Button.Size.Default),
                  _ => typ := "button",
                  _ => "Login with Google"
                ),
                Field.description(cls := "text-center", "Don't have an account? ", a(href := "#", "Sign up"))
              )
            )
          )
        )
      )
    )
