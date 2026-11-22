package shadcnscalajs.webcomponents

import com.raquo.laminar.api.L.*
import shadcnscalajs.core.Tags.slotTag
import shadcnscalajs.ui.Questionnaire

import scala.scalajs.js

/** `<sc-questionnaire>` — the questions come as JSON (attribute or `questions` property); answers leave as an
  * `sc-submit` event whose `detail` maps each question id to an array of values.
  *
  * {{{
  * <sc-questionnaire questions='[
  *   {"id":"stack","title":"Which backend?","choices":[{"value":"http4s","label":"http4s","shortcut":"1"}]},
  *   {"id":"extras","title":"Extras?","kind":"multiple","required":false,"skippable":true,
  *    "choices":[{"value":"ci","label":"CI"},{"value":"docker","label":"Docker"}]},
  *   {"id":"name","title":"Project name?","kind":"text","placeholder":"my-app"}
  * ]'>
  *   <p slot="done">Thanks!</p>
  * </sc-questionnaire>
  * }}}
  *
  * `kind` is `single` (default), `multiple`, or `text`. A question is required unless `"required": false`. Content in
  * `slot="done"` replaces the form after submit; `el.reset()` starts over.
  */
class ScQuestionnaire extends ScElementBase:

  private val questionsVar = Var(Seq.empty[Questionnaire.Question])
  private var state: Option[Questionnaire.State] = None

  observeAttribute("questions")(v => questionsVar.set(ScQuestionnaire.parse(v.orNull)))
  jsonProperty("questions")(v => questionsVar.set(ScQuestionnaire.parse(v)))

  this.asInstanceOf[js.Dynamic].reset = (() => state.foreach(_.reset())): js.Function0[Unit]

  mount(
    div(
      cls := "contents",
      child <-- questionsVar.signal.map { questions =>
        if questions.isEmpty then
          state = None
          div()
        else
          val s = Questionnaire.state(questions)
          state = Some(s)
          Questionnaire(
            s,
            onSubmit = answers =>
              emit(
                "sc-submit",
                js.Dictionary(answers.toSeq.map((id, values) => id -> js.Array(values*))*).asInstanceOf[js.Any]
              ),
            done = div(slotTag(nameAttr := "done"))
          )()
      }
    )
  )

object ScQuestionnaire:
  def register(): Unit =
    ScElements.define("sc-questionnaire", js.constructorOf[ScQuestionnaire], "questions")

  private def str(d: js.Dynamic, key: String): Option[String] =
    val raw = d.selectDynamic(key)
    if raw == null || js.isUndefined(raw) then None else Some(raw.toString)

  private def bool(d: js.Dynamic, key: String, default: Boolean): Boolean =
    val raw = d.selectDynamic(key)
    if js.typeOf(raw) == "boolean" then raw.asInstanceOf[Boolean] else default

  private def array(value: js.Any): Seq[js.Dynamic] =
    val parsed =
      if value != null && js.typeOf(value) == "string" then
        try js.JSON.parse(value.asInstanceOf[String])
        catch case _: Throwable => null
      else value
    if parsed != null && js.Array.isArray(parsed) then parsed.asInstanceOf[js.Array[js.Dynamic]].toSeq else Nil

  private[webcomponents] def parse(value: js.Any): Seq[Questionnaire.Question] =
    array(value).filter(d => d != null && js.typeOf(d) == "object").flatMap { d =>
      for
        id <- str(d, "id")
        title <- str(d, "title")
      yield Questionnaire.Question(
        id = id,
        title = title,
        kind = str(d, "kind") match
          case Some("multiple") => Questionnaire.Kind.Multiple
          case Some("text")     => Questionnaire.Kind.Text
          case _                => Questionnaire.Kind.Single
        ,
        choices = array(d.selectDynamic("choices").asInstanceOf[js.Any]).flatMap { c =>
          str(c, "value").map { v =>
            Questionnaire.Choice(
              value = v,
              label = str(c, "label").getOrElse(v),
              description = str(c, "description"),
              shortcut = str(c, "shortcut"),
              disabled = bool(c, "disabled", false)
            )
          }
        },
        description = str(d, "description"),
        required = bool(d, "required", true),
        skippable = bool(d, "skippable", false),
        placeholder = str(d, "placeholder").getOrElse("")
      )
    }
