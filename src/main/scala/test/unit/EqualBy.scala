package test.unit

import java.util.concurrent.{CompletableFuture, ExecutionException, TimeUnit, TimeoutException}

/**
 * A test verifying that an evaluated expression equals an expected value
 * according to a custom comparison function (`equalsFn`).
 *
 * @param name The name of the test.
 * @param toEvaluate The call-by-name expression to evaluate.
 * @param expected The expected value.
 * @param equalsFn The custom equality function `(T, T) => Boolean`.
 * @param mkString Function to convert the result `T` to a String for reporting.
 * @param timeoutOverride Optional specific timeout duration (in seconds) for this test.
 * @tparam T The type of the expression result and expected value.
 * @author Pepe Gallardo
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
   * Executes the core logic of the EqualBy test.
   * This method is called by `Test.run()` and receives the `Config` context.
   *
   * @param config The configuration context for this test run (used for logging, localization, timeout).
   * @return A `TestResult` indicating the outcome (Success, Failure, Timeout, etc.).
   */
  override protected def executeTest(using config: Config): TestResult = {
    val logger = config.logger // Get logger from the provided config

    // Helper to generate the "Expected: ..." part for failure messages, using the current config
    def expectedDescription(): String =
      config.msg("expected", logger.green(mkString(expected)))

    val future = CompletableFuture.supplyAsync(() => {
      try {
        val result = toEvaluate // Evaluate the expression
        if (equalsFn(result, expected)) {
          TestResult.Success() // Test passes
        } else {
          // Test fails: result does not equal expected via equalsFn
          TestResult.EqualityFailure(expected, result, mkString)
        }
      } catch {
        // Catch internal exceptions during evaluation
        case ie: InterruptedException =>
          Thread.currentThread().interrupt()
          TestResult.UnexpectedExceptionFailure(ie, expectedDescription())
        case t: Throwable =>
          TestResult.UnexpectedExceptionFailure(t, expectedDescription())
      }
    })

    try {
      // Wait for the future to complete with the resolved timeout
      future.get(config.timeout, TimeUnit.SECONDS)
    } catch {
      case _: TimeoutException =>
        TestResult.TimeoutFailure(config.timeout, expectedDescription())
      case e: ExecutionException =>
        val cause = Option(e.getCause).getOrElse(e)
        TestResult.UnexpectedExceptionFailure(cause, expectedDescription())
      case e: InterruptedException =>
        Thread.currentThread().interrupt()
        TestResult.UnexpectedExceptionFailure(e, expectedDescription())
      case e: java.lang.Exception => // Catch any other potential exceptions during future.get()
        TestResult.UnexpectedExceptionFailure(e, expectedDescription())
    }
  }
}

/**
 * Companion object for the [[EqualBy]] test class.
 * Provides factory `apply` methods for convenient test creation.
 */
object EqualBy {
  /**
   * Creates an EqualBy test with custom equality, optional mkString, and optional timeout.
   *
   * @param name Test name.
   * @param toEvaluate Expression to evaluate.
   * @param expected The expected value.
   * @param equalsFn Custom equality function `(T, T) => Boolean`.
   * @param mkString Function to convert `T` to String for reporting. Defaults to `_.toString`.
   * @param timeoutOverride Optional specific timeout in seconds.
   * @tparam T Type of the expression and result.
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
    new EqualBy(name, toEvaluate, expected, equalsFn, mkString, timeoutOverride)

  /**
   * Creates an EqualBy test with custom equality and optional timeout (uses default mkString).
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
    timeoutOverride: Option[Int]
  ): EqualBy[T] =
    new EqualBy(name, toEvaluate, expected, equalsFn, mkString = (obj: T) => obj.toString, timeoutOverride = timeoutOverride)

  /**
   * Creates an EqualBy test with custom equality and custom mkString (uses default timeout).
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
    new EqualBy(name, toEvaluate, expected, equalsFn, mkString, timeoutOverride = None)

  /**
   * Creates an EqualBy test with custom equality (uses default mkString and timeout).
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
    new EqualBy(name, toEvaluate, expected, equalsFn, mkString = (obj: T) => obj.toString, timeoutOverride = None)

  /**
   * Creates an EqualBy test with custom equality and a specific timeout (uses default mkString).
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
    timeoutOverride: Int
  ): EqualBy[T] =
    new EqualBy(name, toEvaluate, expected, equalsFn, mkString = (obj: T) => obj.toString, timeoutOverride = Some(timeoutOverride))
}
