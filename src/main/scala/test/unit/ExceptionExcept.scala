package test.unit

import scala.annotation.targetName
import scala.reflect.ClassTag


/**
 * A specific exception test that verifies an expression throws *any* `Throwable`
 * *except* for a specified excluded type `E`.
 * It optionally allows checking the thrown exception's message against an exact string or a predicate.
 *
 * It extends [[ExceptionBy]], providing a `throwablePredicate` that checks if the
 * thrown exception is *not* an instance of the excluded type `E`.
 * The constructor is private; instances should be created using the factory methods in the companion object.
 *
 * @param name The descriptive name of the test case.
 * @param toEvaluate The call-by-name expression expected to throw an exception (but not of type `E`).
 * @param mkString A function to convert the result (type `T`) to a string if no exception is thrown.
 * @param excludedExceptionClass The `Class` object representing the exception type `E` that is *not* expected.
 * @param _expectedMessage Internal storage for the optional exact message expectation.
 * @param _messagePredicate Internal storage for the message predicate function.
 * @param _predicateHelp Internal storage for the optional predicate help text.
 * @param _helpKey Internal storage for the localization key describing the overall expectation.
 * @param _helpArgs Internal storage for the arguments used to format the `_helpKey`.
 * @param timeoutOverride An optional duration in seconds to override the default test timeout.
 * @tparam T The return type of `toEvaluate` (if it were to complete normally).
 * @tparam E The type of `Throwable` that is explicitly *not* expected.
 * @author Pepe Gallardo & Gemini
 */
class ExceptionExcept[T, E <: Throwable] private (
  override val name: String,
  toEvaluate: => T,
  mkString: T => String,
  excludedExceptionClass: Class[E],
  _expectedMessage: Option[String], // Renamed to avoid clash with parent's protected val
  _messagePredicate: String => Boolean, // Renamed
  _predicateHelp: Option[String], // Renamed
  _helpKey: String, // Renamed
  _helpArgs: Seq[HelpArg], // Renamed, now Seq[HelpArg]
  override protected val timeoutOverride: Option[Int]
) extends ExceptionBy[T](
  name = name,
  toEvaluate = toEvaluate,
  mkString = mkString,
  throwablePredicate = (thrown: Throwable) => !excludedExceptionClass.isInstance(thrown), // Predicate: NOT instance of E
  expectedMessage = _expectedMessage, // Pass renamed value to parent
  messagePredicate = _messagePredicate, // Pass renamed value to parent
  predicateHelp = _predicateHelp, // Pass renamed value to parent
  helpKey = _helpKey, // Pass renamed value to parent
  helpArgs = _helpArgs, // Pass Seq[HelpArg] to parent
  timeoutOverride = timeoutOverride
) {
  // Logic inherited from ExceptionBy
}

/**
 * Companion object for the [[ExceptionExcept]] class.
 * Provides factory `apply` methods for creating tests that expect any exception *except* a specific type `E`.
 *
 * @author Pepe Gallardo & Gemini
 */
object ExceptionExcept {

  /**
   * Creates an `ExceptionExcept` test instance, the most general factory method.
   * It determines the appropriate localization key (`helpKey`) and arguments (`helpArgs`)
   * based on whether an exact message, a predicate with help, or neither is specified.
   *
   * @param name The name of the test.
   * @param toEvaluate The code block expected to throw an exception (but not `E`).
   * @param mkString Function to convert result `T` to string if no exception is thrown. Defaults to `.toString`.
   * @param expectedMessage If `Some(msg)`, requires the thrown exception's message to be exactly `msg`. Priority over `messagePredicate`.
   * @param messagePredicate If `expectedMessage` is `None`, this predicate is applied to the message. Defaults to always true.
   * @param predicateHelp Description of the `messagePredicate` for error messages.
   * @param timeoutOverride Optional specific timeout duration in seconds.
   * @tparam T The return type of `toEvaluate`.
   * @tparam E The exception type (`<: Throwable`) that is *not* expected. Requires a `ClassTag`.
   * @return An [[ExceptionExcept]][T, E] test instance.
   */
  def apply[T, E <: Throwable : ClassTag](
    name: String,
    toEvaluate: => T,
    mkString: T => String = (obj: T) => obj.toString,
    expectedMessage: Option[String] = None,
    messagePredicate: String => Boolean = (_: String) => true,
    predicateHelp: Option[String] = None,
    timeoutOverride: Option[Int] = None
  ): ExceptionExcept[T, E] = {
    // Get the runtime class and simple name of the excluded type E
    val excludedClass = implicitly[ClassTag[E]].runtimeClass.asInstanceOf[Class[E]]
    val excludedTypeName = excludedClass.getSimpleName

    // Determine the localization key and formatting arguments based on message expectations
    val (key: String, args: Seq[HelpArg]) = expectedMessage match {
      case Some(exactMsg) =>
        // Expecting an exact message
        ("exception.except.with.message.description", Seq(HelpArg.TypeName(excludedTypeName), HelpArg.ExactMessage(exactMsg)))
      case None => // Not expecting an exact message, check predicate
        predicateHelp match {
          case Some(help) =>
            // Expecting predicate with help text
            ("exception.except.with.predicate.description", Seq(HelpArg.TypeName(excludedTypeName), HelpArg.PredicateHelp(help)))
          case None =>
             // Expecting any message (or predicate without help text)
            ("exception.except.description", Seq(HelpArg.TypeName(excludedTypeName)))
        }
    }

    // Call the private constructor with all parameters
    new ExceptionExcept[T, E](
      name = name,
      toEvaluate = toEvaluate,
      mkString = mkString,
      excludedExceptionClass = excludedClass,
      _expectedMessage = expectedMessage,
      _messagePredicate = messagePredicate,
      _predicateHelp = predicateHelp,
      _helpKey = key, // Pass determined key
      _helpArgs = args, // Pass determined args
      timeoutOverride = timeoutOverride
    )
  }

  // --- Convenience Overloads ---

  /**
   * Creates a test expecting any exception except `E`, with an *exact* message requirement.
   *
   * @param name Test name.
   * @param toEvaluate Code block expected to throw an exception (not `E`).
   * @param expectedMessage The exact required message of the thrown exception.
   * @tparam T Return type of `toEvaluate`.
   * @tparam E Excluded exception type. Requires `ClassTag`.
   * @return An [[ExceptionExcept]][T, E] test instance.
   */
  @targetName("applyWithExactMessage")
  def apply[T, E <: Throwable : ClassTag](name: String, toEvaluate: => T, expectedMessage: String): ExceptionExcept[T, E] = {
    val excludedClass = implicitly[ClassTag[E]].runtimeClass.asInstanceOf[Class[E]]
    val excludedTypeName = excludedClass.getSimpleName
    val key = "exception.except.with.message.description"
    val args = Seq(HelpArg.TypeName(excludedTypeName), HelpArg.ExactMessage(expectedMessage))

    new ExceptionExcept[T, E](
      name = name,
      toEvaluate = toEvaluate,
      mkString = (obj: T) => obj.toString, // Default
      excludedExceptionClass = excludedClass,
      _expectedMessage = Some(expectedMessage), // Set exact message
      _messagePredicate = (_: String) => true, // Default (ignored)
      _predicateHelp = None, // Default (ignored)
      _helpKey = key,
      _helpArgs = args,
      timeoutOverride = None // Default
    )
  }

  /**
   * Creates a test expecting any exception except `E`, with a message *predicate* requirement and help text.
   *
   * @param name Test name.
   * @param toEvaluate Code block expected to throw an exception (not `E`).
   * @param messagePredicate Predicate the thrown exception's message must satisfy.
   * @param predicateHelp Description of the predicate for error messages.
   * @tparam T Return type of `toEvaluate`.
   * @tparam E Excluded exception type. Requires `ClassTag`.
   * @return An [[ExceptionExcept]][T, E] test instance.
   */
  @targetName("applyWithPredicateAndHelp")
  def apply[T, E <: Throwable : ClassTag](name: String, toEvaluate: => T, messagePredicate: String => Boolean, predicateHelp: String): ExceptionExcept[T, E] = {
     val excludedClass = implicitly[ClassTag[E]].runtimeClass.asInstanceOf[Class[E]]
     val excludedTypeName = excludedClass.getSimpleName
     val key = "exception.except.with.predicate.description"
     val args = Seq(HelpArg.TypeName(excludedTypeName), HelpArg.PredicateHelp(predicateHelp))

     new ExceptionExcept[T, E](
       name = name,
       toEvaluate = toEvaluate,
       mkString = (obj: T) => obj.toString, // Default
       excludedExceptionClass = excludedClass,
       _expectedMessage = None, // No exact message
       _messagePredicate = messagePredicate, // Use provided predicate
       _predicateHelp = Some(predicateHelp), // Use provided help
       _helpKey = key,
       _helpArgs = args,
       timeoutOverride = None // Default
     )
   }

  /**
   * Creates a test expecting any exception except `E`, with a message *predicate* requirement (no help text).
   *
   * @param name Test name.
   * @param toEvaluate Code block expected to throw an exception (not `E`).
   * @param messagePredicate Predicate the thrown exception's message must satisfy.
   * @tparam T Return type of `toEvaluate`.
   * @tparam E Excluded exception type. Requires `ClassTag`.
   * @return An [[ExceptionExcept]][T, E] test instance.
   */
  @targetName("applyWithPredicate")
  def apply[T, E <: Throwable : ClassTag](name: String, toEvaluate: => T, messagePredicate: String => Boolean): ExceptionExcept[T, E] = {
    val excludedClass = implicitly[ClassTag[E]].runtimeClass.asInstanceOf[Class[E]]
    val excludedTypeName = excludedClass.getSimpleName
    // Predicate without help uses the basic description key
    val key = "exception.except.description"
    val args = Seq(HelpArg.TypeName(excludedTypeName)) // Only type name needed

    new ExceptionExcept[T, E](
      name = name,
      toEvaluate = toEvaluate,
      mkString = (obj: T) => obj.toString, // Default
      excludedExceptionClass = excludedClass,
      _expectedMessage = None, // No exact message
      _messagePredicate = messagePredicate, // Use provided predicate
      _predicateHelp = None, // No help text
      _helpKey = key,
      _helpArgs = args,
      timeoutOverride = None // Default
    )
  }

  // --- Timeout Overloads ---

  /**
   * Creates a test expecting any exception except `E`, with an *exact* message and a specific timeout.
   *
   * @param name Test name.
   * @param toEvaluate Code block expected to throw an exception (not `E`).
   * @param expectedMessage The exact required message of the thrown exception.
   * @param timeoutOverride Specific timeout duration in seconds.
   * @tparam T Return type of `toEvaluate`.
   * @tparam E Excluded exception type. Requires `ClassTag`.
   * @return An [[ExceptionExcept]][T, E] test instance.
   */
  @targetName("applyWithExactMessageAndTimeout")
  def apply[T, E <: Throwable : ClassTag](name: String, toEvaluate: => T, expectedMessage: String, timeoutOverride: Int): ExceptionExcept[T, E] = {
    val excludedClass = implicitly[ClassTag[E]].runtimeClass.asInstanceOf[Class[E]]
    val excludedTypeName = excludedClass.getSimpleName
    val key = "exception.except.with.message.description"
    val args = Seq(HelpArg.TypeName(excludedTypeName), HelpArg.ExactMessage(expectedMessage))

    new ExceptionExcept[T, E](
      name = name,
      toEvaluate = toEvaluate,
      mkString = (obj: T) => obj.toString, // Default
      excludedExceptionClass = excludedClass,
      _expectedMessage = Some(expectedMessage), // Set exact message
      _messagePredicate = (_: String) => true, // Default (ignored)
      _predicateHelp = None, // Default (ignored)
      _helpKey = key,
      _helpArgs = args,
      timeoutOverride = Some(timeoutOverride) // Set timeout
    )
  }

  /**
   * Creates a test expecting any exception except `E`, with a message *predicate*, help text, and a specific timeout.
   *
   * @param name Test name.
   * @param toEvaluate Code block expected to throw an exception (not `E`).
   * @param messagePredicate Predicate the thrown exception's message must satisfy.
   * @param predicateHelp Description of the predicate for error messages.
   * @param timeoutOverride Specific timeout duration in seconds.
   * @tparam T Return type of `toEvaluate`.
   * @tparam E Excluded exception type. Requires `ClassTag`.
   * @return An [[ExceptionExcept]][T, E] test instance.
   */
  @targetName("applyWithPredicateAndHelpAndTimeout")
  def apply[T, E <: Throwable : ClassTag](name: String, toEvaluate: => T, messagePredicate: String => Boolean, predicateHelp: String, timeoutOverride: Int): ExceptionExcept[T, E] = {
    val excludedClass = implicitly[ClassTag[E]].runtimeClass.asInstanceOf[Class[E]]
    val excludedTypeName = excludedClass.getSimpleName
    val key = "exception.except.with.predicate.description"
    val args = Seq(HelpArg.TypeName(excludedTypeName), HelpArg.PredicateHelp(predicateHelp))

    new ExceptionExcept[T, E](
      name = name,
      toEvaluate = toEvaluate,
      mkString = (obj: T) => obj.toString, // Default
      excludedExceptionClass = excludedClass,
      _expectedMessage = None, // No exact message
      _messagePredicate = messagePredicate, // Use provided predicate
      _predicateHelp = Some(predicateHelp), // Use provided help
      _helpKey = key,
      _helpArgs = args,
      timeoutOverride = Some(timeoutOverride) // Set timeout
    )
  }

  /**
   * Creates a test expecting any exception except `E` (any message), with a specific timeout.
   *
   * @param name Test name.
   * @param toEvaluate Code block expected to throw an exception (not `E`).
   * @param timeoutOverride Specific timeout duration in seconds.
   * @tparam T Return type of `toEvaluate`.
   * @tparam E Excluded exception type. Requires `ClassTag`.
   * @return An [[ExceptionExcept]][T, E] test instance.
   */
  def apply[T, E <: Throwable : ClassTag](name: String, toEvaluate: => T, timeoutOverride: Int): ExceptionExcept[T, E] = {
    val excludedClass = implicitly[ClassTag[E]].runtimeClass.asInstanceOf[Class[E]]
    val excludedTypeName = excludedClass.getSimpleName
    val key = "exception.except.description"
    val args = Seq(HelpArg.TypeName(excludedTypeName))

    new ExceptionExcept[T, E](
      name = name,
      toEvaluate = toEvaluate,
      mkString = (obj: T) => obj.toString, // Default
      excludedExceptionClass = excludedClass,
      _expectedMessage = None, // Default (any message)
      _messagePredicate = (_: String) => true, // Default (any message)
      _predicateHelp = None, // Default
      _helpKey = key,
      _helpArgs = args,
      timeoutOverride = Some(timeoutOverride) // Set timeout
    )
  }
}
