package shadcnscalajs.site

import com.raquo.laminar.api.L.*
import shadcnscalajs.blocks.calendar01.Calendar01
import shadcnscalajs.blocks.dashboard01.Dashboard01
import shadcnscalajs.blocks.login01.Login01
import shadcnscalajs.blocks.login03.Login03
import shadcnscalajs.blocks.login04.Login04
import shadcnscalajs.blocks.otp01.Otp01
import shadcnscalajs.blocks.sidebar03.Sidebar03
import shadcnscalajs.blocks.sidebar07.Sidebar07
import shadcnscalajs.blocks.signup01.Signup01

/** The docs site's view of the block catalog.
  *
  * Hand-maintained, matching `componentNavList`'s house style. It can drift from the `*.registry.json` sidecars in
  * `modules/blocks`; generating it from those sidecars is a tracked follow-up.
  */
object Blocks:

  final case class Meta(name: String, title: String, description: String, categories: List[String])

  val all: List[Meta] = List(
    Meta("login-01", "Login 01", "A simple login form.", List("authentication", "login")),
    Meta(
      "login-03",
      "Login 03",
      "A login page with a muted background and brand header.",
      List("authentication", "login")
    ),
    Meta(
      "login-04",
      "Login 04",
      "A login page with form and image.",
      List("authentication", "login")
    ),
    Meta(
      "signup-01",
      "Signup 01",
      "A signup form with name, email and password fields.",
      List("authentication", "signup")
    ),
    Meta("otp-01", "OTP 01", "A one-time-password verification form.", List("authentication", "otp")),
    Meta("calendar-01", "Calendar 01", "A single date picker.", List("calendar", "date")),
    Meta("sidebar-03", "Sidebar 03", "A sidebar with submenus.", List("sidebar")),
    Meta("sidebar-07", "Sidebar 07", "A sidebar that collapses to icons.", List("sidebar")),
    Meta(
      "dashboard-01",
      "Dashboard 01",
      "A dashboard with sidebar, charts and data table.",
      List("dashboard", "analytics")
    )
  )

  def find(name: String): Option[Meta] = all.find(_.name == name)

  /** The live block itself, for the preview route. */
  def render(name: String): Option[HtmlElement] = name match
    case "login-01"     => Some(Login01())
    case "login-03"     => Some(Login03())
    case "login-04"     => Some(Login04())
    case "signup-01"    => Some(Signup01())
    case "otp-01"       => Some(Otp01())
    case "calendar-01"  => Some(Calendar01())
    case "sidebar-03"   => Some(Sidebar03())
    case "sidebar-07"   => Some(Sidebar07())
    case "dashboard-01" => Some(Dashboard01())
    case _              => None

  /** Categories in first-seen order, each with its blocks. */
  def byCategory: List[(String, List[Meta])] =
    all
      .flatMap(m => m.categories.headOption.map(_ -> m))
      .groupBy(_._1)
      .toList
      .map { case (c, pairs) =>
        c -> pairs.map(_._2)
      }
      .sortBy(_._1)
