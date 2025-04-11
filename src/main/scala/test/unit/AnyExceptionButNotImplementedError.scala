package test.unit

import scala.reflect.ClassTag
import scala.annotation.targetName

/**
 * Factory object for creating tests that expect any `Throwable` to be thrown,
 * *except* for `NotImplementedError`. It allows specifying conditions
 * on the thrown exception's message (exact match or predicate).
 *
 * This object delegates the actual test creation to [[ExceptionExcept]].
 *
 * @author Pepe Gallardo & Gemini
 */
object AnyExceptionButNotImplementedError {

  /**
   * Creates a test that expects any `Throwable` except `NotImplementedError`.
   * This is the most general factory method, accepting all configuration options.
   * It delegates directly to the `ExceptionExcept` factory.
   *
   * @param name The name of the test.
   * @param toEvaluate The code block that is expected to throw an exception.
   * @param mkString A function to convert the result (if no exception is thrown) to a string for reporting. Defaults to `.toString`.
   * @param expectedMessage If `Some(msg)`, the thrown exception's message must match `msg` exactly. Takes priority over `messagePredicate`.
   * @param messagePredicate If `expectedMessage` is `None`, this predicate is applied to the thrown exception's message. Defaults to always true.
   * @param predicateHelp If `messagePredicate` is used, this provides a human-readable description of the predicate for error messages.
   * @param timeoutOverride An optional duration in seconds to override the default test timeout.
   * @tparam T The return type of the `toEvaluate` block (if it were to complete normally).
   * @return An [[ExceptionExcept]][T, NotImplementedError] test instance configured as specified.
   */
  def apply[T](
    name: String,
    toEvaluate: => T,
    mkString: T => String = (obj: T) => obj.toString,
    expectedMessage: Option[String] = None,
    messagePredicate: String => Boolean = (_: String) => true,
    predicateHelp: Option[String] = None,
    timeoutOverride: Option[Int] = None
  ): ExceptionExcept[T, NotImplementedError] =
    // Delegate directly to ExceptionExcept's base factory
    ExceptionExcept[T, NotImplementedError](
      name = name,
      toEvaluate = toEvaluate,
      mkString = mkString,
      expectedMessage = expectedMessage,
      messagePredicate = messagePredicate,
      predicateHelp = predicateHelp,
      timeoutOverride = timeoutOverride
    )

  // --- Convenience Overloads ---

  /**
   * Creates a test expecting any `Throwable` except `NotImplementedError`,
   * requiring the exception message to match the `expectedMessage` exactly.
   *
   * @param name The name of the test.
   * @param toEvaluate The code block expected to throw an exception.
   * @param expectedMessage The exact string the thrown exception's message must equal.
   * @tparam T The return type of the `toEvaluate` block.
   * @return An [[ExceptionExcept]][T, NotImplementedError] test instance.
   */
  @targetName("applyWithExactMessage")
  def apply[T](
    name: String,
    toEvaluate: => T,
    expectedMessage: String // Exact message overload
  ): ExceptionExcept[T, NotImplementedError] =
    ExceptionExcept[T, NotImplementedError](
      name = name,
      toEvaluate = toEvaluate,
      mkString = (obj: T) => obj.toString, // Default
      expectedMessage = Some(expectedMessage), // Set exact message
      messagePredicate = (_: String) => true, // Default (ignored)
      predicateHelp = None, // Default (ignored)
      timeoutOverride = None // Default
    )

  /**
   * Creates a test expecting any `Throwable` except `NotImplementedError`,
   * requiring the exception message to satisfy the given `messagePredicate`.
   * A help string describing the predicate is required.
   *
   * @param name The name of the test.
   * @param toEvaluate The code block expected to throw an exception.
   * @param messagePredicate A function that takes the exception message and returns `true` if it is acceptable.
   * @param predicateHelp A human-readable description of the predicate for error messages.
   * @tparam T The return type of the `toEvaluate` block.
   * @return An [[ExceptionExcept]][T, NotImplementedError] test instance.
   */
  @targetName("applyWithPredicateAndHelp")
  def apply[T](
    name: String,
    toEvaluate: => T,
    messagePredicate: String => Boolean, // Predicate overload
    predicateHelp: String               // Help text REQUIRED
  ): ExceptionExcept[T, NotImplementedError] =
    ExceptionExcept[T, NotImplementedError](
      name = name,
      toEvaluate = toEvaluate,
      mkString = (obj: T) => obj.toString, // Default
      expectedMessage = None, // No exact message
      messagePredicate = messagePredicate, // Use provided predicate
      predicateHelp = Some(predicateHelp), // Use provided help
      timeoutOverride = None // Default
    )

  /**
   * Creates a test expecting any `Throwable` except `NotImplementedError`,
   * requiring the exception message to satisfy the given `messagePredicate`.
   * No help string is provided for the predicate.
   *
   * @param name The name of the test.
   * @param toEvaluate The code block expected to throw an exception.
   * @param messagePredicate A function that takes the exception message and returns `true` if it is acceptable.
   * @tparam T The return type of the `toEvaluate` block.
   * @return An [[ExceptionExcept]][T, NotImplementedError] test instance.
   */
  @targetName("applyWithPredicate")
  def apply[T](
    name: String,
    toEvaluate: => T,
    messagePredicate: String => Boolean // Predicate overload (no help)
  ): ExceptionExcept[T, NotImplementedError] =
    ExceptionExcept[T, NotImplementedError](
      name = name,
      toEvaluate = toEvaluate,
      mkString = (obj: T) => obj.toString, // Default
      expectedMessage = None, // No exact message
      messagePredicate = messagePredicate, // Use provided predicate
      predicateHelp = None, // No help text
      timeoutOverride = None // Default
    )

  /**
   * Creates a test expecting any `Throwable` except `NotImplementedError`,
   * requiring the exception message to match `expectedMessage` exactly,
   * and specifying a custom timeout.
   *
   * @param name The name of the test.
   * @param toEvaluate The code block expected to throw an exception.
   * @param expectedMessage The exact string the thrown exception's message must equal.
   * @param timeoutOverride The specific timeout duration in seconds for this test.
   * @tparam T The return type of the `toEvaluate` block.
   * @return An [[ExceptionExcept]][T, NotImplementedError] test instance.
   */
  @targetName("applyWithExactMessageAndTimeout")
  def apply[T](
    name: String,
    toEvaluate: => T,
    expectedMessage: String,
    timeoutOverride: Int
  ): ExceptionExcept[T, NotImplementedError] =
    ExceptionExcept[T, NotImplementedError](
      name = name,
      toEvaluate = toEvaluate,
      mkString = (obj: T) => obj.toString, // Default
      expectedMessage = Some(expectedMessage), // Set exact message
      messagePredicate = (_: String) => true, // Default (ignored)
      predicateHelp = None, // Default (ignored)
      timeoutOverride = Some(timeoutOverride) // Set timeout
    )

  /**
   * Creates a test expecting any `Throwable` except `NotImplementedError`,
   * requiring the exception message to satisfy `messagePredicate`,
   * providing help text, and specifying a custom timeout.
   *
   * @param name The name of the test.
   * @param toEvaluate The code block expected to throw an exception.
   * @param messagePredicate A function that takes the exception message and returns `true` if it is acceptable.
   * @param predicateHelp A human-readable description of the predicate for error messages.
   * @param timeoutOverride The specific timeout duration in seconds for this test.
   * @tparam T The return type of the `toEvaluate` block.
   * @return An [[ExceptionExcept]][T, NotImplementedError] test instance.
   */
  @targetName("applyWithPredicateAndHelpAndTimeout")
  def apply[T](
    name: String,
    toEvaluate: => T,
    messagePredicate: String => Boolean,
    predicateHelp: String,
    timeoutOverride: Int
  ): ExceptionExcept[T, NotImplementedError] =
    ExceptionExcept[T, NotImplementedError](
      name = name,
      toEvaluate = toEvaluate,
      mkString = (obj: T) => obj.toString, // Default
      expectedMessage = None, // No exact message
      messagePredicate = messagePredicate, // Use provided predicate
      predicateHelp = Some(predicateHelp), // Use provided help
      timeoutOverride = Some(timeoutOverride) // Set timeout
    )

  /**
   * Creates a test expecting any `Throwable` except `NotImplementedError`,
   * with any exception message, and specifying a custom timeout.
   *
   * @param name The name of the test.
   * @param toEvaluate The code block expected to throw an exception.
   * @param timeoutOverride The specific timeout duration in seconds for this test.
   * @tparam T The return type of the `toEvaluate` block.
   * @return An [[ExceptionExcept]][T, NotImplementedError] test instance.
   */
  def apply[T](
    name: String,
    toEvaluate: => T,
    timeoutOverride: Int
  ): ExceptionExcept[T, NotImplementedError] =
    ExceptionExcept[T, NotImplementedError](
      name = name,
      toEvaluate = toEvaluate,
      mkString = (obj: T) => obj.toString, // Default
      expectedMessage = None, // Default (any message)
      messagePredicate = (_: String) => true, // Default (any message)
      predicateHelp = None, // Default
      timeoutOverride = Some(timeoutOverride) // Set timeout
    )

}
