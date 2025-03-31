package test.unit

/**
 * A test verifying that a boolean expression evaluates to `false`.
 * This is a specialized version of [[Property]] where the property is `!x`.
 *
 * @param name The name of the test.
 * @param toEvaluate The call-by-name boolean expression to evaluate.
 * @param timeoutOverride Optional specific timeout duration (in seconds) for this test.
 * @author Pepe Gallardo
 */
class Refute(
  override val name: String,
  toEvaluate: => Boolean,
  override protected val timeoutOverride: Option[Int] = None
) extends Property[Boolean](
  name = name,
  toEvaluate = toEvaluate,
  property = x => !x, // The property is that the value must be false
  mkString = None, // Don't provide direct mkString function
  mkStringKey = Some(v => s"property.was.$v"), // Provide key pattern: "property.was.true" or "property.was.false"
  help = None, // Don't provide direct help string
  helpKey = Some("property.must.be.false"), // Provide I18n key for "property should be false"
  timeoutOverride = timeoutOverride
) {
  // Inherits executeTest from Property
}

/**
 * Companion object for the [[Refute]] test class.
 * Provides factory `apply` methods for convenient test creation.
 */
object Refute {
  /**
   * Creates a Refute test with an optional timeout override.
   *
   * @param name Test name.
   * @param toEvaluate Boolean expression to evaluate (must be false).
   * @param timeoutOverride Optional specific timeout in seconds.
   * @return A `Refute` test instance.
   */
  def apply(
    name: String,
    toEvaluate: => Boolean,
    timeoutOverride: Option[Int] = None
  ): Refute =
    new Refute(name, toEvaluate, timeoutOverride) // Call constructor directly

  /**
   * Creates a Refute test with a specific timeout override.
   *
   * @param name Test name.
   * @param toEvaluate Boolean expression to evaluate (must be false).
   * @param timeoutOverride Specific timeout in seconds.
   * @return A `Refute` test instance.
   */
  def apply(
    name: String,
    toEvaluate: => Boolean,
    timeoutOverride: Int
  ): Refute =
    new Refute(name, toEvaluate, Some(timeoutOverride)) // Call constructor directly
}
