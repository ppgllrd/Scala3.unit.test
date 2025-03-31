package test.unit

import scala.reflect.ClassTag

/**
 * A central factory object providing convenience methods to create instances
 * of all available `Test` types (e.g., `Equal`, `Property`, `Exception`).
 * Importing this object (`import test.unit.TestFactory.*`) allows for concise
 * test definition without needing to import individual companion objects.
 *
 * @author AI Assistant based on Pepe Gallardo's framework
 */
object TestFactory {

  // --- Equality Tests ---

  /** Creates an `Equal` test (uses `==` for comparison). */
  def equal[T](
    name: String,
    toEvaluate: => T,
    expected: T,
    mkString: T => String = (obj: T) => obj.toString,
    timeoutOverride: Option[Int] = None
  ): Equal[T] =
    Equal(name, toEvaluate, expected, mkString, timeoutOverride)

  /** Creates an `Equal` test with a specific timeout (Int). */
  def equal[T](
    name: String,
    toEvaluate: => T,
    expected: T,
    timeoutOverride: Int
  ): Equal[T] =
    Equal(name, toEvaluate, expected, timeoutOverride = Some(timeoutOverride))

  /** Creates an `Equal` test with required args only. */
  def equal[T](
    name: String,
    toEvaluate: => T,
    expected: T
  ): Equal[T] =
    Equal(name, toEvaluate, expected)

  /** Creates an `EqualBy` test (uses a custom `equalsFn`). */
  def equalBy[T](
    name: String,
    toEvaluate: => T,
    expected: T,
    equalsFn: (T, T) => Boolean,
    mkString: T => String = (obj: T) => obj.toString,
    timeoutOverride: Option[Int] = None
  ): EqualBy[T] =
    EqualBy(name, toEvaluate, expected, equalsFn, mkString, timeoutOverride)

  /** Creates an `EqualBy` test with a specific timeout (Int). */
  def equalBy[T](
    name: String,
    toEvaluate: => T,
    expected: T,
    equalsFn: (T, T) => Boolean,
    timeoutOverride: Int
  ): EqualBy[T] =
    EqualBy(name, toEvaluate, expected, equalsFn, timeoutOverride = Some(timeoutOverride))

  /** Creates an `EqualBy` test with required args only. */
  def equalBy[T](
    name: String,
    toEvaluate: => T,
    expected: T,
    equalsFn: (T, T) => Boolean
  ): EqualBy[T] =
    EqualBy(name, toEvaluate, expected, equalsFn)


  // --- Property Tests ---

  /** Creates a `Property` test verifying a predicate. */
  def property[T](
    name: String,
    toEvaluate: => T,
    property: T => Boolean,
    mkString: T => String = (obj: T) => obj.toString,
    help: Option[String] = None,
    timeoutOverride: Option[Int] = None
  ): Property[T] =
    Property(name, toEvaluate, property, mkString, help, timeoutOverride)

  /** Creates a `Property` test with a specific help string. */
  def property[T](
    name: String,
    toEvaluate: => T,
    property: T => Boolean,
    help: String
  ): Property[T] =
    Property(name, toEvaluate, property, help = Some(help))

  /** Creates a `Property` test with predicate only. */
  def property[T](
    name: String,
    toEvaluate: => T,
    property: T => Boolean
  ): Property[T] =
    Property(name, toEvaluate, property)

  /** Creates a `Property` test with custom mkString and help string. */
  def property[T](
    name: String,
    toEvaluate: => T,
    property: T => Boolean,
    mkString: T => String,
    help: String
  ): Property[T] =
    Property(name, toEvaluate, property, mkString, help = Some(help))

  /** Creates a `Property` test with custom mkString only. */
  def property[T](
    name: String,
    toEvaluate: => T,
    property: T => Boolean,
    mkString: T => String
  ): Property[T] =
    Property(name, toEvaluate, property, mkString)

  /** Creates a `Property` test with help string and specific timeout (Int). */
  def property[T](
    name: String,
    toEvaluate: => T,
    property: T => Boolean,
    help: String,
    timeoutOverride: Int
  ): Property[T] =
    Property(name, toEvaluate, property, help = Some(help), timeoutOverride = Some(timeoutOverride))

  /** Creates a `Property` test with specific timeout (Int) only. */
  def property[T](
    name: String,
    toEvaluate: => T,
    property: T => Boolean,
    timeoutOverride: Int
  ): Property[T] =
    Property(name, toEvaluate, property, timeoutOverride = Some(timeoutOverride))


  // --- Assert / Refute Tests ---

  /** Creates an `Assert` test (expression must evaluate to true). */
  def assertTest(
    name: String,
    toEvaluate: => Boolean,
    timeoutOverride: Option[Int] = None
  ): Assert =
    Assert(name, toEvaluate, timeoutOverride)

  /** Creates an `Assert` test with a specific timeout (Int). */
  def assertTest(
    name: String,
    toEvaluate: => Boolean,
    timeoutOverride: Int
  ): Assert =
    Assert(name, toEvaluate, Some(timeoutOverride))

  /** Creates a `Refute` test (expression must evaluate to false). */
  def refuteTest(
    name: String,
    toEvaluate: => Boolean,
    timeoutOverride: Option[Int] = None
  ): Refute =
    Refute(name, toEvaluate, timeoutOverride)

  /** Creates a `Refute` test with a specific timeout (Int). */
  def refuteTest(
    name: String,
    toEvaluate: => Boolean,
    timeoutOverride: Int
  ): Refute =
    Refute(name, toEvaluate, Some(timeoutOverride))


  // --- Exception Tests ---

  /** Creates an `Exception` test (expects specific exception type `E`, optional message). */
  def expectException[T, E <: Throwable : ClassTag](
    name: String,
    toEvaluate: => T,
    mkString: T => String = (obj: T) => obj.toString,
    expectedMessage: Option[String] = None,
    timeoutOverride: Option[Int] = None
  ): Exception[T, E] =
    Exception[T, E](name, toEvaluate, mkString, expectedMessage, timeoutOverride)

  /** Creates an `Exception` test expecting a specific message. */
  def expectException[T, E <: Throwable : ClassTag](
    name: String,
    toEvaluate: => T,
    expectedMessage: String
  ): Exception[T, E] =
    Exception[T, E](name, toEvaluate, expectedMessage = Some(expectedMessage))

  /** Creates an `Exception` test expecting a specific type (any message). */
  def expectException[T, E <: Throwable : ClassTag](
    name: String,
    toEvaluate: => T
  ): Exception[T, E] =
    Exception[T, E](name, toEvaluate, expectedMessage = None)

  /** Creates an `Exception` test expecting specific message and timeout (Int). */
  def expectException[T, E <: Throwable : ClassTag](
    name: String,
    toEvaluate: => T,
    expectedMessage: String,
    timeoutOverride: Int
  ): Exception[T, E] =
    Exception[T, E](name, toEvaluate, expectedMessage = Some(expectedMessage), timeoutOverride = Some(timeoutOverride))

  /** Creates an `Exception` test expecting specific type and timeout (Int). */
  def expectException[T, E <: Throwable : ClassTag](
    name: String,
    toEvaluate: => T,
    timeoutOverride: Int
  ): Exception[T, E] =
    Exception[T, E](name, toEvaluate, expectedMessage = None, timeoutOverride = Some(timeoutOverride))

  /** Creates an `ExceptionExcept` test (expects any exception *except* type `E`). */
  def expectExceptionExcept[T, E <: Throwable : ClassTag](
    name: String,
    toEvaluate: => T,
    mkString: T => String = (obj: T) => obj.toString,
    messagePredicate: String => Boolean = _ => true,
    timeoutOverride: Option[Int] = None
  ): ExceptionExcept[T, E] =
    ExceptionExcept[T, E](name, toEvaluate, mkString, messagePredicate, timeoutOverride)

  /** Creates an `ExceptionExcept` test with a message predicate. */
  def expectExceptionExcept[T, E <: Throwable : ClassTag](
    name: String,
    toEvaluate: => T,
    messagePredicate: String => Boolean
  ): ExceptionExcept[T, E] =
    ExceptionExcept[T, E](name, toEvaluate, messagePredicate = messagePredicate)

  /** Creates an `ExceptionExcept` test with default message predicate. */
  def expectExceptionExcept[T, E <: Throwable : ClassTag](
    name: String,
    toEvaluate: => T
  ): ExceptionExcept[T, E] =
    ExceptionExcept[T, E](name, toEvaluate, messagePredicate = _ => true)

  /** Creates an `ExceptionExcept` test with message predicate and timeout (Int). */
  def expectExceptionExcept[T, E <: Throwable : ClassTag](
    name: String,
    toEvaluate: => T,
    messagePredicate: String => Boolean,
    timeoutOverride: Int
  ): ExceptionExcept[T, E] =
    ExceptionExcept[T, E](name, toEvaluate, messagePredicate = messagePredicate, timeoutOverride = Some(timeoutOverride))

  /** Creates an `ExceptionExcept` test with timeout (Int) only. */
  def expectExceptionExcept[T, E <: Throwable : ClassTag](
    name: String,
    toEvaluate: => T,
    timeoutOverride: Int
  ): ExceptionExcept[T, E] =
    ExceptionExcept[T, E](name, toEvaluate, messagePredicate = _ => true, timeoutOverride = Some(timeoutOverride))

  /** Creates a test expecting any exception except `NotImplementedError`. */
  def anyExceptionButNotImplementedError[T](
    name: String,
    toEvaluate: => T,
    mkString: T => String = (obj: T) => obj.toString,
    messagePredicate: String => Boolean = _ => true,
    timeoutOverride: Option[Int] = None
  ): ExceptionExcept[T, NotImplementedError] =
    AnyExceptionButNotImplementedError(name, toEvaluate, mkString, messagePredicate, timeoutOverride)

  /** Creates a test expecting any exception except `NotImplementedError` with message predicate. */
  def anyExceptionButNotImplementedError[T](
    name: String,
    toEvaluate: => T,
    messagePredicate: String => Boolean
  ): ExceptionExcept[T, NotImplementedError] =
    AnyExceptionButNotImplementedError(name, toEvaluate, messagePredicate = messagePredicate)

  /** Creates a test expecting any exception except `NotImplementedError` with default message predicate. */
  def anyExceptionButNotImplementedError[T](
    name: String,
    toEvaluate: => T
  ): ExceptionExcept[T, NotImplementedError] =
    AnyExceptionButNotImplementedError(name, toEvaluate, messagePredicate = _ => true)

  /** Creates a test expecting any exception except `NotImplementedError` with message predicate and timeout (Int). */
  def anyExceptionButNotImplementedError[T](
    name: String,
    toEvaluate: => T,
    messagePredicate: String => Boolean,
    timeoutOverride: Int
  ): ExceptionExcept[T, NotImplementedError] =
    AnyExceptionButNotImplementedError(name, toEvaluate, messagePredicate = messagePredicate, timeoutOverride = Some(timeoutOverride))

  /** Creates a test expecting any exception except `NotImplementedError` with timeout (Int) only. */
  def anyExceptionButNotImplementedError[T](
    name: String,
    toEvaluate: => T,
    timeoutOverride: Int
  ): ExceptionExcept[T, NotImplementedError] =
    AnyExceptionButNotImplementedError(name, toEvaluate, messagePredicate = _ => true, timeoutOverride = Some(timeoutOverride))
}
