package test.unit

import scala.reflect.ClassTag

/** Central factory object providing convenient methods for creating
  * various types of [[Test]] instances (e.g., equality, property, exception tests).
  *
  * This object delegates the actual creation to the companion object factories
  * of the specific test classes (like [[Equal]], [[Property]], [[ExceptionOneOf]], etc.).
  * It aims to provide a simpler, unified entry point for test definition.
  *
  * @author Pepe Gallardo & Gemini
  */
object TestFactory {

  // --- Equality Tests --- (Delegating to Equal / EqualBy)

  /** Creates an [[Equal]] test verifying `toEvaluate == expected`. */
  def equal[T](
      name: String,
      toEvaluate: => T,
      expected: T,
      mkString: T => String = (obj: T) => obj.toString,
      timeoutOverride: Option[Int] = None
  ): Equal[T] = Equal( // Delegate to Equal factory
      name = name,
      toEvaluate = toEvaluate,
      expected = expected,
      mkString = mkString,
      timeoutOverride = timeoutOverride
  )

  /** Creates an [[Equal]] test with a specific timeout. */
  def equal[T](
      name: String,
      toEvaluate: => T,
      expected: T,
      timeoutOverride: Int
  ): Equal[T] = Equal( // Delegate to Equal factory
    name = name,
    toEvaluate = toEvaluate,
    expected = expected,
    timeoutOverride = Some(timeoutOverride)
  )

  /** Creates a basic [[Equal]] test. */
  def equal[T](name: String, toEvaluate: => T, expected: T): Equal[T] =
    Equal(name = name, toEvaluate = toEvaluate, expected = expected) // Delegate

  /** Creates an [[EqualBy]] test using a custom `equalsFn`. */
  def equalBy[T](
      name: String,
      toEvaluate: => T,
      expected: T,
      equalsFn: (T, T) => Boolean,
      mkString: T => String = (obj: T) => obj.toString,
      timeoutOverride: Option[Int] = None
  ): EqualBy[T] = EqualBy( // Delegate to EqualBy factory
      name = name,
      toEvaluate = toEvaluate,
      expected = expected,
      equalsFn = equalsFn,
      mkString = mkString,
      timeoutOverride = timeoutOverride
  )

  /** Creates an [[EqualBy]] test with a custom `equalsFn` and specific timeout. */
  def equalBy[T](
      name: String,
      toEvaluate: => T,
      expected: T,
      equalsFn: (T, T) => Boolean,
      timeoutOverride: Int
  ): EqualBy[T] = EqualBy( // Delegate to EqualBy factory
    name = name,
    toEvaluate = toEvaluate,
    expected = expected,
    equalsFn = equalsFn,
    timeoutOverride = Some(timeoutOverride)
  )

  /** Creates a basic [[EqualBy]] test with a custom `equalsFn`. */
  def equalBy[T](
      name: String,
      toEvaluate: => T,
      expected: T,
      equalsFn: (T, T) => Boolean
  ): EqualBy[T] = EqualBy( // Delegate
      name = name,
      toEvaluate = toEvaluate,
      expected = expected,
      equalsFn = equalsFn
  )

  // --- Property Tests --- (Delegating to Property)

  /** Creates a [[Property]] test verifying `property(toEvaluate)` is true. */
  def property[T](
      name: String,
      toEvaluate: => T,
      property: T => Boolean,
      mkString: T => String = (obj: T) => obj.toString,
      help: Option[String] = None,
      timeoutOverride: Option[Int] = None
  ): Property[T] = Property( // Delegate to Property factory
      name = name,
      toEvaluate = toEvaluate,
      property = property,
      mkString = mkString,
      help = help,
      timeoutOverride = timeoutOverride
  )

  /** Creates a [[Property]] test with a specific help string. */
  def property[T](
      name: String,
      toEvaluate: => T,
      property: T => Boolean,
      help: String
  ): Property[T] = Property( // Delegate
      name = name,
      toEvaluate = toEvaluate,
      property = property,
      help = Some(help)
  )

  /** Creates a basic [[Property]] test. */
  def property[T](
      name: String,
      toEvaluate: => T,
      property: T => Boolean
  ): Property[T] = Property( // Delegate
      name = name,
      toEvaluate = toEvaluate,
      property = property
  )

  /** Creates a [[Property]] test with custom `mkString` and help string. */
  def property[T](
      name: String,
      toEvaluate: => T,
      property: T => Boolean,
      mkString: T => String,
      help: String
  ): Property[T] = Property( // Delegate
      name = name,
      toEvaluate = toEvaluate,
      property = property,
      mkString = mkString,
      help = Some(help)
  )

  /** Creates a [[Property]] test with custom `mkString`. */
  def property[T](
      name: String,
      toEvaluate: => T,
      property: T => Boolean,
      mkString: T => String
  ): Property[T] = Property( // Delegate
      name = name,
      toEvaluate = toEvaluate,
      property = property,
      mkString = mkString
  )

  /** Creates a [[Property]] test with help string and specific timeout. */
  def property[T](
      name: String,
      toEvaluate: => T,
      property: T => Boolean,
      help: String,
      timeoutOverride: Int
  ): Property[T] = Property( // Delegate
      name = name,
      toEvaluate = toEvaluate,
      property = property,
      help = Some(help),
      timeoutOverride = Some(timeoutOverride)
  )

  /** Creates a [[Property]] test with specific timeout. */
  def property[T](
      name: String,
      toEvaluate: => T,
      property: T => Boolean,
      timeoutOverride: Int
  ): Property[T] = Property( // Delegate
      name = name,
      toEvaluate = toEvaluate,
      property = property,
      timeoutOverride = Some(timeoutOverride)
  )

  // --- Assert / Refute Tests --- (Delegating to Assert / Refute)

  /** Creates an [[Assert]] test verifying `toEvaluate` is true. */
  def assertTest(
      name: String,
      toEvaluate: => Boolean,
      timeoutOverride: Option[Int] = None
  ): Assert = Assert( // Delegate to Assert factory
      name = name,
      toEvaluate = toEvaluate,
      timeoutOverride = timeoutOverride
  )

  /** Creates an [[Assert]] test with a specific timeout. */
  def assertTest(
      name: String,
      toEvaluate: => Boolean,
      timeoutOverride: Int
  ): Assert = Assert( // Delegate
      name = name,
      toEvaluate = toEvaluate,
      timeoutOverride = Some(timeoutOverride)
  )

  /** Creates a [[Refute]] test verifying `toEvaluate` is false. */
  def refuteTest(
      name: String,
      toEvaluate: => Boolean,
      timeoutOverride: Option[Int] = None
  ): Refute = Refute( // Delegate to Refute factory
      name = name,
      toEvaluate = toEvaluate,
      timeoutOverride = timeoutOverride
  )

  /** Creates a [[Refute]] test with a specific timeout. */
  def refuteTest(
      name: String,
      toEvaluate: => Boolean,
      timeoutOverride: Int
  ): Refute = Refute( // Delegate
      name = name,
      toEvaluate = toEvaluate,
      timeoutOverride = Some(timeoutOverride)
  )

  // --- Exception Tests --- (Delegating to Exception / ExceptionOneOf / ExceptionExcept / AnyExceptionButNotImplementedError)

  /**
   * Creates a test expecting a *specific* exception type `E`.
   * Delegates to [[Exception]]. Primarily supports checking for an *exact* message.
   * Use `expectExceptionOneOf` or other factories for predicate-based message checks.
   *
   * @param name Test name.
   * @param toEvaluate Code block expected to throw `E`.
   * @param mkString Function to format result if no exception is thrown.
   * @param expectedMessage Optional exact message the thrown exception must have.
   * @param timeoutOverride Optional specific timeout.
   * @tparam T Return type of `toEvaluate`.
   * @tparam E Specific exception type expected. Requires `ClassTag`.
   * @return An [[ExceptionOneOf]][T] test instance (as `Exception` delegates to it).
   */
  def expectException[T, E <: Throwable: ClassTag](
      name: String,
      toEvaluate: => T,
      mkString: T => String = (obj: T) => obj.toString,
      expectedMessage: Option[String] = None,
      // Predicate parameters are handled by the delegate if needed
      timeoutOverride: Option[Int] = None
  ): ExceptionOneOf[T] =
    Exception[T, E]( // Delegate to Exception factory
      name = name,
      toEvaluate = toEvaluate,
      mkString = mkString,
      expectedMessage = expectedMessage,
      timeoutOverride = timeoutOverride
    )

  /** Creates a test expecting specific exception `E` with an *exact* message. */
  def expectException[T, E <: Throwable: ClassTag](
      name: String,
      toEvaluate: => T,
      expectedMessage: String // Exact message overload
  ): ExceptionOneOf[T] =
    Exception[T, E]( // Delegate
        name = name,
        toEvaluate = toEvaluate,
        expectedMessage = expectedMessage
    )

  /** Creates a test expecting specific exception `E` (any message). */
  def expectException[T, E <: Throwable: ClassTag](
      name: String,
      toEvaluate: => T
  ): ExceptionOneOf[T] =
    Exception[T, E](name = name, toEvaluate = toEvaluate) // Delegate


  /**
   * Creates an [[ExceptionOneOf]] test expecting one of the specified `expectedTypes`.
   * Allows checking the message via exact match (`expectedMessage`) or predicate (`messagePredicate`).
   *
   * @param name Test name.
   * @param toEvaluate Code block expected to throw one of the types.
   * @param mkString Function to format result if no exception is thrown.
   * @param expectedMessage Optional exact message (takes priority).
   * @param messagePredicate Message predicate (used if `expectedMessage` is None).
   * @param predicateHelp Help text for the predicate.
   * @param timeoutOverride Optional specific timeout.
   * @param expectedTypes Varargs sequence of allowed exception `ClassTag`s. Must not be empty.
   * @tparam T Return type of `toEvaluate`.
   * @return An [[ExceptionOneOf]][T] test instance.
   */
  def expectExceptionOneOf[T](
      name: String,
      toEvaluate: => T,
      mkString: T => String = (obj: T) => obj.toString,
      expectedMessage: Option[String] = None,
      messagePredicate: String => Boolean = (_: String) => true,
      predicateHelp: Option[String] = None,
      timeoutOverride: Option[Int] = None
  )(expectedTypes: ClassTag[_ <: Throwable]*): ExceptionOneOf[T] =
    ExceptionOneOf[T]( // Delegate to ExceptionOneOf factory
      name = name,
      toEvaluate = toEvaluate,
      mkString = mkString,
      expectedMessage = expectedMessage,
      messagePredicate = messagePredicate,
      predicateHelp = predicateHelp,
      timeoutOverride = timeoutOverride
    )(expectedTypes *) // Pass varargs

  /** Creates an [[ExceptionOneOf]] test with an *exact* message requirement. */
  def expectExceptionOneOf[T](
      name: String,
      toEvaluate: => T,
      expectedMessage: String
  )(expectedTypes: ClassTag[_ <: Throwable]*): ExceptionOneOf[T] =
    ExceptionOneOf[T]( // Delegate
        name = name,
        toEvaluate = toEvaluate,
        expectedMessage = expectedMessage
    )(expectedTypes *)

  /** Creates an [[ExceptionOneOf]] test with a message *predicate* and required help text. */
  def expectExceptionOneOf[T](
      name: String,
      toEvaluate: => T,
      messagePredicate: String => Boolean,
      predicateHelp: String
  )(expectedTypes: ClassTag[_ <: Throwable]*): ExceptionOneOf[T] =
    ExceptionOneOf[T]( // Delegate
        name = name,
        toEvaluate = toEvaluate,
        messagePredicate = messagePredicate,
        predicateHelp = predicateHelp
    )(expectedTypes *)

  /** Creates an [[ExceptionOneOf]] test with a message *predicate* (no help text). */
  def expectExceptionOneOf[T](
      name: String,
      toEvaluate: => T,
      messagePredicate: String => Boolean
  )(expectedTypes: ClassTag[_ <: Throwable]*): ExceptionOneOf[T] =
    ExceptionOneOf[T]( // Delegate
        name = name,
        toEvaluate = toEvaluate,
        messagePredicate = messagePredicate
    )(expectedTypes *)

  /** Creates an [[ExceptionOneOf]] test expecting one of the types (any message). */
  def expectExceptionOneOf[T](
      name: String,
      toEvaluate: => T
  )(expectedTypes: ClassTag[_ <: Throwable]*): ExceptionOneOf[T] =
    ExceptionOneOf[T](name = name, toEvaluate = toEvaluate)(expectedTypes *) // Delegate


  /**
   * Creates an [[ExceptionExcept]] test expecting any exception *except* type `E`.
   * Allows checking the message via exact match (`expectedMessage`) or predicate (`messagePredicate`).
   *
   * @param name Test name.
   * @param toEvaluate Code block expected to throw (but not `E`).
   * @param mkString Function to format result if no exception is thrown.
   * @param expectedMessage Optional exact message (takes priority).
   * @param messagePredicate Message predicate (used if `expectedMessage` is None).
   * @param predicateHelp Help text for the predicate.
   * @param timeoutOverride Optional specific timeout.
   * @tparam T Return type of `toEvaluate`.
   * @tparam E The exception type *not* expected. Requires `ClassTag`.
   * @return An [[ExceptionExcept]][T, E] test instance.
   */
  def expectExceptionExcept[T, E <: Throwable: ClassTag](
      name: String,
      toEvaluate: => T,
      mkString: T => String = (obj: T) => obj.toString,
      expectedMessage: Option[String] = None,
      messagePredicate: String => Boolean = (_: String) => true, // Delegate handles default
      predicateHelp: Option[String] = None, // Delegate handles default
      timeoutOverride: Option[Int] = None
  ): ExceptionExcept[T, E] =
    ExceptionExcept[T, E]( // Delegate to ExceptionExcept factory
      name = name,
      toEvaluate = toEvaluate,
      mkString = mkString,
      expectedMessage = expectedMessage,
      messagePredicate = messagePredicate,
      predicateHelp = predicateHelp,
      timeoutOverride = timeoutOverride
    )

  /** Creates an [[ExceptionExcept]] test with an *exact* message requirement. */
  def expectExceptionExcept[T, E <: Throwable: ClassTag](
      name: String,
      toEvaluate: => T,
      expectedMessage: String
  ): ExceptionExcept[T, E] =
    ExceptionExcept[T, E]( // Delegate
        name = name,
        toEvaluate = toEvaluate,
        expectedMessage = expectedMessage
    )

  /** Creates an [[ExceptionExcept]] test with a message *predicate* and required help text. */
  def expectExceptionExcept[T, E <: Throwable: ClassTag](
      name: String,
      toEvaluate: => T,
      messagePredicate: String => Boolean,
      predicateHelp: String
  ): ExceptionExcept[T, E] =
    ExceptionExcept[T, E]( // Delegate
        name = name,
        toEvaluate = toEvaluate,
        messagePredicate = messagePredicate,
        predicateHelp = predicateHelp
    )

  /** Creates an [[ExceptionExcept]] test with a message *predicate* (no help text). */
  def expectExceptionExcept[T, E <: Throwable: ClassTag](
      name: String,
      toEvaluate: => T,
      messagePredicate: String => Boolean
  ): ExceptionExcept[T, E] =
    ExceptionExcept[T, E]( // Delegate
        name = name,
        toEvaluate = toEvaluate,
        messagePredicate = messagePredicate
    )

  /** Creates an [[ExceptionExcept]] test expecting not E (any message). */
  def expectExceptionExcept[T, E <: Throwable: ClassTag](
      name: String,
      toEvaluate: => T
  ): ExceptionExcept[T, E] =
    ExceptionExcept[T, E](name = name, toEvaluate = toEvaluate) // Delegate


  /**
   * Creates a test expecting any exception *except* `NotImplementedError`.
   * Delegates to [[AnyExceptionButNotImplementedError]].
   * Allows checking the message via exact match (`expectedMessage`) or predicate (`messagePredicate`).
   *
   * @param name Test name.
   * @param toEvaluate Code block expected to throw (but not `NotImplementedError`).
   * @param mkString Function to format result if no exception is thrown.
   * @param expectedMessage Optional exact message (takes priority).
   * @param messagePredicate Message predicate (used if `expectedMessage` is None).
   * @param predicateHelp Help text for the predicate.
   * @param timeoutOverride Optional specific timeout.
   * @tparam T Return type of `toEvaluate`.
   * @return An [[ExceptionExcept]][T, NotImplementedError] test instance.
   */
  def anyExceptionButNotImplementedError[T](
      name: String,
      toEvaluate: => T,
      mkString: T => String = (obj: T) => obj.toString,
      expectedMessage: Option[String] = None,
      messagePredicate: String => Boolean = (_: String) => true, // Delegate handles default
      predicateHelp: Option[String] = None, // Delegate handles default
      timeoutOverride: Option[Int] = None
  ): ExceptionExcept[T, NotImplementedError] =
    AnyExceptionButNotImplementedError[T]( // Delegate to AnyExceptionButNotImplementedError factory
      name = name,
      toEvaluate = toEvaluate,
      mkString = mkString,
      expectedMessage = expectedMessage,
      messagePredicate = messagePredicate,
      predicateHelp = predicateHelp,
      timeoutOverride = timeoutOverride
    )

  /** Creates a test expecting any exception except `NotImplementedError` with an *exact* message. */
  def anyExceptionButNotImplementedError[T](
      name: String,
      toEvaluate: => T,
      expectedMessage: String
  ): ExceptionExcept[T, NotImplementedError] =
    AnyExceptionButNotImplementedError[T]( // Delegate
        name = name,
        toEvaluate = toEvaluate,
        expectedMessage = expectedMessage
    )

  /** Creates a test expecting any exception except `NotImplementedError` with a *predicate* and required help text. */
  def anyExceptionButNotImplementedError[T](
      name: String,
      toEvaluate: => T,
      messagePredicate: String => Boolean,
      predicateHelp: String
  ): ExceptionExcept[T, NotImplementedError] =
    AnyExceptionButNotImplementedError[T]( // Delegate
      name = name,
      toEvaluate = toEvaluate,
      messagePredicate = messagePredicate,
      predicateHelp = predicateHelp
    )

  /** Creates a test expecting any exception except `NotImplementedError` with a *predicate* (no help text). */
  def anyExceptionButNotImplementedError[T](
      name: String,
      toEvaluate: => T,
      messagePredicate: String => Boolean
  ): ExceptionExcept[T, NotImplementedError] =
    AnyExceptionButNotImplementedError[T]( // Delegate
        name = name,
        toEvaluate = toEvaluate,
        messagePredicate = messagePredicate
    )

  /** Creates a test expecting any exception except `NotImplementedError` (any message). */
  def anyExceptionButNotImplementedError[T](
      name: String,
      toEvaluate: => T
  ): ExceptionExcept[T, NotImplementedError] =
    AnyExceptionButNotImplementedError[T](name = name, toEvaluate = toEvaluate) // Delegate

}
