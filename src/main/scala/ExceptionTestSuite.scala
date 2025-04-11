
import test.unit.TestFactory.*
import test.unit.{Config, TestSuite}

import scala.reflect.ClassTag
import java.io.IOException
import java.io.FileNotFoundException
import java.sql.SQLException


// Define a specific Error subclass if needed, or use an existing one like Error
// class NotImplementedError(msg: String = null) extends Error(msg) // Example definition

/**
 * Test suite specifically designed to verify the error reporting
 * of the different exception testing classes when failures occur.
 */
object ExceptionTestSuite extends App {

  // --- Helper functions to produce specific outcomes ---
  def noThrow(): String = "Success"
  def throwArg(msg: String = "Illegal Argument"): Nothing = throw new IllegalArgumentException(msg)
  def throwRuntime(msg: String = "Runtime Error"): Nothing = throw new RuntimeException(msg)
  def throwIO(msg: String = "IO Error"): Nothing = throw new IOException(msg)
  def throwSQL(msg: String = "SQL Error"): Nothing = throw new SQLException(msg)
  def throwNotImplemented(msg: String = "Not Implemented"): Nothing = throw new NotImplementedError(msg)
  def throwFileNotFound(msg: String = "File Not Found"): Nothing = throw new FileNotFoundException(msg)

  def slowThrowArg(msg: String = "Slow Illegal Argument", delayMs: Int = 150): Nothing = {
    Thread.sleep(delayMs)
    throwArg(msg)
  }
  def slowNoThrow(delayMs: Int = 150): String = {
    Thread.sleep(delayMs)
    noThrow()
  }

  // --- Test Suite Definition ---
  val exceptionTests = TestSuite(
    name = "Exception Test Error Reporting",

    // === Testing `expectException` (which uses ExceptionOneOf internally) ===

    expectException[String, IllegalArgumentException](
      name = "expectException: No Exception Thrown",
      toEvaluate = noThrow() // Should fail: no exception
    ),
    expectException[String, IllegalArgumentException](
      name = "expectException: Wrong Exception Type",
      toEvaluate = throwRuntime() // Should fail: wrong type
    ),
    expectException[String, IllegalArgumentException](
      name = "expectException: Correct Type, Wrong Exact Message",
      toEvaluate = throwArg("Actual message"),
      expectedMessage = "Expected message" // Should fail: wrong message
    ),
    expectException[String, IllegalArgumentException](
      name = "expectException: Timeout",
      toEvaluate = slowThrowArg(delayMs = 2000), // Will throw correct type, but too slow
      timeoutOverride = Some(1) // Timeout in seconds (adjust if needed)
    ),

    // === Testing `expectExceptionOneOf` ===

    expectExceptionOneOf[String](
      name = "expectOneOf: No Exception Thrown",
      toEvaluate = noThrow()
    )(implicitly[ClassTag[IOException]], implicitly[ClassTag[SQLException]]), // Should fail: no exception

    expectExceptionOneOf[String](
      name = "expectOneOf: Wrong Exception Type (Not in Set)",
      toEvaluate = throwArg() // IllegalArgumentException is not IO or SQL
    )(implicitly[ClassTag[IOException]], implicitly[ClassTag[SQLException]]), // Should fail: wrong type

    expectExceptionOneOf[String](
      name = "expectOneOf: Correct Type (Subclass), Wrong Exact Message",
      toEvaluate = throwFileNotFound("Actual FNFE"), // FNFE is subclass of IO
      expectedMessage = "Expected message"
    )(implicitly[ClassTag[IOException]], implicitly[ClassTag[SQLException]]), // Should fail: wrong message

    expectExceptionOneOf[String](
      name = "expectOneOf: Correct Type, Wrong Exact Message",
      toEvaluate = throwSQL("Actual SQL message"),
      expectedMessage = "Expected message"
    )(implicitly[ClassTag[IOException]], implicitly[ClassTag[SQLException]]), // Should fail: wrong message

    expectExceptionOneOf[String](
      name = "expectOneOf: Correct Type, Failed Predicate",
      toEvaluate = throwIO("Actual IO message"),
      messagePredicate = _.startsWith("Expected"),
      predicateHelp = "starts with 'Expected'"
    )(implicitly[ClassTag[IOException]], implicitly[ClassTag[SQLException]]), // Should fail: predicate fails

    expectExceptionOneOf[String](
      name = "expectOneOf: Timeout",
      toEvaluate = slowThrowArg(msg = "Irrelevant", delayMs = 2000), // Throws wrong type, but timeout occurs first
      timeoutOverride = Some(1)
    )(implicitly[ClassTag[IOException]], implicitly[ClassTag[SQLException]]), // Should fail: timeout

    // === Testing `expectExceptionExcept` ===

    expectExceptionExcept[String, IOException](
      name = "expectExcept: No Exception Thrown",
      toEvaluate = noThrow() // Should fail: no exception
    ),
    expectExceptionExcept[String, IOException](
      name = "expectExcept: Excluded Type Thrown",
      toEvaluate = throwIO("Throwing the excluded type") // Should fail: type predicate fails
    ),
    expectExceptionExcept[String, IOException](
      name = "expectExcept: Allowed Type, Wrong Exact Message",
      toEvaluate = throwSQL("Actual SQL message"), // SQLException is allowed
      expectedMessage = "Expected message"   // Should fail: wrong message
    ),
    expectExceptionExcept[String, IOException](
      name = "expectExcept: Allowed Type, Failed Predicate",
      toEvaluate = throwSQL("Actual SQL message"),    // SQLException is allowed
      messagePredicate = _.contains("Expected"),
      predicateHelp = "contains 'Expected'"      // Should fail: predicate fails
    ),
    expectExceptionExcept[String, IOException](
      name = "expectExcept: Timeout",
      toEvaluate = slowThrowArg(msg = "Irrelevant", delayMs = 2000), // Type is allowed, but timeout
      timeoutOverride = Some(1)
    ),

    // === Testing `anyExceptionButNotImplementedError` ===

    anyExceptionButNotImplementedError[String](
      name = "anyExceptNIE: No Exception Thrown",
      toEvaluate = noThrow() // Should fail: no exception
    ),
    anyExceptionButNotImplementedError[String](
      name = "anyExceptNIE: Excluded Type (NIE) Thrown",
      toEvaluate = throwNotImplemented("Throwing NIE") // Should fail: type predicate fails
    ),
    anyExceptionButNotImplementedError[String](
      name = "anyExceptNIE: Allowed Type, Wrong Exact Message",
      toEvaluate = throwArg("Actual Arg message"), // Arg is allowed
      expectedMessage = "Expected message"   // Should fail: wrong message
    ),
    anyExceptionButNotImplementedError[String](
      name = "anyExceptNIE: Allowed Type, Failed Predicate",
      toEvaluate = throwRuntime("Actual Runtime message"), // Runtime is allowed
      messagePredicate = _.startsWith("X"),
      predicateHelp = "starts with X"               // Should fail: predicate fails
    ),
    anyExceptionButNotImplementedError[String](
      name = "anyExceptNIE: Timeout",
      toEvaluate = slowThrowArg(msg = "Irrelevant", delayMs = 2000), // Type is allowed, but timeout
      timeoutOverride = Some(1)
    )
  )

  // --- Run the Test Suite ---
  // Define your configuration (or use Config.Default)
  given config: Config = Config.Default.copy(language = test.unit.Language.English)
  // Or: given config: Config = Config.Default

  TestSuite.runAll(exceptionTests)(using config) 

}
