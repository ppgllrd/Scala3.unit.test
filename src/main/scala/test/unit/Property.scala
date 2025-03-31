package test.unit

import java.util.concurrent.{CompletableFuture, ExecutionException, TimeUnit, TimeoutException}

/**
 * A test verifying that an evaluated expression satisfies a given property (predicate function).
 * Optionally includes a help message describing the property being checked.
 *
 * @param name The name of the test.
 * @param toEvaluate The call-by-name expression to evaluate.
 * @param property The predicate function `T => Boolean` that the result must satisfy.
 * @param mkString Optional function to convert the result `T` to a String for reporting failures.
 * @param mkStringKey Optional function returning an I18n key to format the result `T`. Used if `mkString` is None.
 * @param help Optional plain text string describing the property being tested.
 * @param helpKey Optional I18n key used to generate the property description if `help` is None.
 * @param timeoutOverride Optional specific timeout duration (in seconds) for this test.
 * @tparam T The type of the expression result.
 * @author Pepe Gallardo
 */
class Property[T](
  override val name: String,
  toEvaluate: => T,
  property: T => Boolean,
  protected val mkString: Option[T => String] = None,
  protected val mkStringKey: Option[T => String] = None,
  protected val help: Option[String] = None,
  protected val helpKey: Option[String] = None,
  override protected val timeoutOverride: Option[Int] = None
) extends Test {

  /**
   * Executes the core logic of the Property test.
   * This method is called by `Test.run()` and receives the `Config` context.
   *
   * @param config The configuration context for this test run (used for logging, localization, timeout).
   * @return A `TestResult` indicating the outcome (Success, Failure, Timeout, etc.).
   */
  override protected def executeTest(using config: Config): TestResult = {
    val logger = config.logger // Get logger from the provided config

    // Helper to generate the description of the property being tested for failure messages
    def generatePropertyDescription(): String = {
      val helpDetail = help.orElse(helpKey.map(key => config.msg(key))) // Use literal help, fallback to localized key
                         .fold("")(desc => s": ${logger.green(desc)}") // Format as ": description"
      config.msg("property.failure", helpDetail) // Main message "Does not verify...%s"
    }

    // Helper to generate the string representation of the obtained result for failure messages
    def formatResult(result: T): String = {
      mkString.map(_.apply(result)) // Use direct mkString function if provided
        .orElse(mkStringKey.map(keyFn => config.msg(keyFn(result)))) // Fallback to localized key lookup
        .getOrElse(result.toString) // Ultimate fallback to default toString
    }

    val future = CompletableFuture.supplyAsync(() => {
      try {
        val result = toEvaluate // Evaluate the expression
        if (property(result)) {
          TestResult.Success() // Property holds, test passes
        } else {
          // Property does not hold, test fails
          TestResult.PropertyFailure(
            result = result,
            // Provide a function that formats the result using the *current* config
            mkString = r => formatResult(r),
            propertyDescription = generatePropertyDescription() // Generate description using current config
          )
        }
      } catch {
        // Catch internal exceptions during evaluation
        case ie: InterruptedException =>
          Thread.currentThread().interrupt()
          TestResult.UnexpectedExceptionFailure(ie, generatePropertyDescription())
        case t: Throwable =>
          TestResult.UnexpectedExceptionFailure(t, generatePropertyDescription())
      }
    })

    try {
      // Wait for the future to complete with the resolved timeout
      future.get(config.timeout, TimeUnit.SECONDS)
    } catch {
      case _: TimeoutException =>
        TestResult.TimeoutFailure(config.timeout, generatePropertyDescription())
      case e: ExecutionException =>
        val cause = Option(e.getCause).getOrElse(e)
        TestResult.UnexpectedExceptionFailure(cause, generatePropertyDescription())
      case e: InterruptedException =>
        Thread.currentThread().interrupt()
        TestResult.UnexpectedExceptionFailure(e, generatePropertyDescription())
      case e: java.lang.Exception => // Catch any other potential exceptions during future.get()
        TestResult.UnexpectedExceptionFailure(e, generatePropertyDescription())
    }
  }
}

/**
 * Companion object for the [[Property]] test class.
 * Provides factory `apply` methods for convenient test creation.
 */
object Property {
  /**
   * Base factory for creating `Property` tests. Allows specifying raw help string
   * or relying on default/key-based generation later.
   *
   * @param name Test name.
   * @param toEvaluate Expression to evaluate.
   * @param property Predicate the result must satisfy.
   * @param mkString Function to convert `T` to String for reporting. Defaults to `_.toString`.
   * @param help Optional plain text description of the property. If `None`, description generated later.
   * @param timeoutOverride Optional specific timeout in seconds.
   * @tparam T Type of the expression result.
   * @return A `Property[T]` test instance.
   */
  def apply[T](
    name: String,
    toEvaluate: => T,
    property: T => Boolean,
    mkString: T => String = (obj: T) => obj.toString,
    help: Option[String] = None,
    timeoutOverride: Option[Int] = None
  ): Property[T] =
    new Property( // Call constructor directly
      name = name,
      toEvaluate = toEvaluate,
      property = property,
      mkString = Some(mkString), // Wrap the provided function in Some
      mkStringKey = None, // Not using key-based mkString here
      help = help, // Pass the help option directly
      helpKey = None, // Not using key-based help here
      timeoutOverride = timeoutOverride
    )

  // --- Convenience Overloads --- (Call constructor directly)

  /** Creates test with a specific help string (defaults: mkString, timeout). */
  def apply[T](
    name: String,
    toEvaluate: => T,
    property: T => Boolean,
    help: String
  ): Property[T] =
    new Property( // Call constructor
      name = name,
      toEvaluate = toEvaluate,
      property = property,
      mkString = Some((obj: T) => obj.toString), // Default mkString wrapped
      mkStringKey = None,
      help = Some(help), // Wrap help string in Some
      helpKey = None,
      timeoutOverride = None
    )

  /** Creates test with only property predicate (defaults: mkString, help, timeout). */
  def apply[T](
    name: String,
    toEvaluate: => T,
    property: T => Boolean
  ): Property[T] =
    new Property( // Call constructor
      name = name,
      toEvaluate = toEvaluate,
      property = property,
      mkString = Some((obj: T) => obj.toString), // Default mkString wrapped
      mkStringKey = None,
      help = None, // No help specified
      helpKey = None,
      timeoutOverride = None
    )

  /** Creates test with custom mkString and help string (default: timeout). */
  def apply[T](
    name: String,
    toEvaluate: => T,
    property: T => Boolean,
    mkString: T => String,
    help: String
  ): Property[T] =
    new Property( // Call constructor
      name = name,
      toEvaluate = toEvaluate,
      property = property,
      mkString = Some(mkString), // Wrap mkString
      mkStringKey = None,
      help = Some(help), // Wrap help
      helpKey = None,
      timeoutOverride = None
    )

  /** Creates test with custom mkString (defaults: help, timeout). */
  def apply[T](
    name: String,
    toEvaluate: => T,
    property: T => Boolean,
    mkString: T => String
  ): Property[T] =
    new Property( // Call constructor
      name = name,
      toEvaluate = toEvaluate,
      property = property,
      mkString = Some(mkString), // Wrap mkString
      mkStringKey = None,
      help = None, // No help specified
      helpKey = None,
      timeoutOverride = None
    )

  /** Creates test with help string and specific timeout (default: mkString). */
  def apply[T](
    name: String,
    toEvaluate: => T,
    property: T => Boolean,
    help: String,
    timeoutOverride: Int
  ): Property[T] =
    new Property( // Call constructor
      name = name,
      toEvaluate = toEvaluate,
      property = property,
      mkString = Some((obj: T) => obj.toString), // Default mkString wrapped
      mkStringKey = None,
      help = Some(help), // Wrap help
      helpKey = None,
      timeoutOverride = Some(timeoutOverride) // Wrap timeout
    )

  /** Creates test with specific timeout (defaults: mkString, help). */
  def apply[T](
    name: String,
    toEvaluate: => T,
    property: T => Boolean,
    timeoutOverride: Int
  ): Property[T] =
    new Property( // Call constructor
      name = name,
      toEvaluate = toEvaluate,
      property = property,
      mkString = Some((obj: T) => obj.toString), // Default mkString wrapped
      mkStringKey = None,
      help = None, // No help specified
      helpKey = None,
      timeoutOverride = Some(timeoutOverride) // Wrap timeout
    )
}
