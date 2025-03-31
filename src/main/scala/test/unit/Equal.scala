package test.unit

/**
 * A test verifying that an evaluated expression is equal (using standard `==`)
 * to an expected result. It extends [[EqualBy]] using `==` as the comparison function.
 *
 * @param name Test name.
 * @param toEvaluate The call-by-name expression to evaluate.
 * @param expected The expected value.
 * @param mkString Function to convert `T` to String for reporting. Defaults to `_.toString`.
 * @param timeoutOverride Optional specific timeout duration (in seconds) for this test.
 * @tparam T Type of the expression and result.
 * @author Pepe Gallardo
 */
class Equal[T](
  override val name: String,
  toEvaluate: => T,
  expected: T,
  mkString: T => String = (obj: T) => obj.toString,
  override protected val timeoutOverride: Option[Int] = None
) extends EqualBy[T](
  name = name,
  toEvaluate = toEvaluate,
  expected = expected,
  equalsFn = _ == _, // Use standard equality
  mkString = mkString,
  timeoutOverride = timeoutOverride
) {
  // Inherits executeTest from EqualBy
}

/**
 * Companion object for the [[Equal]] test class.
 * Provides factory `apply` methods for convenient test creation.
 */
object Equal {
  /**
   * Creates an Equal test with optional mkString and optional timeout.
   *
   * @param name Test name.
   * @param toEvaluate Expression to evaluate.
   * @param expected Expected value.
   * @param mkString Function to convert `T` to String. Defaults to `_.toString`.
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
    new Equal(name, toEvaluate, expected, mkString, timeoutOverride)

  /**
   * Creates an Equal test with an optional timeout (uses default mkString).
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
    timeoutOverride: Option[Int]
  ): Equal[T] =
    new Equal(name, toEvaluate, expected, mkString = (obj: T) => obj.toString, timeoutOverride = timeoutOverride)

  /**
   * Creates an Equal test with a specific timeout (uses default mkString).
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
    timeoutOverride: Int
  ): Equal[T] =
    new Equal(name, toEvaluate, expected, mkString = (obj: T) => obj.toString, timeoutOverride = Some(timeoutOverride))


  /**
   * Creates an Equal test with a custom mkString (uses default timeout).
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
    new Equal(name, toEvaluate, expected, mkString, timeoutOverride = None)

  /**
   * Creates an Equal test with only required arguments (uses default mkString and timeout).
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
    new Equal(name, toEvaluate, expected, mkString = (obj: T) => obj.toString, timeoutOverride = None)
}
