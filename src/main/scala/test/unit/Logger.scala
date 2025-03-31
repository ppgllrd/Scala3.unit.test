package test.unit

/**
 * Trait defining the interface for logging test execution progress and results.
 * Implementations handle the actual output (e.g., console, file, silent).
 * Log methods require a `Config` implicitly to access localization and formatting preferences.
 */
trait Logger:
  /** Indicates whether this logger supports ANSI color codes for styling output. */
  def supportsAnsiColors: Boolean

  /** Convenience method to apply red color if supported. */
  def red(text: String): String = AnsiColor.red(text, supportsAnsiColors)
  /** Convenience method to apply green color if supported. */
  def green(text: String): String = AnsiColor.green(text, supportsAnsiColors)
  /** Convenience method to apply blue color if supported. */
  def blue(text: String): String = AnsiColor.blue(text, supportsAnsiColors)
  /** Convenience method to apply bold style if supported. */
  def bold(text: String): String = AnsiColor.bold(text, supportsAnsiColors)
  /** Convenience method to apply underline style if supported. */
  def underline(text: String): String = AnsiColor.underline(text, supportsAnsiColors)

  /** Prints the given object without a newline. */
  def print(any: Any): Unit
  /** Prints the given object followed by a newline. */
  def println(any: Any): Unit
  /** Prints a newline. */
  def println(): Unit = println("") // Default implementation

  /**
   * Logs the start of a test execution. Typically prints the test name.
   * Requires `Config` implicitly for potential context-dependent formatting.
   * @param testName The name of the test being started.
   * @param config The configuration context for this run.
   */
  def logStart(testName: String)(using config: Config): Unit

  /**
   * Logs the result of a completed test execution.
   * Requires `Config` implicitly to format the result message correctly
   * using the appropriate language and color settings.
   * @param result The `TestResult` object containing the outcome.
   * @param config The configuration context for this run.
   */
  def logResult(result: TestResult)(using config: Config): Unit

  /** Ensures any buffered output is written to the destination. */
  def flush(): Unit

// --- Concrete Logger Implementations ---

/**
 * A logger that prints output to the standard console without ANSI colors.
 */
class ConsoleLogger extends Logger:
  override def supportsAnsiColors: Boolean = false

  def print(any: Any): Unit = System.out.print(any)
  def println(any: Any): Unit = System.out.println(any)

  /** Logs the test name followed by a colon and space. */
  def logStart(testName: String)(using config: Config): Unit = {
    print(s"$testName: ")
  }

  /** Logs the formatted message from the TestResult. */
  def logResult(result: TestResult)(using config: Config): Unit = {
    // result.message() uses the implicit config for formatting
    println(result.message)
  }

  def flush(): Unit = System.out.flush()

/**
 * A logger that prints output to the standard console *with* ANSI colors.
 * Extends [[ConsoleLogger]].
 */
class AnsiConsoleLogger extends ConsoleLogger:
  override def supportsAnsiColors: Boolean = true
  // Inherits print, println, logStart, logResult, flush

/**
 * A logger that produces no output. Useful for disabling logging.
 */
class SilentLogger extends Logger:
  override def supportsAnsiColors: Boolean = false

  def print(any: Any): Unit = {}
  def println(any: Any): Unit = {}
  def logStart(testName: String)(using config: Config): Unit = {}
  def logResult(result: TestResult)(using config: Config): Unit = {}
  def flush(): Unit = {}
