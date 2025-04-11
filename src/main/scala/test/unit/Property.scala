package test.unit

import java.util.concurrent.{CompletableFuture, ExecutionException, TimeUnit, TimeoutException}

/**
 * Represents a test that verifies if the result of an evaluated expression
 * satisfies a given predicate function (the "property").
 *
 * It supports providing a custom description of the property being tested,
 * either directly as a string (`help`) or via a localization key (`helpKey`).
 * Similarly, formatting the evaluated result (on failure) can be customized
 * using `mkString` or `mkStringKey`.
 *
 * This class handles the asynchronous execution, timeout, and exception handling.
 *
 * @param name The descriptive name of the test case.
 * @param toEvaluate The call-by-name expression whose result will be evaluated and checked against the `property`.
 * @param property The predicate function `T => Boolean`. The test passes if `property(result)` is `true`.
 * @param mkString An optional function `T => String` to customize the string representation of the evaluated `result` in failure messages. If `None`, `mkStringKey` is checked.
 * @param mkStringKey An optional function `T => String` that returns a localization key (from [[I18n]]). The result associated with this key (potentially formatted with the result itself) is used as the string representation in failure messages. Used only if `mkString` is `None`. If both are `None`, `result.toString` is used.
 * @param help An optional plain text string describing the property being tested. Used in failure messages. Takes priority over `helpKey`.
 * @param helpKey An optional localization key (from [[I18n]]) used to generate the description of the property being tested if `help` is `None`.
 * @param timeoutOverride An optional duration in seconds to override the default test timeout.
 * @tparam T The type of the value produced by `toEvaluate`.
 * @author Pepe Gallardo & Gemini
 */
class Property[T](
  override val name: String,
  toEvaluate: => T,
  property: T => Boolean,
  protected val mkString: Option[T => String],
  protected val mkStringKey: Option[T => String],
  protected val help: Option[String],
  protected val helpKey: Option[String],
  override protected val timeoutOverride: Option[Int] = None
) extends Test {

  /**
   * Generates the description of the property being tested, used in failure messages.
   * It prioritizes the direct `help` string if provided, otherwise uses the `helpKey`
   * to look up a localized string. The description is formatted and potentially colored.
   *
   * @param config The configuration context providing localization and logger.
   * @return A formatted string describing the property expectation (e.g., "Does not verify expected property: [green]value must be positive[reset]").
   */
  private def generatePropertyDescription()(using config: Config): String = {
    val logger = config.logger
    // Base failure message (e.g., "Does not verify expected property")
    val baseMessage = config.msg("property.failure.base")

    // Determine the help detail text, preferring direct 'help' over 'helpKey'
    val helpDetailOpt: Option[String] = help // Use direct help string if available
      .orElse(helpKey.map(key => config.msg(key))) // Otherwise, use helpKey to get localized string
      .map(logger.green) // Apply green color to the detail text

    // Combine base message and the optional, colored help detail
    helpDetailOpt match {
      // If detail exists, append it using the localized suffix pattern (e.g., ": %s")
      case Some(detail) => baseMessage + config.msg("property.failure.suffix", detail)
      // If no detail, just use the base message
      case None => baseMessage
    }
  }

  /**
   * Formats the evaluated result `T` into a string for reporting in failure messages.
   * It prioritizes `mkString` if provided, then `mkStringKey`, falling back to `result.toString`.
   *
   * @param result The value of type `T` that was evaluated.
   * @param config The configuration context providing localization.
   * @return The formatted string representation of the result.
   */
  private def formatResult(result: T)(using config: Config): String = {
    mkString.map(_.apply(result)) // 1. Try direct mkString function
      .orElse(mkStringKey.map(keyFn => config.msg(keyFn(result)))) // 2. Try mkStringKey function to get I18n key
      .getOrElse(result.toString) // 3. Fallback to standard toString
  }

  /**
   * Executes the core logic of the `Property` test.
   * It evaluates the `toEvaluate` expression asynchronously.
   * If the evaluation completes successfully within the timeout:
   *  - It applies the `property` predicate to the result.
   *  - Returns [[TestResult.Success]] if the property holds (`true`).
   *  - Returns [[TestResult.PropertyFailure]] otherwise (`false`), including the formatted result and property description.
   * If the evaluation times out or throws an unexpected exception:
   *  - Returns [[TestResult.TimeoutFailure]] or [[TestResult.UnexpectedExceptionFailure]] respectively.
   *
   * @param config The configuration context for this test run, containing the resolved timeout.
   * @return A [[TestResult]] indicating the outcome.
   */
  override protected def executeTest(using config: Config): TestResult = {
    lazy val currentPropertyDesc = generatePropertyDescription() // Generate once before async

    val future = CompletableFuture.supplyAsync(() => {
      try {
        val result = toEvaluate // Evaluate the expression
        if (property(result)) {
           // Property holds true
          TestResult.Success()
        } else {
          // Property failed
          TestResult.PropertyFailure(
            result = result,
            mkString = r => formatResult(r)(using config), // Pass lambda that uses formatResult
            propertyDescription = currentPropertyDesc
          )
        }
      } catch {
        // Handle potential exceptions during evaluation
        case ie: InterruptedException =>
          Thread.currentThread().interrupt()
          TestResult.UnexpectedExceptionFailure(thrown = ie, originalExpectationDescription = currentPropertyDesc)
        case t: Throwable =>
          TestResult.UnexpectedExceptionFailure(thrown = t, originalExpectationDescription = currentPropertyDesc)
      }
    })

    try {
      // Wait for the future to complete, respecting the timeout
      future.get(config.timeout, TimeUnit.SECONDS)
    } catch {
      // Handle issues during the waiting phase
      case _: TimeoutException =>
        future.cancel(true)
        TestResult.TimeoutFailure(timeout = config.timeout, expectedBehaviorDescription = currentPropertyDesc)
      case e: ExecutionException =>
        val cause = Option(e.getCause).getOrElse(e)
        TestResult.UnexpectedExceptionFailure(thrown = cause, originalExpectationDescription = currentPropertyDesc)
      case e: InterruptedException =>
        Thread.currentThread().interrupt()
        TestResult.UnexpectedExceptionFailure(thrown = e, originalExpectationDescription = currentPropertyDesc)
      case e: java.util.concurrent.CancellationException =>
        TestResult.TimeoutFailure(timeout = config.timeout, expectedBehaviorDescription = currentPropertyDesc)
      case e: java.lang.Exception =>
        TestResult.UnexpectedExceptionFailure(thrown = e, originalExpectationDescription = currentPropertyDesc)
    }
  }
}

/**
 * Companion object for the [[Property]] class.
 * Provides factory `apply` methods for convenient creation of `Property` tests.
 * These methods handle the optional parameters and delegate to a private `create`
 * method which ensures correct defaults and calls the primary constructor.
 *
 * @author Pepe Gallardo & Gemini
 */
object Property {

  /**
   * Private helper method to create a Property instance.
   * It centralizes the logic for handling default values (especially for `mkString`)
   * and ensures all Option types are correctly passed to the primary constructor.
   */
  private def create[T](
    name: String,
    toEvaluate: => T,
    property: T => Boolean,
    mkStringOpt: Option[T => String] = None,
    mkStringKeyOpt: Option[T => String] = None,
    helpTextOpt: Option[String] = None,
    helpKeyOpt: Option[String] = None,
    timeoutOverride: Option[Int] = None
  ): Property[T] = {
    // Ensure a default mkString (using toString) is used if neither mkStringOpt nor mkStringKeyOpt is provided.
    val finalMkStringOpt = mkStringOpt.orElse(if (mkStringKeyOpt.isEmpty) Some((obj: T) => obj.toString) else None)

    // Call the primary constructor with resolved Option values
    new Property(
      name = name,
      toEvaluate = toEvaluate,
      property = property,
      mkString = finalMkStringOpt, // Use the resolved mkString option
      mkStringKey = mkStringKeyOpt, // Pass the key option directly
      help = helpTextOpt,           // Pass the help text option directly
      helpKey = helpKeyOpt,         // Pass the help key option directly
      timeoutOverride = timeoutOverride
    )
  }

  // --- Public Convenience Overloads using the private create helper ---

  /**
   * Creates a `Property` test using a custom predicate. Allows specifying an optional
   * custom `mkString` function for formatting results on failure, an optional `help` string
   * describing the property, and an optional timeout override.
   *
   * @param name Test name.
   * @param toEvaluate Expression to evaluate.
   * @param property The predicate `T => Boolean` the result must satisfy.
   * @param mkString Function to convert result `T` to String. Defaults to `.toString` if no help key is used.
   * @param help Optional string describing the property for failure messages.
   * @param timeoutOverride Optional specific timeout in seconds.
   * @tparam T Type of the expression and result.
   * @return A `Property[T]` test instance.
   */
  def apply[T](
    name: String,
    toEvaluate: => T,
    property: T => Boolean,
    mkString: T => String = (obj: T) => obj.toString, // Default mkString function
    help: Option[String] = None,                      // Help text as Option
    timeoutOverride: Option[Int] = None
  ): Property[T] =
    create( // Call private helper
      name = name,
      toEvaluate = toEvaluate,
      property = property,
      mkStringOpt = Some(mkString), // Wrap provided function in Some
      helpTextOpt = help,           // Pass Option directly
      timeoutOverride = timeoutOverride
    )

  /**
   * Creates a `Property` test with a specific help string describing the property.
   * Uses the default `mkString` function (`.toString`).
   *
   * @param name Test name.
   * @param toEvaluate Expression to evaluate.
   * @param property The predicate `T => Boolean` the result must satisfy.
   * @param help A string describing the property for failure messages.
   * @tparam T Type of the expression and result.
   * @return A `Property[T]` test instance.
   */
  def apply[T](
    name: String,
    toEvaluate: => T,
    property: T => Boolean,
    help: String // Accepts help string directly
  ): Property[T] =
    create( // Call private helper
      name = name,
      toEvaluate = toEvaluate,
      property = property,
      helpTextOpt = Some(help) // Wrap provided help string in Some
    )

  /**
   * Creates a `Property` test with only the essential arguments: name, expression, and property predicate.
   * Uses default formatting (`.toString`) and provides no specific help message on failure.
   *
   * @param name Test name.
   * @param toEvaluate Expression to evaluate.
   * @param property The predicate `T => Boolean` the result must satisfy.
   * @tparam T Type of the expression and result.
   * @return A `Property[T]` test instance.
   */
  def apply[T](
    name: String,
    toEvaluate: => T,
    property: T => Boolean
  ): Property[T] =
    create( // Call private helper with minimal arguments
      name = name,
      toEvaluate = toEvaluate,
      property = property
    )

  /**
   * Creates a `Property` test with a custom `mkString` function and a specific help string.
   *
   * @param name Test name.
   * @param toEvaluate Expression to evaluate.
   * @param property The predicate `T => Boolean` the result must satisfy.
   * @param mkString Custom function to convert result `T` to String.
   * @param help A string describing the property for failure messages.
   * @tparam T Type of the expression and result.
   * @return A `Property[T]` test instance.
   */
  def apply[T](
    name: String,
    toEvaluate: => T,
    property: T => Boolean,
    mkString: T => String, // Accepts mkString function directly
    help: String           // Accepts help string directly
  ): Property[T] =
    create( // Call private helper
      name = name,
      toEvaluate = toEvaluate,
      property = property,
      mkStringOpt = Some(mkString), // Wrap function in Some
      helpTextOpt = Some(help)      // Wrap string in Some
    )

  /**
   * Creates a `Property` test with a custom `mkString` function but no specific help string.
   *
   * @param name Test name.
   * @param toEvaluate Expression to evaluate.
   * @param property The predicate `T => Boolean` the result must satisfy.
   * @param mkString Custom function to convert result `T` to String.
   * @tparam T Type of the expression and result.
   * @return A `Property[T]` test instance.
   */
  def apply[T](
    name: String,
    toEvaluate: => T,
    property: T => Boolean,
    mkString: T => String // Accepts mkString function directly
  ): Property[T] =
    create( // Call private helper
      name = name,
      toEvaluate = toEvaluate,
      property = property,
      mkStringOpt = Some(mkString) // Wrap function in Some
    )

  /**
   * Creates a `Property` test with a specific help string and a specific timeout override.
   *
   * @param name Test name.
   * @param toEvaluate Expression to evaluate.
   * @param property The predicate `T => Boolean` the result must satisfy.
   * @param help A string describing the property for failure messages.
   * @param timeoutOverride Specific timeout duration in seconds.
   * @tparam T Type of the expression and result.
   * @return A `Property[T]` test instance.
   */
  def apply[T](
    name: String,
    toEvaluate: => T,
    property: T => Boolean,
    help: String,           // Accepts help string directly
    timeoutOverride: Int    // Accepts timeout directly
  ): Property[T] =
    create( // Call private helper
      name = name,
      toEvaluate = toEvaluate,
      property = property,
      helpTextOpt = Some(help),          // Wrap help string in Some
      timeoutOverride = Some(timeoutOverride) // Wrap timeout in Some
    )

  /**
   * Creates a `Property` test with a specific timeout override only.
   * Uses default formatting and no specific help message.
   *
   * @param name Test name.
   * @param toEvaluate Expression to evaluate.
   * @param property The predicate `T => Boolean` the result must satisfy.
   * @param timeoutOverride Specific timeout duration in seconds.
   * @tparam T Type of the expression and result.
   * @return A `Property[T]` test instance.
   */
  def apply[T](
    name: String,
    toEvaluate: => T,
    property: T => Boolean,
    timeoutOverride: Int // Accepts timeout directly
  ): Property[T] =
    create( // Call private helper
      name = name,
      toEvaluate = toEvaluate,
      property = property,
      timeoutOverride = Some(timeoutOverride) // Wrap timeout in Some
    )

  /**
   * Internal factory method used by [[Assert]] and [[Refute]].
   * Creates a `Property` test where the result formatting and help message
   * are specified using localization keys instead of direct functions/strings.
   *
   * @param name Test name.
   * @param toEvaluate Expression to evaluate.
   * @param property The predicate `T => Boolean` the result must satisfy.
   * @param mkStringKey Function `T => String` returning the I18n key for result formatting.
   * @param helpKey The I18n key for the property description.
   * @param timeoutOverride Optional specific timeout in seconds.
   * @tparam T Type of the expression and result.
   * @return A `Property[T]` test instance configured with key-based formatting/help.
   */
  private[unit] def fromKeyBased[T](
    name: String,
    toEvaluate: => T,
    property: T => Boolean,
    mkStringKey: T => String,
    helpKey: String,
    timeoutOverride: Option[Int]
  ): Property[T] =
    create( // Call private helper
      name = name,
      toEvaluate = toEvaluate,
      property = property,
      mkStringKeyOpt = Some(mkStringKey), // Pass key function wrapped in Some
      helpKeyOpt = Some(helpKey),         // Pass key string wrapped in Some
      timeoutOverride = timeoutOverride
    )

}
