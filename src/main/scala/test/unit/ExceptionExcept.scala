package test.unit

import scala.annotation.targetName
import scala.reflect.ClassTag

/**
 * A test verifying that the evaluated expression throws any `Throwable` *except*
 * a specific excluded type `E`. Optionally, the thrown exception's message
 * can be checked against a predicate.
 *
 * Extends [[ExceptionBy]] by providing specific predicates and description details.
 *
 * @param name Test name.
 * @param toEvaluate The call-by-name expression expected to throw an exception (but not E).
 * @param mkString Function to convert `T` to String if no exception is thrown.
 * @param excludedExceptionClass The `Class` object for the excluded exception type `E`.
 * @param messagePredicate Predicate `String => Boolean` for the thrown exception's message.
 * @param timeoutOverride Optional specific timeout duration (in seconds) for this test.
 * @tparam T The type of the expression result (if it didn't throw).
 * @tparam E The type of `Throwable` that is *not* expected. Must have a `ClassTag`.
 * @author Pepe Gallardo
 */
class ExceptionExcept[T, E <: Throwable] private ( // Private constructor, use factory
  override val name: String,
  toEvaluate: => T,
  mkString: T => String,
  excludedExceptionClass: Class[E],
  messagePredicate: String => Boolean,
  override protected val timeoutOverride: Option[Int]
) extends ExceptionBy[T](
  name = name,
  toEvaluate = toEvaluate,
  mkString = mkString,
  throwablePredicate = (thrown: Throwable) => !excludedExceptionClass.isInstance(thrown), // Predicate: not an instance of E
  messagePredicate = messagePredicate, // Pass message predicate through
  helpKey = "exception.except.description", // I18n key for "Any exception except %s"
  helpArgs = Seq(excludedExceptionClass.getSimpleName), // Argument for the key (raw name, colored at runtime)
  timeoutOverride = timeoutOverride
) {
  // Inherits executeTest from ExceptionBy
}

/**
 * Companion object for the [[ExceptionExcept]] test class.
 * Provides factory `apply` methods using `ClassTag` to obtain the `Class` object for `E`.
 */
object ExceptionExcept {

  /**
   * Base factory for creating `ExceptionExcept` tests.
   *
   * @param name Test name.
   * @param toEvaluate Expression expected to throw (but not E).
   * @param mkString Function to convert `T` to String. Defaults to `_.toString`.
   * @param messagePredicate Predicate for the thrown message. Defaults to always true.
   * @param timeoutOverride Optional specific timeout in seconds.
   * @tparam T Type of expression result.
   * @tparam E Type of `Throwable` *not* expected (requires `ClassTag`).
   * @return An `ExceptionExcept[T, E]` test instance.
   */
  def apply[T, E <: Throwable : ClassTag](
    name: String,
    toEvaluate: => T,
    mkString: T => String = (obj: T) => obj.toString,
    messagePredicate: String => Boolean = _ => true,
    timeoutOverride: Option[Int] = None
  ): ExceptionExcept[T, E] = {
    val excludedClass = implicitly[ClassTag[E]].runtimeClass.asInstanceOf[Class[E]]
    new ExceptionExcept(name, toEvaluate, mkString, excludedClass, messagePredicate, timeoutOverride)
  }

  // --- Convenience Overloads --- (Call constructor directly)

  /** Creates test with specific message predicate (defaults: mkString, timeout). */
  @targetName("applyWithMessagePredicate")
  def apply[T, E <: Throwable : ClassTag](
    name: String,
    toEvaluate: => T,
    messagePredicate: String => Boolean
  ): ExceptionExcept[T, E] =
    new ExceptionExcept(
      name,
      toEvaluate,
      mkString = (obj: T) => obj.toString,
      implicitly[ClassTag[E]].runtimeClass.asInstanceOf[Class[E]],
      messagePredicate,
      timeoutOverride = None)

  /** Creates test with defaults (mkString, messagePredicate, timeout). */
  def apply[T, E <: Throwable : ClassTag](
    name: String,
    toEvaluate: => T
  ): ExceptionExcept[T, E] =
    new ExceptionExcept(
      name,
      toEvaluate,
      mkString = (obj: T) => obj.toString,
      implicitly[ClassTag[E]].runtimeClass.asInstanceOf[Class[E]],
      messagePredicate = _ => true,
      timeoutOverride = None)

  /** Creates test with custom mkString and message predicate (default: timeout). */
  def apply[T, E <: Throwable : ClassTag](
    name: String,
    toEvaluate: => T,
    mkString: T => String,
    messagePredicate: String => Boolean
  ): ExceptionExcept[T, E] =
    new ExceptionExcept(
      name,
      toEvaluate,
      mkString,
      implicitly[ClassTag[E]].runtimeClass.asInstanceOf[Class[E]],
      messagePredicate,
      timeoutOverride = None)

  /** Creates test with custom mkString (defaults: messagePredicate, timeout). */
  @targetName("applyWithMkString")
  def apply[T, E <: Throwable : ClassTag](
    name: String,
    toEvaluate: => T,
    mkString: T => String
  ): ExceptionExcept[T, E] =
    new ExceptionExcept(
      name,
      toEvaluate,
      mkString,
      implicitly[ClassTag[E]].runtimeClass.asInstanceOf[Class[E]],
      messagePredicate = _ => true,
      timeoutOverride = None)

  /** Creates test with specific message predicate and timeout (default: mkString). */
  @targetName("applyWithMessagePredicateAndTimeout")
  def apply[T, E <: Throwable : ClassTag](
    name: String,
    toEvaluate: => T,
    messagePredicate: String => Boolean,
    timeoutOverride: Int
  ): ExceptionExcept[T, E] =
    new ExceptionExcept(
      name,
      toEvaluate,
      mkString = (obj: T) => obj.toString,
      implicitly[ClassTag[E]].runtimeClass.asInstanceOf[Class[E]],
      messagePredicate,
      timeoutOverride = Some(timeoutOverride))

  /** Creates test with specific timeout (defaults: mkString, messagePredicate). */
  def apply[T, E <: Throwable : ClassTag](
    name: String,
    toEvaluate: => T,
    timeoutOverride: Int
  ): ExceptionExcept[T, E] =
    new ExceptionExcept(
      name,
      toEvaluate,
      mkString = (obj: T) => obj.toString,
      implicitly[ClassTag[E]].runtimeClass.asInstanceOf[Class[E]],
      messagePredicate = _ => true,
      timeoutOverride = Some(timeoutOverride))

  /** Creates test with custom mkString, specific message predicate, and timeout. */
  def apply[T, E <: Throwable : ClassTag](
    name: String,
    toEvaluate: => T,
    mkString: T => String,
    messagePredicate: String => Boolean,
    timeoutOverride: Int
  ): ExceptionExcept[T, E] =
    new ExceptionExcept(
      name,
      toEvaluate,
      mkString,
      implicitly[ClassTag[E]].runtimeClass.asInstanceOf[Class[E]],
      messagePredicate,
      Some(timeoutOverride))

  /** Creates test with custom mkString and specific timeout (default: messagePredicate). */
  @targetName("applyWithMkStringAndTimeout")
  def apply[T, E <: Throwable : ClassTag](
    name: String,
    toEvaluate: => T,
    mkString: T => String,
    timeoutOverride: Int
  ): ExceptionExcept[T, E] =
    new ExceptionExcept(
      name,
      toEvaluate,
      mkString,
      implicitly[ClassTag[E]].runtimeClass.asInstanceOf[Class[E]],
      messagePredicate = _ => true,
      timeoutOverride = Some(timeoutOverride))
}
