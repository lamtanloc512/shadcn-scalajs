package shadcnscalajs.blocks.login03

import com.raquo.laminar.api.L.*
import com.raquo.laminar.api.L.svg.{path as svgPath, svg as svgTag}
import shadcnscalajs.ui.*

/** Port of shadcn-svelte `blocks/login-03/components/login-form.svelte`. */
object LoginForm:

  private def appleIcon: SvgElement =
    svgTag(
      svg.viewBox := "0 0 24 24",
      svgPath(
        svg.d := "M12.152 6.896c-.948 0-2.415-1.078-3.96-1.04-2.04.027-3.91 1.183-4.961 3.014-2.117 3.675-.546 9.103 1.519 12.09 1.013 1.454 2.208 3.09 3.792 3.039 1.52-.065 2.09-.987 3.935-.987 1.831 0 2.35.987 3.96.948 1.637-.026 2.676-1.48 3.676-2.948 1.156-1.688 1.636-3.325 1.662-3.415-.039-.013-3.182-1.221-3.22-4.857-.026-3.04 2.48-4.494 2.597-4.559-1.429-2.09-3.623-2.324-4.39-2.376-2-.156-3.675 1.09-4.61 1.09zM15.53 3.83c.843-1.012 1.4-2.427 1.245-3.83-1.207.052-2.662.805-3.532 1.818-.78.896-1.454 2.338-1.273 3.714 1.338.104 2.715-.688 3.559-1.701",
        svg.fill := "currentColor"
      )
    )

  private def googleIcon: SvgElement =
    svgTag(
      svg.viewBox := "0 0 24 24",
      svgPath(
        svg.d := "M12.48 10.92v3.28h7.84c-.24 1.84-.853 3.187-1.787 4.133-1.147 1.147-2.933 2.4-6.053 2.4-4.827 0-8.6-3.893-8.6-8.72s3.773-8.72 8.6-8.72c2.6 0 4.507 1.027 5.907 2.347l2.307-2.307C18.747 1.44 16.133 0 12.48 0 5.867 0 .307 5.387.307 12s5.56 12 12.173 12c3.573 0 6.267-1.173 8.373-3.36 2.16-2.16 2.84-5.213 2.84-7.667 0-.76-.053-1.467-.173-2.053H12.48z",
        svg.fill := "currentColor"
      )
    )

  def apply(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      cls := "flex flex-col gap-6",
      mods,
      Card(
        Card.header(
          cls := "text-center",
          Card.title(cls := "text-xl", "Welcome back"),
          Card.description("Login with your Apple or Google account")
        ),
        Card.content(
          form(
            Field.group(
              Field(
                Button.of(
                  _.variant(Button.Variant.Outline),
                  _.size(Button.Size.Default),
                  _ => typ := "button",
                  _ => appleIcon,
                  _ => "Login with Apple"
                ),
                Button.of(
                  _.variant(Button.Variant.Outline),
                  _.size(Button.Size.Default),
                  _ => typ := "button",
                  _ => googleIcon,
                  _ => "Login with Google"
                )
              ),
              Field.separator(cls := "*:data-[slot=field-separator-content]:bg-card")(span("Or continue with")),
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
                    cls := "ms-auto text-sm underline-offset-4 hover:underline",
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
                Field.description(cls := "text-center", "Don't have an account? ", a(href := "#", "Sign up"))
              )
            )
          )
        )
      ),
      Field.description(
        cls := "px-6 text-center",
        "By clicking continue, you agree to our ",
        a(href := "#", "Terms of Service"),
        " and ",
        a(href := "#", "Privacy Policy"),
        "."
      )
    )
