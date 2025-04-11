package test.unit

import scala.reflect.ClassTag
import scala.util.Try
import scala.annotation.targetName

/**
 * A specific exception test that verifies an expression throws an exception whose type
 * is *one of* a specified set of allowed `Throwable` types.
 * It optionally allows checking the thrown exception's message against an exact string or a predicate.
 *
 * It extends [[ExceptionBy]], providing a `throwablePredicate` that checks if the
 * thrown exception's type is present in the `expectedExceptionClasses` set.
 * The constructor is private; instances should be created using the factory methods in the companion object.
 *
 * @param name The descriptive name of the test case.
 * @param toEvaluate The call-by-name expression expected to throw one of the allowed exception types.
 * @param mkString A function to convert the result (type `T`) to a string if no exception is thrown.
 * @param expectedExceptionClasses A set of `Class` objects representing the allowed exception types.
 * @param _expectedMessage Internal storage for the optional exact message expectation.
 * @param _messagePredicate Internal storage for the message predicate function.
 * @param _predicateHelp Internal storage for the optional predicate help text.
 * @param _helpKey Internal storage for the localization key describing the overall expectation.
 * @param _helpArgs Internal storage for the arguments used to format the `_helpKey`.
 * @param timeoutOverride An optional duration in seconds to override the default test timeout.
 * @tparam T The return type of `toEvaluate` (if it were to complete normally).
 * @author Pepe Gallardo & Gemini
 */
class ExceptionOneOf[T] private (
    override val name: String,
    toEvaluate: => T,
    mkString: T => String,
    expectedExceptionClasses: Set[Class[_ <: Throwable]],
    _expectedMessage: Option[String], // Renamed
    _messagePredicate: String => Boolean, // Renamed
    _predicateHelp: Option[String], // Renamed
    _helpKey: String, // Renamed
    _helpArgs: Seq[HelpArg], // Renamed, now Seq[HelpArg]
    override protected val timeoutOverride: Option[Int]
) extends ExceptionBy[T](
      name = name,
      toEvaluate = toEvaluate,
      mkString = mkString,
      // Predicate: thrown exception's class is one of the expected classes
      throwablePredicate = (thrown: Throwable) =>
        expectedExceptionClasses.exists(cls =>
          // Use Try for safety, although isInstance should generally be safe
          Try(cls.isInstance(thrown)).getOrElse(false)
        ),
      expectedMessage = _expectedMessage, // Pass renamed value to parent
      messagePredicate = _messagePredicate, // Pass renamed value to parent
      predicateHelp = _predicateHelp, // Pass renamed value to parent
      helpKey = _helpKey, // Pass renamed value to parent
      helpArgs = _helpArgs, // Pass Seq[HelpArg] to parent
      timeoutOverride = timeoutOverride
    ) {}

/**
 * Companion object for the [[ExceptionOneOf]] class.
 * Provides factory `apply` methods for creating tests that expect an exception of
 * one of several specified types.
 *
 * @author Pepe Gallardo & Gemini
 */
object ExceptionOneOf {

  /**
   * Creates an `ExceptionOneOf` test instance, the most general factory method.
   * Requires at least one expected exception type. It determines the appropriate
   * localization key (`helpKey`) and arguments (`helpArgs`) based on the number
   * of expected types and whether message checks (exact or predicate) are specified.
   *
   * @param name The name of the test.
   * @param toEvaluate The code block expected to throw one of the `expectedTypes`.
   * @param mkString Function to convert result `T` to string if no exception is thrown. Defaults to `.toString`.
   * @param expectedMessage If `Some(msg)`, requires the thrown exception's message to be exactly `msg`. Priority over `messagePredicate`.
   * @param messagePredicate If `expectedMessage` is `None`, this predicate is applied to the message. Defaults to always true.
   * @param predicateHelp Description of the `messagePredicate` for error messages.
   * @param timeoutOverride Optional specific timeout duration in seconds.
   * @param expectedTypes A varargs sequence of `ClassTag`s representing the allowed exception types (`<: Throwable`). Must not be empty.
   * @tparam T The return type of `toEvaluate`.
   * @return An [[ExceptionOneOf]][T] test instance.
   * @throws IllegalArgumentException if `expectedTypes` is empty.
   */
  def apply[T](
      name: String,
      toEvaluate: => T,
      mkString: T => String = (obj: T) => obj.toString,
      expectedMessage: Option[String] = None,
      messagePredicate: String => Boolean = (_: String) => true,
      predicateHelp: Option[String] = None,
      timeoutOverride: Option[Int] = None
  )(expectedTypes: ClassTag[_ <: Throwable]*): ExceptionOneOf[T] = {
    require(
      expectedTypes.nonEmpty,
      "Must provide at least one expected exception type for ExceptionOneOf."
    )

    // Convert ClassTags to a Set of Class objects
    val expectedClasses: Set[Class[_ <: Throwable]] =
      expectedTypes
        .map(_.runtimeClass.asInstanceOf[Class[_ <: Throwable]])
        .toSet
    // Get simple names for constructing help arguments, sorted for consistent order
    val typeNamesSeq =
      expectedClasses
        .map(_.getSimpleName)
        .toSeq
        .sorted

    // Determine the localization key and formatting arguments based on expectations
    val (key: String, args: Seq[HelpArg]) = expectedMessage match {
      case Some(exactMsg) =>
        // Expecting an exact message
        val baseKey = if (expectedClasses.size == 1) "exception.with.message.description" // Single type key
                      else "exception.oneof.with.message.description" // Multiple types key
        // Create appropriate TypeName or TypeNameList argument
        val typeArg = if (typeNamesSeq.size == 1) HelpArg.TypeName(typeNamesSeq.head)
                      else HelpArg.TypeNameList(typeNamesSeq)
        (baseKey, Seq(typeArg, HelpArg.ExactMessage(exactMsg))) // Args: Type(s), Message

      case None => // Not expecting an exact message, check predicate
          // Create appropriate TypeName or TypeNameList argument
          val typeArg = if (typeNamesSeq.size == 1) HelpArg.TypeName(typeNamesSeq.head)
                        else HelpArg.TypeNameList(typeNamesSeq)
          predicateHelp match {
            case Some(help) =>
               // Expecting predicate with help text
              val baseKey = if (expectedClasses.size == 1) "exception.with.predicate.description"
                            else "exception.oneof.with.predicate.description"
              (baseKey, Seq(typeArg, HelpArg.PredicateHelp(help))) // Args: Type(s), Predicate Help

            case None =>
              // Expecting any message (or predicate without help text)
               val baseKey = if (expectedClasses.size == 1) "exception.description"
                             else "exception.oneof.description"
               (baseKey, Seq(typeArg)) // Args: Type(s) only
        }
    }

    // Call the private constructor
    new ExceptionOneOf[T](
      name = name,
      toEvaluate = toEvaluate,
      mkString = mkString,
      expectedExceptionClasses = expectedClasses,
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
   * Creates a test expecting one of `expectedTypes`, with an *exact* message requirement.
   *
   * @param name Test name.
   * @param toEvaluate Code block expected to throw one of the `expectedTypes`.
   * @param expectedMessage The exact required message of the thrown exception.
   * @param expectedTypes Varargs sequence of allowed exception type `ClassTag`s. Must not be empty.
   * @tparam T Return type of `toEvaluate`.
   * @return An [[ExceptionOneOf]][T] test instance.
   */
  @targetName("applyWithExactMessage")
  def apply[T](name: String, toEvaluate: => T, expectedMessage: String)(
      expectedTypes: ClassTag[_ <: Throwable]*
  ): ExceptionOneOf[T] = {
    require(expectedTypes.nonEmpty, "Must provide at least one expected exception type.")
    val expectedClasses = expectedTypes.map(_.runtimeClass.asInstanceOf[Class[_ <: Throwable]]).toSet
    val typeNamesSeq = expectedClasses.map(_.getSimpleName).toSeq.sorted
    val baseKey = if (expectedClasses.size == 1) "exception.with.message.description" else "exception.oneof.with.message.description"
    val typeArg = if (typeNamesSeq.size == 1) HelpArg.TypeName(typeNamesSeq.head) else HelpArg.TypeNameList(typeNamesSeq)
    val key = baseKey
    val args = Seq(typeArg, HelpArg.ExactMessage(expectedMessage))

    new ExceptionOneOf[T](
      name = name,
      toEvaluate = toEvaluate,
      mkString = (obj: T) => obj.toString, // Default
      expectedExceptionClasses = expectedClasses,
      _expectedMessage = Some(expectedMessage), // Set exact message
      _messagePredicate = (_: String) => true, // Default (ignored)
      _predicateHelp = None, // Default (ignored)
      _helpKey = key,
      _helpArgs = args,
      timeoutOverride = None // Default
    )
  }


  /**
   * Creates a test expecting one of `expectedTypes`, with a message *predicate* requirement and help text.
   *
   * @param name Test name.
   * @param toEvaluate Code block expected to throw one of the `expectedTypes`.
   * @param messagePredicate Predicate the thrown exception's message must satisfy.
   * @param predicateHelp Description of the predicate for error messages.
   * @param expectedTypes Varargs sequence of allowed exception type `ClassTag`s. Must not be empty.
   * @tparam T Return type of `toEvaluate`.
   * @return An [[ExceptionOneOf]][T] test instance.
   */
  @targetName("applyWithPredicateAndHelp")
  def apply[T](
      name: String,
      toEvaluate: => T,
      messagePredicate: String => Boolean,
      predicateHelp: String
  )(expectedTypes: ClassTag[_ <: Throwable]*): ExceptionOneOf[T] = {
    require(expectedTypes.nonEmpty, "Must provide at least one expected exception type.")
    val expectedClasses = expectedTypes.map(_.runtimeClass.asInstanceOf[Class[_ <: Throwable]]).toSet
    val typeNamesSeq = expectedClasses.map(_.getSimpleName).toSeq.sorted
    val baseKey = if (expectedClasses.size == 1) "exception.with.predicate.description" else "exception.oneof.with.predicate.description"
    val typeArg = if (typeNamesSeq.size == 1) HelpArg.TypeName(typeNamesSeq.head) else HelpArg.TypeNameList(typeNamesSeq)
    val key = baseKey
    val args = Seq(typeArg, HelpArg.PredicateHelp(predicateHelp))

    new ExceptionOneOf[T](
      name = name,
      toEvaluate = toEvaluate,
      mkString = (obj: T) => obj.toString, // Default
      expectedExceptionClasses = expectedClasses,
      _expectedMessage = None, // No exact message
      _messagePredicate = messagePredicate, // Use provided predicate
      _predicateHelp = Some(predicateHelp), // Use provided help
      _helpKey = key,
      _helpArgs = args,
      timeoutOverride = None // Default
    )
  }

  /**
   * Creates a test expecting one of `expectedTypes`, with a message *predicate* requirement (no help text).
   *
   * @param name Test name.
   * @param toEvaluate Code block expected to throw one of the `expectedTypes`.
   * @param messagePredicate Predicate the thrown exception's message must satisfy.
   * @param expectedTypes Varargs sequence of allowed exception type `ClassTag`s. Must not be empty.
   * @tparam T Return type of `toEvaluate`.
   * @return An [[ExceptionOneOf]][T] test instance.
   */
  @targetName("applyWithPredicate")
  def apply[T](
      name: String,
      toEvaluate: => T,
      messagePredicate: String => Boolean
  )(expectedTypes: ClassTag[_ <: Throwable]*): ExceptionOneOf[T] = {
     require(expectedTypes.nonEmpty, "Must provide at least one expected exception type.")
     val expectedClasses = expectedTypes.map(_.runtimeClass.asInstanceOf[Class[_ <: Throwable]]).toSet
     val typeNamesSeq = expectedClasses.map(_.getSimpleName).toSeq.sorted
     val baseKey = if (expectedClasses.size == 1) "exception.description" else "exception.oneof.description"
     val typeArg = if (typeNamesSeq.size == 1) HelpArg.TypeName(typeNamesSeq.head) else HelpArg.TypeNameList(typeNamesSeq)
     val key = baseKey
     val args = Seq(typeArg)

     new ExceptionOneOf[T](
       name = name,
       toEvaluate = toEvaluate,
       mkString = (obj: T) => obj.toString, // Default
       expectedExceptionClasses = expectedClasses,
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
   * Creates a test expecting one of `expectedTypes`, with an *exact* message and a specific timeout.
   *
   * @param name Test name.
   * @param toEvaluate Code block expected to throw one of the `expectedTypes`.
   * @param expectedMessage The exact required message of the thrown exception.
   * @param timeoutOverride Specific timeout duration in seconds.
   * @param expectedTypes Varargs sequence of allowed exception type `ClassTag`s. Must not be empty.
   * @tparam T Return type of `toEvaluate`.
   * @return An [[ExceptionOneOf]][T] test instance.
   */
  @targetName("applyWithExactMessageAndTimeout")
  def apply[T](
      name: String,
      toEvaluate: => T,
      expectedMessage: String,
      timeoutOverride: Int
  )(expectedTypes: ClassTag[_ <: Throwable]*): ExceptionOneOf[T] = {
     require(expectedTypes.nonEmpty, "Must provide at least one expected exception type.")
     val expectedClasses = expectedTypes.map(_.runtimeClass.asInstanceOf[Class[_ <: Throwable]]).toSet
     val typeNamesSeq = expectedClasses.map(_.getSimpleName).toSeq.sorted
     val baseKey = if (expectedClasses.size == 1) "exception.with.message.description" else "exception.oneof.with.message.description"
     val typeArg = if (typeNamesSeq.size == 1) HelpArg.TypeName(typeNamesSeq.head) else HelpArg.TypeNameList(typeNamesSeq)
     val key = baseKey
     val args = Seq(typeArg, HelpArg.ExactMessage(expectedMessage))

     new ExceptionOneOf[T](
       name = name,
       toEvaluate = toEvaluate,
       mkString = (obj: T) => obj.toString, // Default
       expectedExceptionClasses = expectedClasses,
       _expectedMessage = Some(expectedMessage), // Set exact message
       _messagePredicate = (_: String) => true, // Default (ignored)
       _predicateHelp = None, // Default (ignored)
       _helpKey = key,
       _helpArgs = args,
       timeoutOverride = Some(timeoutOverride) // Set timeout
     )
   }

  /**
   * Creates a test expecting one of `expectedTypes`, with a message *predicate*, help text, and a specific timeout.
   *
   * @param name Test name.
   * @param toEvaluate Code block expected to throw one of the `expectedTypes`.
   * @param messagePredicate Predicate the thrown exception's message must satisfy.
   * @param predicateHelp Description of the predicate for error messages.
   * @param timeoutOverride Specific timeout duration in seconds.
   * @param expectedTypes Varargs sequence of allowed exception type `ClassTag`s. Must not be empty.
   * @tparam T Return type of `toEvaluate`.
   * @return An [[ExceptionOneOf]][T] test instance.
   */
  @targetName("applyWithPredicateAndHelpAndTimeout")
  def apply[T](
      name: String,
      toEvaluate: => T,
      messagePredicate: String => Boolean,
      predicateHelp: String,
      timeoutOverride: Int
  )(expectedTypes: ClassTag[_ <: Throwable]*): ExceptionOneOf[T] = {
    require(expectedTypes.nonEmpty, "Must provide at least one expected exception type.")
    val expectedClasses = expectedTypes.map(_.runtimeClass.asInstanceOf[Class[_ <: Throwable]]).toSet
    val typeNamesSeq = expectedClasses.map(_.getSimpleName).toSeq.sorted
    val baseKey = if (expectedClasses.size == 1) "exception.with.predicate.description" else "exception.oneof.with.predicate.description"
    val typeArg = if (typeNamesSeq.size == 1) HelpArg.TypeName(typeNamesSeq.head) else HelpArg.TypeNameList(typeNamesSeq)
    val key = baseKey
    val args = Seq(typeArg, HelpArg.PredicateHelp(predicateHelp))

    new ExceptionOneOf[T](
      name = name,
      toEvaluate = toEvaluate,
      mkString = (obj: T) => obj.toString, // Default
      expectedExceptionClasses = expectedClasses,
      _expectedMessage = None, // No exact message
      _messagePredicate = messagePredicate, // Use provided predicate
      _predicateHelp = Some(predicateHelp), // Use provided help
      _helpKey = key,
      _helpArgs = args,
      timeoutOverride = Some(timeoutOverride) // Set timeout
    )
  }

  /**
   * Creates a test expecting one of `expectedTypes` (any message), with a specific timeout.
   *
   * @param name Test name.
   * @param toEvaluate Code block expected to throw one of the `expectedTypes`.
   * @param timeoutOverride Specific timeout duration in seconds.
   * @param expectedTypes Varargs sequence of allowed exception type `ClassTag`s. Must not be empty.
   * @tparam T Return type of `toEvaluate`.
   * @return An [[ExceptionOneOf]][T] test instance.
   */
  def apply[T](name: String, toEvaluate: => T, timeoutOverride: Int)(
      expectedTypes: ClassTag[_ <: Throwable]*
  ): ExceptionOneOf[T] = {
     require(expectedTypes.nonEmpty, "Must provide at least one expected exception type.")
     val expectedClasses = expectedTypes.map(_.runtimeClass.asInstanceOf[Class[_ <: Throwable]]).toSet
     val typeNamesSeq = expectedClasses.map(_.getSimpleName).toSeq.sorted
     val baseKey = if (expectedClasses.size == 1) "exception.description" else "exception.oneof.description"
     val typeArg = if (typeNamesSeq.size == 1) HelpArg.TypeName(typeNamesSeq.head) else HelpArg.TypeNameList(typeNamesSeq)
     val key = baseKey
     val args = Seq(typeArg)

     new ExceptionOneOf[T](
       name = name,
       toEvaluate = toEvaluate,
       mkString = (obj: T) => obj.toString, // Default
       expectedExceptionClasses = expectedClasses,
       _expectedMessage = None, // Default (any message)
       _messagePredicate = (_: String) => true, // Default (any message)
       _predicateHelp = None, // Default
       _helpKey = key,
       _helpArgs = args,
       timeoutOverride = Some(timeoutOverride) // Set timeout
     )
   }
}
