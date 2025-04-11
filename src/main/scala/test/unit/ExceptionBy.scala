package test.unit

import java.util.concurrent.{CompletableFuture, ExecutionException, TimeUnit, TimeoutException}

/**
 * Abstract base class for tests that verify an exception is thrown when evaluating an expression.
 * It handles the common logic for asynchronous execution, timeout, exception catching,
 * and checking the type and message of the thrown exception against expectations.
 *
 * Subclasses must provide the specific logic for checking the exception type (`throwablePredicate`)
 * and define how the expectation description (`helpKey`, `helpArgs`) is constructed.
 *
 * @param name The descriptive name of the test case.
 * @param toEvaluate The call-by-name expression that is expected to throw an exception.
 * @param mkString A function to convert the result of `toEvaluate` (type `T`) to a string, used only if no exception is thrown (which is a failure). Defaults to `.toString`.
 * @param throwablePredicate A function `Throwable => Boolean` that returns `true` if the thrown exception's type is acceptable.
 * @param expectedMessage If `Some(msg)`, the thrown exception's message must equal `msg` exactly. Takes priority over `messagePredicate`.
 * @param messagePredicate If `expectedMessage` is `None`, this predicate is applied to the thrown exception's message. Defaults to always true.
 * @param predicateHelp If `messagePredicate` is used, this provides a human-readable description of the predicate for error messages.
 * @param helpKey The localization key (in [[I18n]]) for the main description of what kind of exception is expected (e.g., "exception.oneof.description").
 * @param helpArgs A sequence of [[HelpArg]] values used to format the `helpKey` message (e.g., type names, expected message).
 * @param timeoutOverride An optional duration in seconds to override the default test timeout.
 * @tparam T The return type of the `toEvaluate` block (if it were to complete normally, which indicates a test failure).
 * @author Pepe Gallardo & Gemini
 */
abstract class ExceptionBy[T](
  override val name: String,
  toEvaluate: => T,
  protected val mkString: T => String = (obj: T) => obj.toString,
  protected val throwablePredicate: Throwable => Boolean,
  protected val expectedMessage: Option[String],
  protected val messagePredicate: String => Boolean,
  protected val predicateHelp: Option[String],
  protected val helpKey: String,
  protected val helpArgs: Seq[HelpArg] = Seq.empty,
  override protected val timeoutOverride: Option[Int] = None
) extends Test {

  /**
   * Generates a formatted, localized string describing the overall expectation of this exception test.
   * It uses the `helpKey` and `helpArgs` provided to the constructor, applying appropriate
   * formatting (like coloring) to the arguments based on their [[HelpArg]] type.
   *
   * @param config The configuration context providing localization and logger.
   * @return The formatted help string describing the expected exception scenario.
   */
  protected def formattedHelp()(using config: Config): String = {
    val logger = config.logger
    val orConnector = config.msg("connector.or") // Localized " or "
    // Process each HelpArg, applying appropriate formatting/coloring
    val processedArgs: Seq[String] = helpArgs.map {
      case HelpArg.TypeName(name) => logger.green(name) // Green type name
      case HelpArg.TypeNameList(names) => names.map(logger.green).mkString(orConnector) // Green type names joined by " or "
      case HelpArg.ExactMessage(msg) => logger.green(s""""$msg"""") // Green quoted message
      case HelpArg.PredicateHelp(text) => logger.green(text) // Green predicate help text
    }
    // Format the localized message pattern (`helpKey`) with the processed arguments
    config.msg(helpKey, processedArgs*)
  }

  /**
   * Checks if the `actualMessage` matches the expectation.
   * If `expectedMessage` was defined (is `Some`), it performs an exact string comparison.
   * Otherwise, it applies the `messagePredicate`.
   *
   * @param actualMessage The message string from the thrown exception (or "null" if the message was null).
   * @return `true` if the message meets the expectation, `false` otherwise.
   */
  protected final def checkMessage(actualMessage: String): Boolean =
    expectedMessage match {
      case Some(exactMsg) => exactMsg == actualMessage // Check exact match
      case None           => messagePredicate(actualMessage) // Use predicate
    }


  /**
   * Executes the core logic of the exception test.
   * It evaluates the `toEvaluate` expression asynchronously.
   * - If no exception is thrown: Returns [[TestResult.NoExceptionFailure]].
   * - If an exception is thrown:
   *    - Checks if the exception type matches using `throwablePredicate`.
   *    - Checks if the exception message matches using `checkMessage`.
   *    - Returns [[TestResult.Success]] if both type and message match.
   *    - Returns specific failure results ([[TestResult.WrongExceptionTypeFailure]],
   *      [[TestResult.WrongExceptionMessageFailure]], [[TestResult.WrongExceptionAndMessageFailure]])
   *      depending on which checks failed. The message failure includes a detailed reason.
   * - If the evaluation times out or throws an *unexpected* exception (e.g., during setup):
   *    Returns [[TestResult.TimeoutFailure]] or [[TestResult.UnexpectedExceptionFailure]].
   *
   * @param config The configuration context for this test run, containing the resolved timeout.
   * @return A [[TestResult]] indicating the outcome.
   */
  override protected def executeTest(using config: Config): TestResult = {
    val logger = config.logger
    lazy val currentFormattedHelp = formattedHelp() // Generate overall expectation description once
    lazy val withExpectedCurrentFormattedHelp = config.msg("expected", currentFormattedHelp)

    val future = CompletableFuture.supplyAsync(() => {
      try {
        val result = toEvaluate // Evaluate the potentially exception-throwing code
        // If we reach here, no exception was thrown, which is a failure for this test type.
        TestResult.NoExceptionFailure(result = result, mkString = mkString, expectedExceptionDescription = currentFormattedHelp)
      } catch {
        case ie: InterruptedException =>
          // Evaluation was interrupted externally
          Thread.currentThread().interrupt()
          TestResult.UnexpectedExceptionFailure(thrown = ie, originalExpectationDescription = currentFormattedHelp)
        case thrown: Throwable =>
          // An exception was thrown, as expected (potentially). Now check type and message.
          val passedType = throwablePredicate(thrown) // Check if the type is acceptable
          val actualMessage = Option(thrown.getMessage).getOrElse("null") // Handle null messages
          val passedMessage = checkMessage(actualMessage) // Check if the message is acceptable

          if (passedType && passedMessage) {
             // Both type and message match expectations.
            TestResult.Success()
          } else if (!passedType) {
             // Type mismatch. Message may or may not match.
            if (!passedMessage) TestResult.WrongExceptionAndMessageFailure(thrown = thrown, expectedExceptionDescription = currentFormattedHelp)
            else TestResult.WrongExceptionTypeFailure(thrown = thrown, expectedExceptionDescription = currentFormattedHelp)
          } else { // Passed Type, Failed Message (!passedMessage must be true)
            // Type matched, but the message did not. Generate a detailed message failure reason.
            val detailMessage: String = expectedMessage match {
              case Some(exactMsg) => // We expected an exact message, but didn't get it.
                config.msg(
                  key = "detail.expected_exact_message",
                  args = logger.green(s""""$exactMsg"""") // Show the expected message (colored green)
                )
              case None => // We expected the message to satisfy a predicate, but it didn't.
                config.msg(
                  key = "detail.expected_predicate",
                  args = logger.green(predicateHelp.getOrElse("???")) // Show the predicate help text (colored green)
                )
            }
            // Return the failure result, including the specific detail about why the message failed.
            TestResult.WrongExceptionMessageFailure(thrown = thrown, expectedExceptionDescription = currentFormattedHelp, detailedExpectation = Some(detailMessage))
          }
      }
    })

    try {
      // Wait for the future to complete, respecting the timeout
      future.get(config.timeout, TimeUnit.SECONDS)
    } catch {
      // Handle issues during the waiting phase
      case _: TimeoutException =>
        future.cancel(true)
        TestResult.TimeoutFailure(timeout = config.timeout, expectedBehaviorDescription = withExpectedCurrentFormattedHelp)
      case e: ExecutionException =>
        val cause = Option(e.getCause).getOrElse(e)
        TestResult.UnexpectedExceptionFailure(thrown = cause, originalExpectationDescription = currentFormattedHelp)
      case e: InterruptedException =>
        Thread.currentThread().interrupt()
        TestResult.UnexpectedExceptionFailure(thrown = e, originalExpectationDescription = currentFormattedHelp)
      case e: java.util.concurrent.CancellationException =>
         TestResult.TimeoutFailure(timeout = config.timeout, expectedBehaviorDescription = withExpectedCurrentFormattedHelp)
      case e: java.lang.Exception =>
        TestResult.UnexpectedExceptionFailure(thrown = e, originalExpectationDescription = currentFormattedHelp)
    }
  }
}
