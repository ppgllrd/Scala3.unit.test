package test.unit

/**
 * Represents distinct types of arguments used in constructing localized and
 * formatted descriptive messages (help strings) for exception tests
 * (subclasses of [[ExceptionBy]]).
 *
 * Using these specific types allows the formatting logic (e.g., in [[ExceptionBy.formattedHelp]])
 * to apply appropriate styling (like colors) and joining logic based on the
 * argument's semantic meaning, decoupling it from the raw values.
 *
 * @author Pepe Gallardo & Gemini
 */
sealed trait HelpArg extends Product with Serializable

/**
 * Companion object and case class implementations for [[HelpArg]].
 * @author Pepe Gallardo & Gemini
 */
object HelpArg {
  /** Represents a single exception type name (e.g., "IOException"). */
  final case class TypeName(name: String) extends HelpArg

  /**
   * Represents a list of multiple exception type names. These are typically
   * joined together using a localized connector (like " or ") during formatting.
   * (e.g., Seq("IOException", "SQLException")).
   */
  final case class TypeNameList(names: Seq[String]) extends HelpArg

  /**
   * Represents an exact exception message string that was expected.
   * Formatting usually includes quoting the message (e.g., "\"Invalid input\"").
   */
  final case class ExactMessage(message: String) extends HelpArg

  /**
   * Represents the human-readable descriptive text associated with an
   * exception message predicate.
   */
  final case class PredicateHelp(text: String) extends HelpArg
}
