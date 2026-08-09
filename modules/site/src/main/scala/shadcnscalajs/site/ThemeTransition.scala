package shadcnscalajs.site

import org.scalajs.dom

import scala.scalajs.js

/** Circular-reveal light/dark switch, the effect vuejs.org uses.
  *
  * The View Transitions API snapshots the page either side of the `dark` class flip, so animating a growing clip-path
  * circle on one snapshot wipes the new theme in from wherever the reader clicked. Doing the same with CSS transitions
  * is not equivalent: every themed property on every element would have to animate independently, which both stutters
  * and leaves shadows and borders visibly out of step.
  *
  * Browsers without the API, and readers who asked for reduced motion, fall through to the instant flip.
  */
object ThemeTransition:

  private val DurationMs = 450

  /** Where a click landed, or `None` for keyboard and menu activations, which report `(0, 0)`. */
  def originOf(ev: dom.MouseEvent): Option[(Double, Double)] =
    if ev.clientX == 0 && ev.clientY == 0 then None else Some((ev.clientX.toDouble, ev.clientY.toDouble))

  /** Applies `change` — which must flip the theme synchronously — behind a circular reveal. */
  def run(origin: Option[(Double, Double)])(change: () => Unit): Unit =
    val doc = dom.document.asInstanceOf[js.Dynamic]
    if !supported(doc) then change()
    else
      val (x, y) = origin.getOrElse(fallbackOrigin)
      val update: js.Function0[Unit] = () => change()
      val onReady: js.Function1[js.Any, Unit] = _ => reveal(x, y)
      doc.startViewTransition(update).ready.asInstanceOf[js.Dynamic].applyDynamic("then")(onReady)

  private def supported(doc: js.Dynamic): Boolean =
    !js.isUndefined(doc.startViewTransition) &&
      doc.startViewTransition != null &&
      !dom.window.matchMedia("(prefers-reduced-motion: reduce)").matches

  /** Keyboard shortcuts and the command menu have no pointer, so start from the toggle the reader would have clicked.
    */
  private def fallbackOrigin: (Double, Double) =
    Option(dom.document.querySelector("[data-theme-toggle]")) match
      case Some(el) =>
        val rect = el.getBoundingClientRect()
        (rect.left + rect.width / 2, rect.top + rect.height / 2)
      case None => (dom.window.innerWidth / 2.0, dom.window.innerHeight / 2.0)

  private def reveal(x: Double, y: Double): Unit =
    // Reach the furthest corner, otherwise the circle stops short and the old theme survives in the far corner.
    val endRadius = math.hypot(
      math.max(x, dom.window.innerWidth - x),
      math.max(y, dom.window.innerHeight - y)
    )
    val collapsed = s"circle(0px at ${x}px ${y}px)"
    val expanded = s"circle(${endRadius}px at ${x}px ${y}px)"

    // The light snapshot is always the animated layer: it shrinks away when turning dark and grows in when turning
    // light. Animating the dark one instead would flash the light theme through the uncovered area.
    val turningDark = dom.document.documentElement.classList.contains("dark")
    val frames = if turningDark then js.Array(expanded, collapsed) else js.Array(collapsed, expanded)
    val pseudo = if turningDark then "::view-transition-old(root)" else "::view-transition-new(root)"

    dom.document.documentElement
      .asInstanceOf[js.Dynamic]
      .animate(
        js.Dynamic.literal(clipPath = frames),
        // `fill` must hold the last keyframe. Without it the clip reverts to unclipped on finish, and when turning dark
        // the animated layer is the light snapshot sitting above the new one — so it repaints the whole viewport light
        // for the frame between the animation ending and the snapshots being torn down.
        js.Dynamic.literal(
          duration = DurationMs,
          easing = "ease-in-out",
          pseudoElement = pseudo,
          fill = "forwards"
        )
      )
