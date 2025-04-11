package test.unit

/**
 * Represents the possible outcomes of executing a [[Test]].
 * This is a sealed trait with case classes/objects for specific results
 * like success, various types of failures (equality, property, exception-related, timeout), etc.
 *
 * The `message` method provides a localized and potentially colored description
 * of the outcome, requiring an implicit [[Config]] for formatting.
 *
 * @author Pepe Gallardo & Gemini
 */
sealed trait TestResult:
  /** Indicates whether this result represents a successful test execution. */
  def isSuccess: Boolean

  /**
   * Generates a formatted message describing the test outcome.
   * Uses the implicit `config` for localization (via `config.msg`) and
   * coloring (via `config.logger`).
   *
   * @param config The implicit configuration context.
   * @return A descriptive string message for this test result.
   */
  def message(using config: Config): String

/**
 * Companion object for [[TestResult]]. Contains the specific case classes/objects
 * representing the different possible outcomes of a test.
 *
 * @author Pepe Gallardo & Gemini
 */
object TestResult:

  private def lowerCapitalize(str: String): String =
    str.headOption.map(_.toLower + str.tail).getOrElse(str)

  /** Represents a successful test execution. */
  case class Success() extends TestResult:
    override def isSuccess: Boolean = true
    /** Returns a simple success message (e.g., "TEST PASSED SUCCESSFULLY!"). */
    override def message(using config: Config): String =
      val logger = config.logger // For coloring
      // Indented, bold, green "PASSED" message from I18n
      s"""
         |   ${logger.bold(logger.green(config.msg("passed")))}""".stripMargin

  /** Base trait for all failure results. */
  sealed trait Failure extends TestResult:
    override def isSuccess: Boolean = false
    /** Helper to get the standard "FAILED!" marker, localized and colored red/bold. */
    protected def failedMarker(using config: Config): String =
      config.logger.bold(config.logger.red(config.msg("failed")))

  /** Failure because a property predicate returned `false`. */
  case class PropertyFailure[T](
    result: T, // The actual result obtained from evaluation
    mkString: T => String, // Function to format the result T to String
    propertyDescription: String // Pre-formatted description of the expected property
  ) extends Failure:
    /** Formats a message including the failure marker, property description, and the obtained result. */
    override def message(using config: Config): String =
      val logger = config.logger
      // Format the obtained result (colored red) using the provided mkString function
      val obtainedMsg = config.msg("obtained.result", logger.red(mkString(result)))
      s"""
         |   $failedMarker
         |   $propertyDescription
         |   $obtainedMsg""".stripMargin

  /** Failure because the actual result was not equal to the expected value (using `==` or custom `equalsFn`). */
  case class EqualityFailure[T](
    expected: T, // The expected value
    actual: T, // The actual result obtained
    mkString: T => String // Function to format T values to String
  ) extends Failure:
     /** Formats a message showing the failure marker, expected value (green), and obtained value (red). */
    override def message(using config: Config): String =
      val logger = config.logger
      // Format expected (green) and actual (red) values
      val expectedMsg = config.msg("expected.result", logger.green(mkString(expected)))
      val obtainedMsg = config.msg("obtained.result", logger.red(mkString(actual)))
      s"""
         |   $failedMarker
         |   $expectedMsg
         |   $obtainedMsg""".stripMargin

  /** Failure because an exception was expected, but none was thrown. */
  case class NoExceptionFailure[T](
    result: T, // The value returned instead of an exception
    mkString: T => String, // Function to format the result T to String
    expectedExceptionDescription: String // Pre-formatted description of the expected exception scenario
  ) extends Failure:
    /** Formats a message indicating no exception was thrown, including the expected scenario and the obtained result. */
    override def message(using config: Config): String =
      val logger = config.logger
      // Format the obtained result (red)
      val obtainedMsg = config.msg("obtained.result", logger.red(mkString(result)))
      // Get the base "no exception" message, including the expected description
      val baseMsg = config.msg("no.exception.basic", expectedExceptionDescription)
      s"""
         |   $failedMarker
         |   $baseMsg
         |   $obtainedMsg""".stripMargin

  /** Failure because an exception was thrown, but it was of the wrong type. */
  case class WrongExceptionTypeFailure(
    thrown: Throwable, // The actual exception that was thrown
    expectedExceptionDescription: String // Pre-formatted description of the expected exception scenario
  ) extends Failure:
     /** Formats a message showing the actual exception type (red) and the expected scenario. */
    override def message(using config: Config): String =
      val logger = config.logger
      // Get actual exception type name (red)
      val thrownName = logger.red(thrown.getClass.getSimpleName)
      // Basic message indicating wrong type thrown
      val wrongTypeMsg = config.msg("wrong.exception.type.basic", thrownName)
      // Message indicating what was expected instead
      val butExpectedMsg = config.msg("but.expected", lowerCapitalize(expectedExceptionDescription))
      s"""
         |   $failedMarker
         |   $wrongTypeMsg
         |   $butExpectedMsg""".stripMargin

  /** Failure because the correct type of exception was thrown, but its message did not match expectations. */
  case class WrongExceptionMessageFailure(
    thrown: Throwable, // The actual exception (correct type, wrong message)
    expectedExceptionDescription: String, // Pre-formatted overall description of the expected scenario (includes type)
    detailedExpectation: Option[String] // Pre-formatted detail about *why* the message failed (e.g., expected exact "X", or predicate "Y")
  ) extends Failure:
    /** Formats a message showing the thrown exception type (green), the actual message (red), and the detailed expectation for the message. */
    override def message(using config: Config): String =
      val logger = config.logger
      // Get thrown type name (green, because type was correct)
      val thrownName = logger.green(thrown.getClass.getSimpleName)
      // Get actual message, handle null, format as red quoted string
      val thrownMsg = Option(thrown.getMessage).getOrElse("null")
      val actualMsgStr = logger.red(s""""$thrownMsg"""")
      // Basic message indicating correct type but wrong message
      val wrongMsgBasic = config.msg("wrong.exception.message.basic", thrownName, actualMsgStr)
      // Get the specific reason for message failure, or a fallback if not provided
      val detailPart = detailedExpectation.getOrElse("(Reason for message failure not specified)")
      s"""
         |   $failedMarker
         |   $wrongMsgBasic
         |   $detailPart""".stripMargin // Appends the detailed reason

  /** Failure because both the type and the message of the thrown exception were incorrect. */
  case class WrongExceptionAndMessageFailure(
    thrown: Throwable, // The actual exception (wrong type and message)
    expectedExceptionDescription: String // Pre-formatted description of the expected scenario
  ) extends Failure:
     /** Formats a message showing the actual exception type and message (both red) and the expected scenario. */
    override def message(using config: Config): String =
      val logger = config.logger
       // Get actual type name (red)
      val thrownName = logger.red(thrown.getClass.getSimpleName)
      // Get actual message, handle null, format as red quoted string
      val thrownMsg = Option(thrown.getMessage).getOrElse("null")
      val actualMsgStr = logger.red(s""""$thrownMsg"""")
      // Basic message indicating wrong type and message thrown
      val wrongAllMsg = config.msg("wrong.exception.and.message.basic", thrownName, actualMsgStr)
       // Message indicating what was expected instead
      val butExpectedMsg = config.msg("but.expected", lowerCapitalize(expectedExceptionDescription))
      s"""
         |   $failedMarker
         |   $wrongAllMsg
         |   $butExpectedMsg""".stripMargin

  /** Failure because the test execution exceeded the allowed time limit. */
  case class TimeoutFailure(
    timeout: Int, // The timeout duration in seconds that was exceeded
    expectedBehaviorDescription: String // Pre-formatted description of what the test was expected to do
  ) extends Failure:
    /**
     * Formats a timeout failure message.
     * It uses the I18n key "timeout" which expects the description and the duration.
     * Example: "[FAILED!] [Expected description] was expected\n   Timeout: test took more than [timeout] seconds to complete"
     */
    override def message(using config: Config): String =
      // Use the specific I18n key "timeout", passing the expected behavior and duration
      val timeoutMsg = config.msg("timeout", expectedBehaviorDescription, timeout)
      s"""
         |   $failedMarker
         |   $timeoutMsg""".stripMargin // Combine marker and formatted timeout message

  /** Failure due to an unexpected exception occurring during test execution (not the one being tested for, if any). */
  case class UnexpectedExceptionFailure(
    thrown: Throwable, // The unexpected exception that was caught
    originalExpectationDescription: String // Pre-formatted description of what the test was originally trying to achieve
  ) extends Failure:
    /** Formats a message showing the failure marker, the original expectation, and details of the unexpected exception (type and message, both red). */
    override def message(using config: Config): String =
      val logger = config.logger
      // Get unexpected exception type name (red)
      val thrownName = logger.red(thrown.getClass.getSimpleName)
      // Get unexpected exception message, handle null, format as red quoted string
      val thrownMsg = Option(thrown.getMessage).getOrElse("null")
      val actualMsgStr = logger.red(s""""$thrownMsg"""")
      // Construct the message using the "unexpected.exception" key
      val unexpectedMsg = config.msg("unexpected.exception", originalExpectationDescription, thrownName, actualMsgStr)
      s"""
         |   $failedMarker
         |   $unexpectedMsg""".stripMargin
