package test.unit

import scala.reflect.ClassTag

/**
 * Factory object for creating tests that verify an expression throws an exception
 * of one *specific* type `E`. It optionally allows checking the thrown exception's
 * message against an *exact* string.
 *
 * This object acts as a convenience wrapper around [[ExceptionOneOf]], simplifying
 * the common case of expecting a single, specific exception type. It does not
 * directly support message predicates (use `ExceptionOneOf` or `TestFactory` for that).
 * There is no corresponding `Exception` class; this object only provides factory methods.
 *
 * @author Pepe Gallardo & Gemini
 */
object Exception {

  /**
   * Base factory method for creating a test that expects a specific exception type `E`.
   * It delegates the actual test creation to [[ExceptionOneOf]].
   * Primarily designed for checking an optional *exact* message.
   *
   * @param name The name of the test.
   * @param toEvaluate The code block expected to throw an exception of type `E`.
   * @param mkString Function to convert result `T` to string if no exception is thrown. Defaults to `.toString`.
   * @param expectedMessage If `Some(msg)`, requires the thrown exception's message to be exactly `msg`.
   * @param timeoutOverride Optional specific timeout duration in seconds.
   * @tparam T The return type of `toEvaluate`.
   * @tparam E The specific exception type (`<: Throwable`) expected. Requires a `ClassTag`.
   * @return An [[ExceptionOneOf]][T] test instance configured for the single type `E`.
   */
  def apply[T, E <: Throwable : ClassTag](
    name: String,
    toEvaluate: => T,
    mkString: T => String = (obj: T) => obj.toString,
    expectedMessage: Option[String] = None, // Pass through exact message
    timeoutOverride: Option[Int] = None
  ): ExceptionOneOf[T] = { // Returns ExceptionOneOf directly
    // Delegate to ExceptionOneOf's base factory, passing the single ClassTag for E
    // Provide default predicate/help, which are effectively ignored if expectedMessage is Some
    ExceptionOneOf[T](
      name = name,
      toEvaluate = toEvaluate,
      mkString = mkString,
      expectedMessage = expectedMessage, // Pass the exact message option
      messagePredicate = (_: String) => true,  // Default predicate (ignored if exact message is present)
      predicateHelp = None,            // Default predicate help (ignored)
      timeoutOverride = timeoutOverride
    )(implicitly[ClassTag[E]]) // Pass the single ClassTag for E as varargs
  }

  // --- Convenience Overloads (delegate to THIS object's base apply method) ---

  /**
   * Creates a test expecting a specific exception type `E` with an *exact* message.
   *
   * @param name Test name.
   * @param toEvaluate Code block expected to throw exception `E`.
   * @param expectedMessage The exact required message of the thrown exception.
   * @tparam T Return type of `toEvaluate`.
   * @tparam E Specific expected exception type. Requires `ClassTag`.
   * @return An [[ExceptionOneOf]][T] test instance.
   */
  def apply[T, E <: Throwable : ClassTag](
    name: String,
    toEvaluate: => T,
    expectedMessage: String // Exact message overload
  ): ExceptionOneOf[T] =
    // Call this object's base apply, setting the exact message
    apply(
        name = name,
        toEvaluate = toEvaluate,
        mkString = (obj: T) => obj.toString, // Default
        expectedMessage = Some(expectedMessage), // Set exact message
        timeoutOverride = None // Default
    )

  /**
   * Creates a test expecting a specific exception type `E` (any message).
   *
   * @param name Test name.
   * @param toEvaluate Code block expected to throw exception `E`.
   * @tparam T Return type of `toEvaluate`.
   * @tparam E Specific expected exception type. Requires `ClassTag`.
   * @return An [[ExceptionOneOf]][T] test instance.
   */
  def apply[T, E <: Throwable : ClassTag](
    name: String,
    toEvaluate: => T // Type only overload
  ): ExceptionOneOf[T] =
     // Call this object's base apply, with no message requirement
     apply(
        name = name,
        toEvaluate = toEvaluate,
        mkString = (obj: T) => obj.toString, // Default
        expectedMessage = None, // No message requirement
        timeoutOverride = None // Default
     )

  /**
   * Creates a test expecting a specific exception type `E`, an *exact* message, and a specific timeout.
   *
   * @param name Test name.
   * @param toEvaluate Code block expected to throw exception `E`.
   * @param expectedMessage The exact required message of the thrown exception.
   * @param timeoutOverride Specific timeout duration in seconds.
   * @tparam T Return type of `toEvaluate`.
   * @tparam E Specific expected exception type. Requires `ClassTag`.
   * @return An [[ExceptionOneOf]][T] test instance.
   */
  def apply[T, E <: Throwable : ClassTag](
    name: String,
    toEvaluate: => T,
    expectedMessage: String,
    timeoutOverride: Int
  ): ExceptionOneOf[T] =
    // Call this object's base apply, setting message and timeout
    apply(
        name = name,
        toEvaluate = toEvaluate,
        mkString = (obj: T) => obj.toString, // Default
        expectedMessage = Some(expectedMessage), // Set exact message
        timeoutOverride = Some(timeoutOverride) // Set timeout
    )

  /**
   * Creates a test expecting a specific exception type `E` (any message) and a specific timeout.
   *
   * @param name Test name.
   * @param toEvaluate Code block expected to throw exception `E`.
   * @param timeoutOverride Specific timeout duration in seconds.
   * @tparam T Return type of `toEvaluate`.
   * @tparam E Specific expected exception type. Requires `ClassTag`.
   * @return An [[ExceptionOneOf]][T] test instance.
   */
  def apply[T, E <: Throwable : ClassTag](
    name: String,
    toEvaluate: => T,
    timeoutOverride: Int
  ): ExceptionOneOf[T] =
     // Call this object's base apply, setting only timeout
     apply(
        name = name,
        toEvaluate = toEvaluate,
        mkString = (obj: T) => obj.toString, // Default
        expectedMessage = None, // No message requirement
        timeoutOverride = Some(timeoutOverride) // Set timeout
     )

}
