package test.unit

/**
 * A specific type of test that verifies if the result of an evaluated expression
 * is equal to an expected value using the standard equality operator (`==`).
 *
 * It extends [[EqualBy]], providing `_ == _` as the equality function.
 *
 * @param name The descriptive name of the test case.
 * @param toEvaluate The call-by-name expression whose result will be evaluated.
 * @param expected The value the result of `toEvaluate` is expected to be equal to (using `==`).
 * @param mkString A function to convert values of type `T` to a string representation for reporting. Defaults to `.toString`.
 * @param timeoutOverride An optional duration in seconds to override the default test timeout.
 * @tparam T The type of the value produced by `toEvaluate` and the `expected` value.
 * @author Pepe Gallardo & Gemini
 */
class Equal[T](
  override val name: String,
  toEvaluate: => T,
  expected: T, // Renamed parameter for clarity in constructor call
  mkString: T => String = (obj: T) => obj.toString,
  override protected val timeoutOverride: Option[Int] = None
) extends EqualBy[T](
  name = name,
  toEvaluate = toEvaluate,
  expected = expected,
  equalsFn = _ == _, // Use standard equality comparison
  mkString = mkString,
  timeoutOverride = timeoutOverride
) {
  // Execution logic is inherited from the EqualBy base class.
}

/**
 * Companion object for the [[Equal]] class.
 * Provides factory `apply` methods for convenient creation of `Equal` tests (using `==`).
 *
 * @author Pepe Gallardo & Gemini
 */
object Equal {
  /**
   * Creates an `Equal` test instance using standard `==` comparison.
   *
   * @param name Test name.
   * @param toEvaluate Expression to evaluate.
   * @param expected Expected value (compared using `==`).
   * @param mkString Function to convert `T` to String. Defaults to `.toString`.
   * @param timeoutOverride Optional specific timeout in seconds.
   * @tparam T Type of the expression and result.
   * @return An `Equal[T]` test instance.
   */
  def apply[T](
    name: String,
    toEvaluate: => T,
    expected: T,
    mkString: T => String = (obj: T) => obj.toString,
    timeoutOverride: Option[Int] = None
  ): Equal[T] =
    new Equal(
        name = name,
        toEvaluate = toEvaluate,
        expected = expected,
        mkString = mkString,
        timeoutOverride = timeoutOverride
    )

  /**
   * Creates an `Equal` test instance with an optional timeout override.
   * Uses the default `mkString` (`.toString`).
   *
   * @param name Test name.
   * @param toEvaluate Expression to evaluate.
   * @param expected Expected value.
   * @param timeoutOverride Optional specific timeout in seconds.
   * @tparam T Type of the expression and result.
   * @return An `Equal[T]` test instance.
   */
  def apply[T](
    name: String,
    toEvaluate: => T,
    expected: T,
    timeoutOverride: Option[Int] // Parameter name matches constructor
  ): Equal[T] =
    new Equal(
        name = name,
        toEvaluate = toEvaluate,
        expected = expected,
        mkString = (obj: T) => obj.toString, // Default mkString
        timeoutOverride = timeoutOverride
    )

  /**
   * Creates an `Equal` test instance with a specific timeout override.
   * Uses the default `mkString` (`.toString`).
   *
   * @param name Test name.
   * @param toEvaluate Expression to evaluate.
   * @param expected Expected value.
   * @param timeoutOverride Specific timeout in seconds.
   * @tparam T Type of the expression and result.
   * @return An `Equal[T]` test instance.
   */
  def apply[T](
    name: String,
    toEvaluate: => T,
    expected: T,
    timeoutOverride: Int // Parameter name matches constructor usage
  ): Equal[T] =
    new Equal(
        name = name,
        toEvaluate = toEvaluate,
        expected = expected,
        mkString = (obj: T) => obj.toString, // Default mkString
        timeoutOverride = Some(timeoutOverride) // Specific timeout
    )


  /**
   * Creates an `Equal` test instance with a custom `mkString` function.
   * Uses the default timeout.
   *
   * @param name Test name.
   * @param toEvaluate Expression to evaluate.
   * @param expected Expected value.
   * @param mkString Function to convert `T` to String.
   * @tparam T Type of the expression and result.
   * @return An `Equal[T]` test instance.
   */
  def apply[T](
    name: String,
    toEvaluate: => T,
    expected: T,
    mkString: T => String
  ): Equal[T] =
    new Equal(
        name = name,
        toEvaluate = toEvaluate,
        expected = expected,
        mkString = mkString,
        timeoutOverride = None // Default timeout
    )

  /**
   * Creates an `Equal` test instance using only the required arguments.
   * Uses the default `mkString` (`.toString`) and the default timeout.
   *
   * @param name Test name.
   * @param toEvaluate Expression to evaluate.
   * @param expected Expected value.
   * @tparam T Type of the expression and result.
   * @return An `Equal[T]` test instance.
   */
  def apply[T](
    name: String,
    toEvaluate: => T,
    expected: T
  ): Equal[T] =
    new Equal(
        name = name,
        toEvaluate = toEvaluate,
        expected = expected,
        mkString = (obj: T) => obj.toString, // Default mkString
        timeoutOverride = None // Default timeout
    )
}
