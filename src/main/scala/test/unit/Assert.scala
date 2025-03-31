package test.unit

/**
 * A test verifying that a boolean expression evaluates to `true`.
 *
 * @param name The name of the test.
 * @param toEvaluate The call-by-name boolean expression to evaluate.
 * @param timeoutOverride Optional specific timeout duration (in seconds) for this test,
 *                        overriding the default from `Config`.
 * @author Pepe Gallardo
 */
class Assert(
  override val name: String,
  toEvaluate: => Boolean,
  override protected val timeoutOverride: Option[Int] = None // Store the override
) extends Property[Boolean](
  name = name,
  toEvaluate = toEvaluate,
  property = x => x, // The property is that the value must be true
  mkString = None, // Don't provide direct mkString function
  mkStringKey = Some(v => s"property.was.$v"), // Key pattern for mkString
  help = None, // Don't provide direct help string
  helpKey = Some("property.must.be.true"), // Key for the help message
  timeoutOverride = timeoutOverride
) {
  // Inherits executeTest from Property
}

/**
 * Companion object for the [[Assert]] test class.
 * Provides factory `apply` methods for convenient test creation.
 */
object Assert {
  /**
   * Creates an Assert test with an optional timeout override.
   *
   * @param name Test name.
   * @param toEvaluate Boolean expression to evaluate (must be true).
   * @param timeoutOverride Optional specific timeout in seconds.
   * @return An `Assert` test instance.
   */
  def apply(
    name: String,
    toEvaluate: => Boolean,
    timeoutOverride: Option[Int] = None
  ): Assert =
    new Assert(name, toEvaluate, timeoutOverride)

  /**
   * Creates an Assert test with a specific timeout override.
   *
   * @param name Test name.
   * @param toEvaluate Boolean expression to evaluate (must be true).
   * @param timeoutOverride Specific timeout in seconds.
   * @return An `Assert` test instance.
   */
  def apply(
    name: String,
    toEvaluate: => Boolean,
    timeoutOverride: Int
  ): Assert =
    new Assert(name, toEvaluate, Some(timeoutOverride))
}
