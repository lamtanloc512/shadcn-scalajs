package shadcnscalajs.site

/** One-line summaries under each component title, following shadcn/ui's own wording where a Laminar port behaves the
  * same, and saying what differs where it does not.
  */
object ComponentDescriptions:

  private val all: Map[String, String] = Map(
    "accordion" -> "A vertically stacked set of interactive headings that each reveal a section of content.",
    "alert" -> "Displays a callout for user attention.",
    "alert-dialog" -> "A modal dialog that interrupts the user with important content and expects a response.",
    "aspect-ratio" -> "Displays content within a desired ratio.",
    "attachment" -> "A file chip for composers and messages, with media, title, upload status, and actions.",
    "avatar" -> "An image element with a fallback for representing the user.",
    "badge" -> "Displays a badge or a component that looks like a badge.",
    "breadcrumb" -> "Displays the path to the current resource using a hierarchy of links.",
    "bubble" -> "A chat bubble with color variants, alignment, and a reactions strip.",
    "button" -> "Displays a button or a component that looks like a button.",
    "button-group" -> "A container that groups related buttons together with consistent styling.",
    "calendar" -> "A date field component that allows users to enter and edit date.",
    "card" -> "Displays a card with header, content, and footer.",
    "carousel" -> "A carousel with motion and swipe, built with scroll snapping instead of Embla.",
    "chart" -> "Beautiful charts. Built with SVG and Laminar signals instead of Recharts.",
    "checkbox" -> "A control that allows the user to toggle between checked and not checked.",
    "code-block" -> "A framed source listing with a header, copy button, line numbers, and diff lines.",
    "collapsible" -> "An interactive component which expands and collapses a panel.",
    "combobox" -> "Autocomplete input and command palette with a list of suggestions.",
    "command" -> "Fast, composable command menu for Laminar.",
    "context-menu" -> "Displays a menu to the user — such as a set of actions or functions — triggered by a right click.",
    "data-table" ->
      "Powerful table and datagrids built from Table and pure Scala state: sorting, filtering, pagination, and row selection.",
    "date-picker" -> "A date picker component with range and presets.",
    "dialog" -> "A window overlaid on either the primary window or another dialog window, rendering the content underneath inert.",
    "direction" -> "Sets the text direction for a subtree so logical utilities and menus flip for right-to-left languages.",
    "drawer" -> "A drawer component that slides in from the edge of the screen.",
    "dropdown-menu" -> "Displays a menu to the user — such as a set of actions or functions — triggered by a button.",
    "empty" -> "Use the Empty component to display an empty state.",
    "field" -> "Combine labels, controls, and help text to compose accessible form fields and grouped inputs.",
    "form" -> "Building forms with Laminar signals, validation, and accessible error messages.",
    "hover-card" -> "For sighted users to preview content available behind a link.",
    "input" -> "Displays a form input field or a component that looks like an input field.",
    "input-group" -> "Add addons, buttons, and helper content to inputs.",
    "input-otp" -> "Accessible one-time password component with copy paste functionality.",
    "item" -> "A versatile component that you can use to display any content.",
    "kbd" -> "Used to display textual user input from keyboard.",
    "label" -> "Renders an accessible label associated with controls.",
    "marker" -> "An inline status line or divider inside a conversation.",
    "menubar" -> "A visually persistent menu common in desktop applications that provides quick access to a consistent set of commands.",
    "message" -> "Lays out one chat turn: avatar, header, content stack, and footer.",
    "message-scroller" -> "A chat viewport that sticks to the newest message and offers a jump-to-end button.",
    "native-select" -> "A styled native HTML select element with consistent design system integration.",
    "navigation-menu" -> "A collection of links for navigating websites.",
    "pagination" -> "Pagination with page navigation, next and previous links.",
    "popover" -> "Displays rich content in a portal, triggered by a button.",
    "progress" -> "Displays an indicator showing the completion progress of a task, typically displayed as a progress bar.",
    "questionnaire" -> "Asks one question at a time — choices or free text — with progress, shortcuts, and validation.",
    "radio" -> "A single radio input styled like the radio group's items.",
    "radio-group" -> "A set of checkable buttons — known as radio buttons — where no more than one can be checked at a time.",
    "range" -> "A styled native range input.",
    "range-calendar" -> "A calendar for picking a start and an end date.",
    "resizable" -> "Accessible resizable panel groups and layouts with keyboard support.",
    "scroll-area" -> "Augments native scroll functionality for custom, cross-browser styling.",
    "scrollbar" -> "Thin, theme-aware scrollbars for any overflow container.",
    "select" -> "Displays a list of options for the user to pick from — triggered by a button.",
    "separator" -> "Visually or semantically separates content.",
    "sheet" -> "Extends the Dialog component to display content that complements the main content of the screen.",
    "sidebar" -> "A composable, themeable and customizable sidebar component.",
    "skeleton" -> "Use to show a placeholder while content is loading.",
    "slider" -> "An input where the user selects a value from within a given range.",
    "sonner" -> "An opinionated toast component — pure Laminar, no React dependency.",
    "spinner" -> "An indicator that can be used to show a loading state.",
    "switch" -> "A control that allows the user to toggle between checked and not checked.",
    "table" -> "A responsive table component.",
    "tabs" -> "A set of layered sections of content — known as tab panels — that are displayed one at a time.",
    "textarea" -> "Displays a form textarea or a component that looks like a textarea.",
    "theme-switcher" -> "Switches between light, dark, and system color schemes.",
    "toast" -> "A succinct message that is displayed temporarily.",
    "toggle" -> "A two-state button that can be either on or off.",
    "toggle-group" -> "A set of two-state buttons that can be toggled on or off.",
    "tooltip" ->
      "A popup that displays information related to an element when the element receives keyboard focus or the mouse hovers over it.",
    "trace" -> "A compact, collapsible log of the steps an agent took.",
    "typography" -> "Styles for headings, paragraphs, lists — utility-class recipes, not a registry component."
  )

  def apply(slug: String, title: String): String =
    all.getOrElse(slug, s"The ${title.toLowerCase} primitive for shadcn-scalajs.")

/** Components added in the latest release, badged in the sidebar and on their pages. */
object NewComponents:
  val slugs: Set[String] = Set(
    "attachment",
    "bubble",
    "code-block",
    "direction",
    "marker",
    "message",
    "message-scroller",
    "questionnaire",
    "trace"
  )
