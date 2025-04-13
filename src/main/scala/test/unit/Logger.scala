package test.unit

/**
 * Defines the interface for logging test execution progress and results.
 * Implementations determine the destination and format of the output
 * (e.g., console with/without color, silent).
 *
 * Logger methods typically require an implicit [[Config]] instance to access
 * localization settings and formatting preferences (like color support).
 *
 * @author Pepe Gallardo & Gemini
 */
trait Logger:
  /**
   * Indicates whether this logger implementation supports rendering ANSI color codes.
   * Used by [[Config]] and other components to conditionally apply colors.
   *
   * @return `true` if ANSI colors are supported and should be used, `false` otherwise.
   */
  def supportsAnsiColors: Boolean

  // --- Convenience methods for applying colors ---
  // These delegate to AnsiColor, passing the logger's support flag.

  /** Applies red color to the text if ANSI colors are supported. */
  def red(text: String): String = AnsiColor.red(text = text, enabled = supportsAnsiColors)
  /** Applies green color to the text if ANSI colors are supported. */
  def green(text: String): String = AnsiColor.green(text = text, enabled = supportsAnsiColors)
  /** Applies blue color to the text if ANSI colors are supported. */
  def blue(text: String): String = AnsiColor.blue(text = text, enabled = supportsAnsiColors)
  /** Applies bold style to the text if ANSI colors are supported. */
  def bold(text: String): String = AnsiColor.bold(text = text, enabled = supportsAnsiColors)
  /** Applies underline style to the text if ANSI colors are supported. */
  def underline(text: String): String = AnsiColor.underline(text = text, enabled = supportsAnsiColors)

  // --- Core Logging Methods (to be implemented by subclasses) ---

  /** Prints the string representation of the given object to the output destination without a trailing newline. */
  def print(any: Any): Unit
  /** Prints the string representation of the given object to the output destination followed by a trailing newline. */
  def println(any: Any): Unit
  /** Prints a newline character to the output destination. */
  def println(): Unit = println("") // Default implementation

  /**
   * Logs information indicating that a specific test is about to start execution.
   * Typically includes printing the test name.
   *
   * @param testName The name of the test being started.
   * @param config The configuration context for the current test run (used for potential formatting).
   */
  def logStart(testName: String)(using config: Config): Unit

  /**
   * Logs the final result of a completed test execution.
   * Implementations should use the provided `config` to format the `result`'s
   * message appropriately (handling localization and colors).
   *
   * @param result The [[TestResult]] object containing the outcome and details of the test.
   * @param config The configuration context for the current test run.
   */
  def logResult(result: TestResult)(using config: Config): Unit

  /**
   * Ensures that any buffered output is written to the underlying destination (e.g., flushing a console stream).
   */
  def flush(): Unit


object Logger:
  /**
   * Applies sentence capitalization to the given text.
   * This method capitalizes the first letter of each sentence
   * while ignoring ANSI color codes and whitespace.
   *
   * It handles:
   *  - Beginning of the string (`^`).
   *  - After a period (`.`), newline (`\n`), or carriage return (`\r`).
   *  - Ignoring whitespace (`\s*`) and line breaks (`\R*`) in between.
   *  - Ignoring ANSI escape codes (`\u001B\[``[0-9;]*m)*`).
    *  - Capitalizing the first lowercase letter found (`[a-z]`).
   *
   * @param text The input text to be processed.
   * @return The text with the first letter of each sentence capitalized.
   */
  def applySentenceCapitalization(text: String): String =
    val rulePattern = """(?m)(?:^|[.\n\r]\s*)(\u001B\[[0-9;]*m)*([a-z])""".r
    rulePattern.replaceAllIn(text, m =>
      // Reconstruct the match: everything up to the letter + capitalized letter
      m.group(0).stripSuffix(m.group(2)) + m.group(2).toUpperCase()
    )

// --- Concrete Logger Implementations ---

/**
 * A [[Logger]] implementation that prints output to the standard console
 * (System.out) *without* using ANSI color codes.
 *
 * @author Pepe Gallardo & Gemini
 */
class ConsoleLogger extends Logger:
  /** ANSI colors are not supported by this logger. */
  override def supportsAnsiColors: Boolean = false

  /** Prints to `System.out` without a newline. */
  def print(any: Any): Unit = System.out.print(any)
  /** Prints to `System.out` with a newline. */
  def println(any: Any): Unit = System.out.println(any)

  /** Logs the test name followed by ": ". */
  def logStart(testName: String)(using config: Config): Unit = {
    print(s"$testName: ") // Simple prefix before result
  }

  /** Logs the formatted message obtained from `result.message`. */
  def logResult(result: TestResult)(using config: Config): Unit = {
    // The result.message method uses the implicit config for localization and formatting (without color here)
    val rawMessage = result.message
    // Apply sentence capitalization to the message
    val capitalizedMessage = Logger.applySentenceCapitalization(rawMessage)
    println(capitalizedMessage)
  }

  /** Flushes `System.out`. */
  def flush(): Unit = System.out.flush()

/**
 * A [[Logger]] implementation that prints output to the standard console
 * (System.out) *with* ANSI color codes enabled.
 * It inherits basic printing logic from [[ConsoleLogger]].
 *
 * @author Pepe Gallardo & Gemini
 */
class AnsiConsoleLogger extends ConsoleLogger:
  /** ANSI colors are supported by this logger. */
  override def supportsAnsiColors: Boolean = true
  // Inherits print, println, logStart, logResult, flush from ConsoleLogger
  // The color methods (red, green, etc.) will now apply colors.
  // The result.message formatting will include colors when called with a config using this logger.

/**
 * A [[Logger]] implementation that produces no output. Useful for suppressing
 * test logging entirely.
 *
 * @author Pepe Gallardo & Gemini
 */
class SilentLogger extends Logger:
  /** ANSI colors are not supported (as there's no output). */
  override def supportsAnsiColors: Boolean = false

  /** Does nothing. */
  def print(any: Any): Unit = ()
  /** Does nothing. */
  def println(any: Any): Unit = ()
  /** Does nothing. */
  def logStart(testName: String)(using config: Config): Unit = ()
  /** Does nothing. */
  def logResult(result: TestResult)(using config: Config): Unit = ()
  /** Does nothing. */
  def flush(): Unit = ()
