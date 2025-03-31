package test.unit

import scala.reflect.ClassTag

/**
 * A test verifying that the evaluated expression throws a specific exception type `E`,
 * optionally checking for an exact message.
 *
 * Extends [[ExceptionBy]] by providing specific predicates and description details.
 *
 * @param name Test name.
 * @param toEvaluate The call-by-name expression expected to throw exception `E`.
 * @param mkString Function to convert `T` to String if no exception is thrown.
 * @param expectedExceptionClass The `Class` object for the expected exception type `E`.
 * @param expectedMessage Optional exact message the thrown exception must have. If `None`, message is not checked.
 * @param timeoutOverride Optional specific timeout duration (in seconds) for this test.
 * @tparam T The type of the expression result (if it didn't throw).
 * @tparam E The type of `Throwable` expected. Must have a `ClassTag`.
 * @author Pepe Gallardo
 */
class Exception[T, E <: Throwable] private ( // Private constructor, use factory
  override val name: String,
  toEvaluate: => T,
  mkString: T => String,
  expectedExceptionClass: Class[E],
  expectedMessage: Option[String],
  override protected val timeoutOverride: Option[Int]
) extends ExceptionBy[T](
  name = name,
  toEvaluate = toEvaluate,
  mkString = mkString,
  throwablePredicate = thrown => expectedExceptionClass.isInstance(thrown), // Predicate: must be instance of E
  messagePredicate = msg => expectedMessage.forall(_ == msg), // Predicate: message must match if Some(expected)
  helpKey = expectedMessage match { // Choose I18n key based on whether message is checked
    case Some(_) => "exception.with.message.description" // "Exception %s with message %s"
    case None => "exception.description" // "Exception %s"
  },
  helpArgs = { // Arguments for the chosen key (raw values, colored at runtime)
    val typeName = expectedExceptionClass.getSimpleName
    expectedMessage match {
      case Some(msg) => Seq(typeName, s""""$msg"""") // Args: TypeName, "Message"
      case None => Seq(typeName) // Arg: TypeName
    }
  },
  timeoutOverride = timeoutOverride
) {
  // Inherits executeTest from ExceptionBy
}

/**
 * Companion object for the [[Exception]] test class.
 * Provides factory `apply` methods using `ClassTag` to obtain the `Class` object for `E`.
 */
object Exception {

  /**
   * Base factory for creating `Exception` tests.
   *
   * @param name Test name.
   * @param toEvaluate Expression expected to throw `E`.
   * @param mkString Function to convert `T` to String. Defaults to `_.toString`.
   * @param expectedMessage Optional exact expected message. Defaults to `None` (message not checked).
   * @param timeoutOverride Optional specific timeout in seconds. Defaults to `None`.
   * @tparam T Type of expression result.
   * @tparam E Type of `Throwable` expected (requires `ClassTag`).
   * @return An `Exception[T, E]` test instance.
   */
  def apply[T, E <: Throwable : ClassTag](
    name: String,
    toEvaluate: => T,
    mkString: T => String = (obj: T) => obj.toString,
    expectedMessage: Option[String] = None,
    timeoutOverride: Option[Int] = None
  ): Exception[T, E] = {
    val expectedClass = implicitly[ClassTag[E]].runtimeClass.asInstanceOf[Class[E]]
    new Exception(name, toEvaluate, mkString, expectedClass, expectedMessage, timeoutOverride)
  }

  // --- Convenience Overloads --- (Call constructor directly)

  /** Creates test expecting specific message (defaults: mkString, timeout). */
  def apply[T, E <: Throwable : ClassTag](
    name: String,
    toEvaluate: => T,
    expectedMessage: String
  ): Exception[T, E] =
    new Exception(
      name,
      toEvaluate,
      mkString = (obj: T) => obj.toString,
      implicitly[ClassTag[E]].runtimeClass.asInstanceOf[Class[E]],
      expectedMessage = Some(expectedMessage),
      timeoutOverride = None
    )

  /** Creates test expecting specific type, no message check (defaults: mkString, timeout). */
  def apply[T, E <: Throwable : ClassTag](
    name: String,
    toEvaluate: => T
  ): Exception[T, E] =
    new Exception(
      name,
      toEvaluate,
      mkString = (obj: T) => obj.toString,
      implicitly[ClassTag[E]].runtimeClass.asInstanceOf[Class[E]],
      expectedMessage = None,
      timeoutOverride = None
    )

  /** Creates test expecting specific type and message, with custom mkString (default: timeout). */
  def apply[T, E <: Throwable : ClassTag](
    name: String,
    toEvaluate: => T,
    mkString: T => String,
    expectedMessage: String
  ): Exception[T, E] =
    new Exception(
      name,
      toEvaluate,
      mkString,
      implicitly[ClassTag[E]].runtimeClass.asInstanceOf[Class[E]],
      expectedMessage = Some(expectedMessage),
      timeoutOverride = None
    )

  /** Creates test expecting specific type, no message check, with custom mkString (default: timeout). */
  def apply[T, E <: Throwable : ClassTag](
    name: String,
    toEvaluate: => T,
    mkString: T => String
  ): Exception[T, E] =
    new Exception(
      name,
      toEvaluate,
      mkString,
      implicitly[ClassTag[E]].runtimeClass.asInstanceOf[Class[E]],
      expectedMessage = None,
      timeoutOverride = None
    )

  /** Creates test expecting specific type and message, with specific timeout (default: mkString). */
  def apply[T, E <: Throwable : ClassTag](
    name: String,
    toEvaluate: => T,
    expectedMessage: String,
    timeoutOverride: Int
  ): Exception[T, E] =
    new Exception(
      name,
      toEvaluate,
      mkString = (obj: T) => obj.toString,
      implicitly[ClassTag[E]].runtimeClass.asInstanceOf[Class[E]],
      expectedMessage = Some(expectedMessage),
      timeoutOverride = Some(timeoutOverride)
    )

  /** Creates test expecting specific type, no message check, with specific timeout (default: mkString). */
  def apply[T, E <: Throwable : ClassTag](
    name: String,
    toEvaluate: => T,
    timeoutOverride: Int
  ): Exception[T, E] =
    new Exception(
      name,
      toEvaluate,
      mkString = (obj: T) => obj.toString,
      implicitly[ClassTag[E]].runtimeClass.asInstanceOf[Class[E]],
      expectedMessage = None,
      timeoutOverride = Some(timeoutOverride)
    )

  /** Creates test expecting specific type and message, with custom mkString and specific timeout. */
  def apply[T, E <: Throwable : ClassTag](
    name: String,
    toEvaluate: => T,
    mkString: T => String,
    expectedMessage: String,
    timeoutOverride: Int
  ): Exception[T, E] =
    new Exception(
      name,
      toEvaluate,
      mkString,
      implicitly[ClassTag[E]].runtimeClass.asInstanceOf[Class[E]],
      expectedMessage = Some(expectedMessage),
      timeoutOverride = Some(timeoutOverride)
    )

  /** Creates test expecting specific type, no message check, with custom mkString and specific timeout. */
  def apply[T, E <: Throwable : ClassTag](
    name: String,
    toEvaluate: => T,
    mkString: T => String,
    timeoutOverride: Int
  ): Exception[T, E] =
    new Exception(
      name,
      toEvaluate,
      mkString,
      implicitly[ClassTag[E]].runtimeClass.asInstanceOf[Class[E]],
      expectedMessage = None,
      timeoutOverride = Some(timeoutOverride)
    )
}
