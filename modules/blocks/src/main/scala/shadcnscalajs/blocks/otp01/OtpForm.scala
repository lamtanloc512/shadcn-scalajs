package shadcnscalajs.blocks.otp01

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.*

/** Authored for shadcn-scalajs — there is no upstream `otp-01` block. Structure follows `login-01`'s card + Field
  * layout, with InputOTP as the input.
  */
object OtpForm:

  def apply(mods: Modifier[HtmlElement]*): HtmlElement =
    val codeVar = Var("")
    div(
      cls := "flex flex-col gap-6",
      mods,
      Card(
        Card.header(
          Card.title("Enter your verification code"),
          Card.description("We sent a 6-digit code to m@example.com")
        ),
        Card.content(
          form(
            Field.group(
              Field(
                cls := "items-center",
                InputOTP(codeVar)
              ),
              Field(
                Button.of(
                  _.variant(Button.Variant.Primary),
                  _.size(Button.Size.Default),
                  _ => typ := "submit",
                  _ => "Verify"
                ),
                Field.description(
                  cls := "text-center",
                  "Didn't get a code? ",
                  a(href := "#", "Resend")
                )
              )
            )
          )
        )
      )
    )
