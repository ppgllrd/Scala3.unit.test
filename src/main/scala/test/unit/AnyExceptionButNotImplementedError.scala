package test.unit

import scala.reflect.ClassTag // Needed for ExceptionExcept factory

/**
 * Factory object for tests expecting any `Throwable` except `NotImplementedError`.
 * This is a specialized factory that delegates to `ExceptionExcept`.
 *
 * @author Pepe Gallardo
 */
object AnyExceptionButNotImplementedError {

  /**
   * Creates a test expecting any exception except `NotImplementedError`.
   *
   * @param name Test name.
   * @param toEvaluate The expression expected to throw an exception.
   * @param mkString Function to convert the result to String if no exception is thrown.
   * @param messagePredicate Predicate for the thrown exception's message.
   * @param timeoutOverride Optional specific timeout for this test.
   * @tparam T The type of the expression result (if it didn't throw).
   * @return An `ExceptionExcept[T, NotImplementedError]` test instance.
   */
  def apply[T](
    name: String,
    toEvaluate: => T,
    mkString: T => String = (obj: T) => obj.toString,
    messagePredicate: String => Boolean = _ => true,
    timeoutOverride: Option[Int] = None
  ): ExceptionExcept[T, NotImplementedError] =
    // Delegate directly to the ExceptionExcept factory
    ExceptionExcept[T, NotImplementedError](
      name = name,
      toEvaluate = toEvaluate,
      mkString = mkString,
      messagePredicate = messagePredicate,
      timeoutOverride = timeoutOverride
    )

  /**
   * Creates a test expecting any exception except `NotImplementedError`, with a message predicate.
   *
   * @param name Test name.
   * @param toEvaluate The expression expected to throw an exception.
   * @param messagePredicate Predicate for the thrown exception's message.
   * @tparam T The type of the expression result (if it didn't throw).
   * @return An `ExceptionExcept[T, NotImplementedError]` test instance.
   */
  def apply[T](
    name: String,
    toEvaluate: => T,
    messagePredicate: String => Boolean
  ): ExceptionExcept[T, NotImplementedError] =
    // Delegate directly to the ExceptionExcept factory
    ExceptionExcept[T, NotImplementedError](
      name = name,
      toEvaluate = toEvaluate,
      mkString = (obj: T) => obj.toString, // Default mkString
      messagePredicate = messagePredicate,
      timeoutOverride = None // Default timeout
    )

  /**
   * Creates a test expecting any exception except `NotImplementedError`, with default message check.
   *
   * @param name Test name.
   * @param toEvaluate The expression expected to throw an exception.
   * @tparam T The type of the expression result (if it didn't throw).
   * @return An `ExceptionExcept[T, NotImplementedError]` test instance.
   */
  def apply[T](
    name: String,
    toEvaluate: => T
  ): ExceptionExcept[T, NotImplementedError] =
    // Delegate directly to the ExceptionExcept factory
    ExceptionExcept[T, NotImplementedError](
      name = name,
      toEvaluate = toEvaluate,
      mkString = (obj: T) => obj.toString, // Default mkString
      messagePredicate = _ => true, // Default message predicate
      timeoutOverride = None // Default timeout
    )

  /**
   * Creates a test expecting any exception except `NotImplementedError`, with a message predicate and specific timeout.
   *
   * @param name Test name.
   * @param toEvaluate The expression expected to throw an exception.
   * @param messagePredicate Predicate for the thrown exception's message.
   * @param timeoutOverride Specific timeout for this test (in seconds).
   * @tparam T The type of the expression result (if it didn't throw).
   * @return An `ExceptionExcept[T, NotImplementedError]` test instance.
   */
  def apply[T](
    name: String,
    toEvaluate: => T,
    messagePredicate: String => Boolean,
    timeoutOverride: Int
  ): ExceptionExcept[T, NotImplementedError] =
    // Delegate directly to the ExceptionExcept factory
    ExceptionExcept[T, NotImplementedError](
      name = name,
      toEvaluate = toEvaluate,
      mkString = (obj: T) => obj.toString, // Default mkString
      messagePredicate = messagePredicate,
      timeoutOverride = Some(timeoutOverride) // Specific timeout
    )

  /**
   * Creates a test expecting any exception except `NotImplementedError`, with a specific timeout.
   *
   * @param name Test name.
   * @param toEvaluate The expression expected to throw an exception.
   * @param timeoutOverride Specific timeout for this test (in seconds).
   * @tparam T The type of the expression result (if it didn't throw).
   * @return An `ExceptionExcept[T, NotImplementedError]` test instance.
   */
  def apply[T](
    name: String,
    toEvaluate: => T,
    timeoutOverride: Int
  ): ExceptionExcept[T, NotImplementedError] =
    // Delegate directly to the ExceptionExcept factory
    ExceptionExcept[T, NotImplementedError](
      name = name,
      toEvaluate = toEvaluate,
      mkString = (obj: T) => obj.toString, // Default mkString
      messagePredicate = _ => true, // Default message predicate
      timeoutOverride = Some(timeoutOverride) // Specific timeout
    )
}
