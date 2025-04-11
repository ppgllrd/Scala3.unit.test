package test.unit

import java.util.concurrent.{CompletableFuture, ExecutionException, TimeUnit, TimeoutException}

/**
 * Represents a test that verifies if the result of an expression is considered equal
 * to an expected value, using a custom equality function (`equalsFn`).
 *
 * This class serves as a base for equality tests and handles the asynchronous
 * execution, timeout, and exception handling logic common to tests.
 *
 * @param name The descriptive name of the test case.
 * @param toEvaluate The call-by-name expression whose result will be evaluated.
 * @param expected The value the result of `toEvaluate` is expected to be equal to, according to `equalsFn`.
 * @param equalsFn A function `(T, T) => Boolean` that defines the custom equality relation.
 * @param mkString A function to convert values of type `T` to a string representation for reporting. Defaults to `.toString`.
 * @param timeoutOverride An optional duration in seconds to override the default test timeout.
 * @tparam T The type of the value produced by `toEvaluate` and the `expected` value.
 * @author Pepe Gallardo & Gemini
 */
class EqualBy[T](
  override val name: String,
  toEvaluate: => T,
  protected val expected: T,
  protected val equalsFn: (T, T) => Boolean,
  protected val mkString: T => String = (obj: T) => obj.toString,
  override protected val timeoutOverride: Option[Int] = None
) extends Test {

  /**
   * Generates a localized description string indicating the expected value.
   * The expected value itself is colored using the logger's green color.
   *
   * @param config The configuration context providing localization and logger.
   * @return A formatted string like "Expected result was: [green]<expected_value>[reset]".
   */
  private def expectedDescription()(using config: Config): String =
    config.msg(key = "expected.result", args = config.logger.green(mkString(expected)))


  /**
   * Executes the core logic of the `EqualBy` test.
   * It evaluates the `toEvaluate` expression asynchronously.
   * If the evaluation completes successfully within the timeout:
   *  - It compares the result with `expected` using `equalsFn`.
   *  - Returns [[TestResult.Success]] if equal, [[TestResult.EqualityFailure]] otherwise.
   * If the evaluation times out or throws an unexpected exception:
   *  - Returns [[TestResult.TimeoutFailure]] or [[TestResult.UnexpectedExceptionFailure]] respectively.
   *
   * @param config The configuration context for this test run, containing the resolved timeout.
   * @return A [[TestResult]] indicating the outcome.
   */
  override protected def executeTest(using config: Config): TestResult = {
    lazy val currentExpectedDesc = expectedDescription() // Generate once before async

    val future = CompletableFuture.supplyAsync(() => {
      try {
        val result = toEvaluate // Evaluate the expression
        if (equalsFn(result, expected)) {
          TestResult.Success() // Pass if the custom equality holds
        } else {
          // Fail, providing expected, actual, and the string formatter
          TestResult.EqualityFailure(expected = expected, actual = result, mkString = mkString)
        }
      } catch {
        // Handle potential exceptions during evaluation
        case ie: InterruptedException =>
          Thread.currentThread().interrupt() // Preserve interrupt status
          TestResult.UnexpectedExceptionFailure(thrown = ie, originalExpectationDescription = currentExpectedDesc)
        case t: Throwable =>
          TestResult.UnexpectedExceptionFailure(thrown = t, originalExpectationDescription = currentExpectedDesc)
      }
    })

    try {
      // Wait for the future to complete, respecting the timeout
      future.get(config.timeout, TimeUnit.SECONDS)
    } catch {
      // Handle issues during the waiting phase
      case _: TimeoutException =>
        future.cancel(true) // Attempt to cancel the task
        TestResult.TimeoutFailure(timeout = config.timeout, expectedBehaviorDescription = currentExpectedDesc)
      case e: ExecutionException =>
        // Exception occurred inside the future's execution
        val cause = Option(e.getCause).getOrElse(e) // Prefer the underlying cause
        TestResult.UnexpectedExceptionFailure(thrown = cause, originalExpectationDescription = currentExpectedDesc)
      case e: InterruptedException =>
        // The waiting thread was interrupted
        Thread.currentThread().interrupt()
        TestResult.UnexpectedExceptionFailure(thrown = e, originalExpectationDescription = currentExpectedDesc)
      case e: java.util.concurrent.CancellationException =>
        // The future was cancelled, likely due to timeout handling
         TestResult.TimeoutFailure(timeout = config.timeout, expectedBehaviorDescription = currentExpectedDesc) // Treat as timeout
      case e: java.lang.Exception =>
        // Other potential exceptions during .get()
        TestResult.UnexpectedExceptionFailure(thrown = e, originalExpectationDescription = currentExpectedDesc)
    }
  }
}

/**
 * Companion object for the [[EqualBy]] class.
 * Provides factory `apply` methods for convenient creation of `EqualBy` tests.
 *
 * @author Pepe Gallardo & Gemini
 */
object EqualBy {
  /**
   * Creates an `EqualBy` test instance with a custom equality function.
   *
   * @param name The descriptive name of the test case.
   * @param toEvaluate The expression whose result will be evaluated.
   * @param expected The value the result is expected to equal (via `equalsFn`).
   * @param equalsFn The custom equality function `(T, T) => Boolean`.
   * @param mkString Function to convert `T` to String for reporting. Defaults to `.toString`.
   * @param timeoutOverride Optional specific timeout in seconds.
   * @tparam T The type of the expression and result.
   * @return An `EqualBy[T]` test instance.
   */
  def apply[T](
    name: String,
    toEvaluate: => T,
    expected: T,
    equalsFn: (T, T) => Boolean,
    mkString: T => String = (obj: T) => obj.toString,
    timeoutOverride: Option[Int] = None
  ): EqualBy[T] =
    new EqualBy(
        name = name,
        toEvaluate = toEvaluate,
        expected = expected,
        equalsFn = equalsFn,
        mkString = mkString,
        timeoutOverride = timeoutOverride
    )

  /**
   * Creates an `EqualBy` test instance with a custom equality function and an optional timeout override.
   * Uses the default `mkString` (`.toString`).
   *
   * @param name Test name.
   * @param toEvaluate Expression to evaluate.
   * @param expected The expected value.
   * @param equalsFn Custom equality function `(T, T) => Boolean`.
   * @param timeoutOverride Optional specific timeout in seconds.
   * @tparam T Type of the expression and result.
   * @return An `EqualBy[T]` test instance.
   */
  def apply[T](
    name: String,
    toEvaluate: => T,
    expected: T,
    equalsFn: (T, T) => Boolean,
    timeoutOverride: Option[Int] // Parameter name matches constructor
  ): EqualBy[T] =
    new EqualBy(
        name = name,
        toEvaluate = toEvaluate,
        expected = expected,
        equalsFn = equalsFn,
        mkString = (obj: T) => obj.toString, // Default mkString
        timeoutOverride = timeoutOverride
    )

  /**
   * Creates an `EqualBy` test instance with a custom equality function and a custom `mkString` function.
   * Uses the default timeout.
   *
   * @param name Test name.
   * @param toEvaluate Expression to evaluate.
   * @param expected The expected value.
   * @param equalsFn Custom equality function `(T, T) => Boolean`.
   * @param mkString Function to convert `T` to String for reporting.
   * @tparam T Type of the expression and result.
   * @return An `EqualBy[T]` test instance.
   */
  def apply[T](
    name: String,
    toEvaluate: => T,
    expected: T,
    equalsFn: (T, T) => Boolean,
    mkString: T => String
  ): EqualBy[T] =
    new EqualBy(
        name = name,
        toEvaluate = toEvaluate,
        expected = expected,
        equalsFn = equalsFn,
        mkString = mkString,
        timeoutOverride = None // Default timeout
    )

  /**
   * Creates an `EqualBy` test instance with a custom equality function.
   * Uses the default `mkString` (`.toString`) and the default timeout.
   *
   * @param name Test name.
   * @param toEvaluate Expression to evaluate.
   * @param expected The expected value.
   * @param equalsFn Custom equality function `(T, T) => Boolean`.
   * @tparam T Type of the expression and result.
   * @return An `EqualBy[T]` test instance.
   */
  def apply[T](
    name: String,
    toEvaluate: => T,
    expected: T,
    equalsFn: (T, T) => Boolean
  ): EqualBy[T] =
    new EqualBy(
        name = name,
        toEvaluate = toEvaluate,
        expected = expected,
        equalsFn = equalsFn,
        mkString = (obj: T) => obj.toString, // Default mkString
        timeoutOverride = None // Default timeout
    )

  /**
   * Creates an `EqualBy` test instance with a custom equality function and a specific timeout override.
   * Uses the default `mkString` (`.toString`).
   *
   * @param name Test name.
   * @param toEvaluate Expression to evaluate.
   * @param expected The expected value.
   * @param equalsFn Custom equality function `(T, T) => Boolean`.
   * @param timeoutOverride Specific timeout in seconds.
   * @tparam T Type of the expression and result.
   * @return An `EqualBy[T]` test instance.
   */
  def apply[T](
    name: String,
    toEvaluate: => T,
    expected: T,
    equalsFn: (T, T) => Boolean,
    timeoutOverride: Int // Parameter name matches constructor usage
  ): EqualBy[T] =
    new EqualBy(
        name = name,
        toEvaluate = toEvaluate,
        expected = expected,
        equalsFn = equalsFn,
        mkString = (obj: T) => obj.toString, // Default mkString
        timeoutOverride = Some(timeoutOverride) // Specific timeout
    )
}
