package test.unit

/**
 * Represents the outcome of a single test execution.
 * This is a sealed trait, with specific case classes for Success and various Failure types.
 */
sealed trait TestResult:
  /** Indicates whether the test execution was successful. */
  def isSuccess: Boolean

  /**
   * Generates a human-readable message describing the test outcome.
   * Requires a `Config` implicitly to handle localization (language) and
   * styling (coloring via the logger).
   *
   * @param config The configuration context for formatting the message.
   * @return The formatted message string ready for display by a logger.
   */
  def message(using config: Config): String

/** Companion object for [[TestResult]]. Contains specific result types. */
object TestResult:

  /** Represents a successful test execution. */
  case class Success() extends TestResult:
    override def isSuccess: Boolean = true
    /** Generates the "TEST PASSED" message, localized and colored. */
    override def message(using config: Config): String =
      val logger = config.logger
      // Indent the message slightly for better alignment
      s"""
         |   ${logger.bold(logger.green(config.msg("passed")))}""".stripMargin 
  // lines if used alone

  /** Base trait for all test failure types. */
  sealed trait Failure extends TestResult:
    override def isSuccess: Boolean = false

    /** Helper to get the standard "TEST FAILED!" marker, localized and colored. */
    protected def failedMarker(using config: Config): String =
      val logger = config.logger
      logger.bold(logger.red(config.msg("failed")))

  /**
   * Failure: The evaluated result did not satisfy the expected property.
   * Used by [[Property]], [[Assert]], [[Refute]].
   *
   * @param result The actual result obtained from the evaluation.
   * @param mkString A function (provided by the test runner context) to convert the `result` to a String,
   *                 respecting the configuration (e.g., using localized keys for Assert/Refute).
   * @param propertyDescription The pre-formatted string (generated using Config during test execution)
   *                            describing the property that failed (e.g., "Does not verify property: should be true").
   * @tparam T The type of the result evaluated.
   */
  case class PropertyFailure[T](result: T, mkString: T => String, propertyDescription: String) extends Failure:
    /** Generates failure message including the property description and obtained result. */
    override def message(using config: Config): String =
      val logger = config.logger
      s"""
         |   $failedMarker
         |   $propertyDescription
         |   ${config.msg("obtained", logger.red(mkString(result)))}""".stripMargin // Format and color obtained
  // value

  /**
   * Failure: The actual result was not equal to the expected result.
   * Used by [[EqualBy]], [[Equal]].
   *
   * @param expected The expected result.
   * @param actual The actual result obtained.
   * @param mkString Function to convert expected and actual results to Strings.
   * @tparam T The type of the expected and actual results.
   */
  case class EqualityFailure[T](expected: T, actual: T, mkString: T => String) extends Failure:
    /** Generates failure message showing expected and obtained results. */
    override def message(using config: Config): String =
      val logger = config.logger
      s"""
         |   $failedMarker
         |   ${config.msg("expected", logger.green(mkString(expected)))}
         |   ${config.msg("obtained", logger.red(mkString(actual)))}""".stripMargin

  /**
   * Failure: An exception was expected, but none was thrown.
   * Used by [[ExceptionBy]] and subclasses.
   *
   * @param result The value that was returned instead of an exception.
   * @param mkString Function to convert the unexpected `result` to a String.
   * @param expectedExceptionDescription A description (already formatted using Config during execution)
   *                                     of the exception that was expected (e.g., "RuntimeException", "Any exception except NullPointerException").
   * @tparam T The type of the result that was unexpectedly returned.
   */
  case class NoExceptionFailure[T](result: T, mkString: T => String, expectedExceptionDescription: String) extends Failure:
    /** Generates failure message indicating no exception was thrown. */
    override def message(using config: Config): String =
      val logger = config.logger
      s"""
         |   $failedMarker
         |   ${config.msg("no.exception.basic", logger.green(expectedExceptionDescription))}
         |   ${config.msg("obtained", logger.red(mkString(result)))}""".stripMargin

  /**
   * Failure: An exception was thrown, but it was of the wrong type or did not satisfy the type predicate.
   * Used by [[ExceptionBy]] and subclasses.
   *
   * @param thrown The `Throwable` that was actually thrown.
   * @param expectedExceptionDescription A description (already formatted) of the exception criteria that were expected.
   */
  case class WrongExceptionTypeFailure(thrown: Throwable, expectedExceptionDescription: String) extends Failure:
    /** Generates failure message indicating the wrong exception type was thrown. */
    override def message(using config: Config): String =
      val logger = config.logger
      val thrownName = logger.red(thrown.getClass.getSimpleName) // Color the actual thrown type red
      s"""
         |   $failedMarker
         |   ${config.msg("wrong.exception.type.basic", thrownName)}
         |   ${config.msg("but.expected", logger.green(expectedExceptionDescription))}""".stripMargin // Color expected green

  /**
   * Failure: An exception of the expected type was thrown, but its message did not match
   * the expectation (either wrong exact message or failed message predicate).
   * Used by [[ExceptionBy]] and subclasses.
   *
   * @param thrown The `Throwable` that was thrown (correct type, wrong message).
   * @param expectedExceptionDescription A description (already formatted) of the exception criteria, including the expected message details.
   */
  case class WrongExceptionMessageFailure(thrown: Throwable, expectedExceptionDescription: String) extends Failure:
    /** Generates failure message indicating the message was wrong. */
    override def message(using config: Config): String =
      val logger = config.logger
      val thrownName = logger.green(thrown.getClass.getSimpleName) // Correct type is green
      val thrownMsg = Option(thrown.getMessage).getOrElse("null")
      val actualMsgStr = logger.red(s""""$thrownMsg"""") // Wrong message is red

      s"""
         |   $failedMarker
         |   ${config.msg("wrong.exception.message.basic", thrownName, actualMsgStr)}
         |   ${config.msg("but.expected", logger.green(expectedExceptionDescription))}""".stripMargin

  /**
   * Failure: An exception was thrown, but *both* its type and message were wrong.
   * Used by [[ExceptionBy]] and subclasses when both predicates fail.
   *
   * @param thrown The `Throwable` that was thrown (wrong type and message).
   * @param expectedExceptionDescription A description (already formatted) of the exception criteria (type and message).
   */
  case class WrongExceptionAndMessageFailure(thrown: Throwable, expectedExceptionDescription: String) extends Failure:
    /** Generates failure message indicating both type and message were wrong. */
    override def message(using config: Config): String =
      val logger = config.logger
      val thrownName = logger.red(thrown.getClass.getSimpleName) // Wrong type is red
      val thrownMsg = Option(thrown.getMessage).getOrElse("null")
      val actualMsgStr = logger.red(s""""$thrownMsg"""") // Wrong message is red

      s"""
         |   $failedMarker
         |   ${config.msg("wrong.exception.and.message.basic", thrownName, actualMsgStr)}
         |   ${config.msg("but.expected", logger.green(expectedExceptionDescription))}""".stripMargin

  /**
   * Failure: The test execution exceeded the allowed timeout duration.
   *
   * @param timeout The timeout duration in seconds that was exceeded.
   * @param expectedBehaviorDescription A description (already formatted) of what the test was expected to achieve
   *                                   (e.g., "Expected: 5", "Property: must be positive", "Exception: FooException").
   */
  case class TimeoutFailure(timeout: Int, expectedBehaviorDescription: String) extends Failure:
    /** Generates failure message indicating a timeout occurred. */
    override def message(using config: Config): String =
      // The 'timeout' message key already includes the description placeholder %1$s
      s"""
         |   $failedMarker
         |   ${config.msg("timeout", expectedBehaviorDescription, timeout)}""".stripMargin

  /**
   * Failure: An unexpected exception was thrown during test execution.
   * This applies to exceptions caught outside the core evaluation logic (e.g., in Future handling)
   * or exceptions thrown by tests not designed to catch them (like `Equal`, `Property`).
   *
   * @param thrown The unexpected `Throwable`.
   * @param originalExpectationDescription A description (already formatted) of what the test was *originally*
   *                                       trying to achieve before the unexpected exception occurred.
   */
  case class UnexpectedExceptionFailure(thrown: Throwable, originalExpectationDescription: String) extends Failure:
    /** Generates failure message reporting the unexpected exception. */
    override def message(using config: Config): String =
      val logger = config.logger
      val thrownName = logger.red(thrown.getClass.getSimpleName)
      val thrownMsg = Option(thrown.getMessage).getOrElse("null")
      val actualMsgStr = logger.red(s""""$thrownMsg"""")
      // The 'unexpected.exception' key includes placeholder %1$s for the original expectation
      s"""
         |   $failedMarker
         |   ${config.msg("unexpected.exception",
        originalExpectationDescription, // What was the test trying to do?
        thrownName,                     // What exception occurred?
        actualMsgStr)}""".stripMargin // What was its message?