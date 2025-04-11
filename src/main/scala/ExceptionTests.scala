import test.unit.TestFactory.*
import test.unit.{Config, TestSuite}

import java.io.{FileNotFoundException, IOException}
import java.sql.SQLException
import scala.reflect.ClassTag

object ExceptionTests extends App {

  def mightThrowIoOrSql(flag: Int): String = flag match {
    case 1 => throw new IOException("Disk error")
    case 2 => throw new SQLException("Connection failed", "SQLState123", 1001)
    case 3 =>
      throw new FileNotFoundException(
        "File not here"
      ) // Subclass of IOException
    case 4 => throw new IllegalArgumentException("Bad flag") // Unexpected
    case _ => "Success"
  }

  val tests = TestSuite(
    "ExceptionOneOf Tests",
    // Test passes: IOException is in the list
    expectExceptionOneOf(
      name = "Test IO",
      toEvaluate = mightThrowIoOrSql(1))(
      implicitly[ClassTag[IOException]],
      implicitly[ClassTag[SQLException]]
    ),
    // Test passes: SQLException is in the list
    expectExceptionOneOf(
      name = "Test SQL",
      toEvaluate = mightThrowIoOrSql(2))(
      implicitly[ClassTag[IOException]],
      implicitly[ClassTag[SQLException]]
    ),
    // Test fails: IOException is not in the list
    expectExceptionOneOf("Test IO", mightThrowIoOrSql(1))(
      implicitly[ClassTag[FileNotFoundException]],
      implicitly[ClassTag[SQLException]]
    ),
    // Test fails: FileNotFoundException is not in the list
    expectExceptionOneOf("Test IO", mightThrowIoOrSql(3))(
      implicitly[ClassTag[IllegalArgumentException]],
      implicitly[ClassTag[SQLException]]
    ),
    // Test fails: IOException is in the list but wrong message
    expectExceptionOneOf("Test IO", mightThrowIoOrSql(1), "an error was produced")(
      implicitly[ClassTag[IOException]],
      implicitly[ClassTag[SQLException]]
    )
  )

  // Define your configuration (or use Config.Default)
  given config: Config = Config.Default.copy(language = test.unit.Language.English)
  // Or: given config: Config = Config.Default


  TestSuite.runAll(tests)(using config)
}
