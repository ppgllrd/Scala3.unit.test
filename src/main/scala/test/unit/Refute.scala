package test.unit

/**
 * A specific type of test that verifies a boolean expression evaluates to `false`.
 * It is implemented as a specialized [[Property]] test where the property is `!x`.
 *
 * @param name The descriptive name of the test case.
 * @param toEvaluate The call-by-name boolean expression to be evaluated. Its result must be `false` for the test to pass.
 * @param timeoutOverride An optional duration in seconds to override the default timeout specified in the [[Config]].
 * @author Pepe Gallardo & Gemini
 */
class Refute(
  override val name: String,
  toEvaluate: => Boolean,
  override protected val timeoutOverride: Option[Int] = None
) extends Property[Boolean](
  name = name,
  toEvaluate = toEvaluate,
  property = x => !x, // The property being checked is that the value is false.
  mkString = None, // Use key-based string formatting for boolean results.
  mkStringKey = Some(v => s"property.was.$v"), // Provides keys like "property.was.true" or "property.was.false".
  help = None, // Use key-based help message.
  helpKey = Some("property.must.be.false"), // Provides the key for "property should be false".
  timeoutOverride = timeoutOverride
) {
  // Execution logic is inherited from the Property base class.
}

/**
 * Companion object for the [[Refute]] class.
 * Provides factory `apply` methods for convenient creation of `Refute` tests.
 *
 * @author Pepe Gallardo & Gemini
 */
object Refute {
  /**
   * Creates a `Refute` test instance.
   *
   * @param name The descriptive name of the test case.
   * @param toEvaluate The call-by-name boolean expression. Must evaluate to `false` to pass.
   * @param timeoutOverride Optional duration in seconds to override the default timeout.
   * @return A [[Refute]] test instance.
   */
  def apply(
    name: String,
    toEvaluate: => Boolean,
    timeoutOverride: Option[Int] = None
  ): Refute =
    new Refute( // Call constructor directly
      name = name,
      toEvaluate = toEvaluate,
      timeoutOverride = timeoutOverride
    )

  /**
   * Creates a `Refute` test instance with a specific timeout override.
   *
   * @param name The descriptive name of the test case.
   * @param toEvaluate The call-by-name boolean expression. Must evaluate to `false` to pass.
   * @param timeoutOverride The specific timeout duration in seconds for this test.
   * @return A [[Refute]] test instance.
   */
  def apply(
    name: String,
    toEvaluate: => Boolean,
    timeoutOverride: Int
  ): Refute =
    new Refute( // Call constructor directly
      name = name,
      toEvaluate = toEvaluate,
      timeoutOverride = Some(timeoutOverride) // Wrap Int in Some
    )
}
