package test.unit

/**
 * A specific type of test that verifies a boolean expression evaluates to `true`.
 * It is implemented as a specialized [[Property]] test.
 *
 * @param name The descriptive name of the test case.
 * @param toEvaluate The call-by-name boolean expression to be evaluated. Its result must be `true` for the test to pass.
 * @param timeoutOverride An optional duration in seconds to override the default timeout specified in the [[Config]].
 * @author Pepe Gallardo & Gemini
 */
class Assert(
  override val name: String,
  toEvaluate: => Boolean,
  override protected val timeoutOverride: Option[Int] = None // Store the override
) extends Property[Boolean](
  name = name,
  toEvaluate = toEvaluate,
  property = x => x, // The property being checked is simply that the value is true.
  mkString = None, // Use key-based string formatting for boolean results.
  mkStringKey = Some(v => s"property.was.$v"), // Provides keys like "property.was.true" or "property.was.false".
  help = None, // Use key-based help message.
  helpKey = Some("property.must.be.true"), // Provides the key for "property should be true".
  timeoutOverride = timeoutOverride
) {
  // Execution logic is inherited from the Property base class.
}

/**
 * Companion object for the [[Assert]] class.
 * Provides factory `apply` methods for convenient creation of `Assert` tests.
 *
 * @author Pepe Gallardo & Gemini
 */
object Assert {
  /**
   * Creates an `Assert` test instance.
   *
   * @param name The descriptive name of the test case.
   * @param toEvaluate The call-by-name boolean expression. Must evaluate to `true` to pass.
   * @param timeoutOverride Optional duration in seconds to override the default timeout.
   * @return An [[Assert]] test instance.
   */
  def apply(
    name: String,
    toEvaluate: => Boolean,
    timeoutOverride: Option[Int] = None
  ): Assert =
    new Assert(
        name = name,
        toEvaluate = toEvaluate,
        timeoutOverride = timeoutOverride
    )

  /**
   * Creates an `Assert` test instance with a specific timeout override.
   *
   * @param name The descriptive name of the test case.
   * @param toEvaluate The call-by-name boolean expression. Must evaluate to `true` to pass.
   * @param timeoutOverride The specific timeout duration in seconds for this test.
   * @return An [[Assert]] test instance.
   */
  def apply(
    name: String,
    toEvaluate: => Boolean,
    timeoutOverride: Int
  ): Assert =
    new Assert(
        name = name,
        toEvaluate = toEvaluate,
        timeoutOverride = Some(timeoutOverride)
    )
}
