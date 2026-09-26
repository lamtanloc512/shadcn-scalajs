package shadcnscalajs.blocks.otp01

import com.raquo.laminar.api.L.*

/** Authored page composition for the OTP block. See Login01's note on file-based routing. */
object Otp01:

  def apply(): HtmlElement =
    div(
      cls := "flex min-h-svh w-full items-center justify-center p-6 md:p-10",
      div(cls := "w-full max-w-sm", OtpForm())
    )
