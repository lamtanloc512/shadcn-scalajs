package shadcnscalajs.site.create

import com.raquo.laminar.api.L.*
import org.scalajs.dom

import shadcnscalajs.ui.{Button, Dialog, Field, Input}

/** Dialog for loading a preset by code or `--preset` flag. Ported from `open-preset.svelte`. */
object OpenPreset:

  private val presetFlagPattern = """(?i)^--preset\b\s+(.+)$""".r

  def apply(state: CreateState, buttonLabel: String = "Open", mods: Modifier[HtmlElement]*): HtmlElement =
    val openVar = Var(false)
    val codeVar = Var("")
    val triggerRef = Var(Option.empty[dom.html.Element])

    def parsePreset(raw: String): Option[String] =
      val trimmed = raw.trim
      if trimmed.isEmpty then None
      else
        val extracted = trimmed match
          case presetFlagPattern(rest) => rest.trim
          case _                       => trimmed
        if Preset.isPresetCode(extracted) && Preset.decode(extracted).isDefined then Some(extracted)
        else None

    val nextPreset: Signal[Option[String]] = codeVar.signal.map(parsePreset)
    val isInvalid: Signal[Boolean] = codeVar.signal.map { raw =>
      raw.trim.nonEmpty && parsePreset(raw).isEmpty
    }

    def focusTrigger(): Unit = triggerRef.now().foreach(_.focus())

    div(
      cls := "contents",
      Button
        .of(
          _.variant(Button.Variant.Outline),
          _.size(Button.Size.Default),
          _ =>
            cls := "touch-manipulation bg-transparent! px-2! py-0! text-sm! transition-none select-none hover:bg-muted! pointer-coarse:h-10!",
          _ => onMountCallback { ctx => triggerRef.set(Some(ctx.thisNode.ref)) },
          _ => onClick --> { (_: dom.MouseEvent) => openVar.set(true) },
          _ => buttonLabel
        )
        .amend(mods),
      Dialog(openVar)(
        // Dialog mirrors native Escape/backdrop into openVar; clear + refocus on close.
        openVar.signal --> { open =>
          if !open then
            codeVar.set("")
            focusTrigger()
        },
        cls := "dark",
        form(
          cls := "flex flex-col gap-4",
          onSubmit --> { ev =>
            ev.preventDefault()
            parsePreset(codeVar.now()) match
              case Some(code) =>
                state.applyPresetCode(code)
                openVar.set(false)
              case None => ()
          },
          headerTag(
            h2(cls := "text-lg font-semibold", "Open Preset"),
            p(cls := "text-sm text-muted-foreground", "Paste a preset code to load a saved configuration.")
          ),
          div(
            cls := "py-4",
            Field(
              dataAttr("invalid") <-- isInvalid.map(invalid => if invalid then "true" else "false"),
              Field.label("Preset code", cls := "sr-only", forId := "preset-code"),
              Field.content(
                Input(
                  idAttr := "preset-code",
                  placeholder := "b2D0wqNxT or --preset b2D0wqNxT",
                  autoCapitalize := "none",
                  spellCheck := false,
                  cls := "h-10 md:h-8",
                  controlled(
                    value <-- codeVar,
                    onInput.mapToValue --> codeVar
                  ),
                  aria.invalid <-- isInvalid.map(invalid => if invalid then "true" else "false")
                )
              )
            )
          ),
          footerTag(
            cls := "flex flex-col-reverse gap-2 sm:flex-row sm:justify-end",
            Button.of(
              _.variant(Button.Variant.Outline),
              _.size(Button.Size.Default),
              _ => typ := "button",
              _ => onClick --> { (_: dom.MouseEvent) => openVar.set(false) },
              _ => "Cancel"
            ),
            Button.of(
              _.variant(Button.Variant.Primary),
              _.size(Button.Size.Default),
              _ => typ := "submit",
              _ => disabled <-- nextPreset.map(_.isEmpty),
              _ => "Open"
            )
          )
        )
      )
    )
