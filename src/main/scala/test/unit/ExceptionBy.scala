package test.unit

import java.util.concurrent.{CompletableFuture, ExecutionException, TimeUnit, TimeoutException}

/**
 * Base class for tests expecting an exception satisfying certain criteria.
 * It stores predicates for the exception type and message, along with
 * keys and arguments for generating a description of the expected exception
 * during test execution.
 *
 * @param name Test name.
 * @param toEvaluate The call-by-name expression expected to throw an exception.
 * @param mkString Function to convert `T` (the unexpected result) to String if no exception is thrown.
 * @param throwablePredicate Predicate `Throwable => Boolean` to check the type/properties of the thrown exception.
 * @param messagePredicate Predicate `String => Boolean` to check the message of the thrown exception.
 * @param helpKey The I18n key for the *description* of the expected exception (e.g., "exception.description").
 * @param helpArgs Arguments to be formatted into the `helpKey` message pattern during execution.
 * @param timeoutOverride Optional specific timeout duration (in seconds) for this test.
 * @tparam T The type of the expression result (if it didn't throw).
 * @author Pepe Gallardo
 */
abstract class ExceptionBy[T](
  override val name: String,
  toEvaluate: => T,
  protected val mkString: T => String = (obj: T) => obj.toString,
  protected val throwablePredicate: Throwable => Boolean,
  protected val messagePredicate: String => Boolean,
  protected val helpKey: String,
  protected val helpArgs: Seq[Any] = Seq.empty,
  override protected val timeoutOverride: Option[Int] = None
) extends Test {

  /**
   * Executes the core logic of the ExceptionBy test.
   * This method is called by `Test.run()` and receives the `Config` context.
   *
   * @param config The configuration context for this test run (used for logging, localization, timeout).
   * @return A `TestResult` indicating the outcome (Success, Failure, Timeout, etc.).
   */
  override protected def executeTest(using config: Config): TestResult = {
    val logger = config.logger // Get logger from the provided config

    // Helper to format the description of the expected exception using the current Config
    def formattedHelp(): String = {
      // Color arguments representing types/messages before formatting
      val processedArgs = helpArgs.map {
         case s: String if s.matches("^[A-Z]\\w*$") => logger.green(s) // Simple Class Name
         case s: String if s.startsWith("\"") && s.endsWith("\"") => logger.green(s) // Quoted message
         case other => other
      }
      config.msg(helpKey, processedArgs*)
    }

    val future = CompletableFuture.supplyAsync(() => {
      try {
        val result = toEvaluate // Evaluate the expression
        // If we get here, no exception was thrown - Failure
        TestResult.NoExceptionFailure(result, mkString, formattedHelp())
      } catch {
        // Catch exceptions thrown during evaluation
        case ie: InterruptedException =>
          Thread.currentThread().interrupt()
          TestResult.UnexpectedExceptionFailure(ie, formattedHelp())
        case thrown: Throwable =>
          // An exception was thrown, check if it matches expectations
          val passedThrowable = throwablePredicate(thrown)
          val message = Option(thrown.getMessage).getOrElse("") // Handle null message safely
          val passedMessage = messagePredicate(message)

          if (passedThrowable && passedMessage) {
            TestResult.Success() // Correct exception and message (if required)
          } else if (!passedThrowable && !passedMessage) {
            TestResult.WrongExceptionAndMessageFailure(thrown, formattedHelp())
          } else if (!passedThrowable) {
            TestResult.WrongExceptionTypeFailure(thrown, formattedHelp())
          } else {
            TestResult.WrongExceptionMessageFailure(thrown, formattedHelp())
          }
      }
    })

    try {
      // Wait for the future to complete with the resolved timeout
      future.get(config.timeout, TimeUnit.SECONDS)
    } catch {
      case _: TimeoutException =>
        TestResult.TimeoutFailure(config.timeout, formattedHelp())
      case e: ExecutionException =>
        val cause = Option(e.getCause).getOrElse(e)
        TestResult.UnexpectedExceptionFailure(cause, formattedHelp())
      case e: InterruptedException =>
        Thread.currentThread().interrupt()
        TestResult.UnexpectedExceptionFailure(e, formattedHelp())
      case e: java.lang.Exception => // Catch any other potential exceptions during future.get()
        TestResult.UnexpectedExceptionFailure(e, formattedHelp())
    }
  }
}
