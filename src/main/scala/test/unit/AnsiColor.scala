package test.unit

/**
 * Provides utilities for embedding ANSI escape codes in strings
 * to produce colored and styled console output.
 *
 * @author Pepe Gallardo & Gemini
 */
object AnsiColor:
  // --- Private ANSI Escape Codes ---
  private val RESET = "\u001B[0m"
  private val BLACK = "\u001B[30m"
  private val RED = "\u001B[31m"
  private val GREEN = "\u001B[32m"
  private val YELLOW = "\u001B[33m"
  private val BLUE = "\u001B[34m"
  private val PURPLE = "\u001B[35m"
  private val CYAN = "\u001B[36m"
  private val WHITE = "\u001B[37m"
  private val BOLD = "\u001B[1m"
  private val UNDERLINE = "\u001b[4m"

  /**
   * Conditionally wraps the given text with ANSI color/style codes.
   * If coloring is disabled, the original text is returned unchanged.
   * The text is always reset to default formatting at the end.
   *
   * @param text The text to be colored or styled.
   * @param color The ANSI escape code string for the desired color or style.
   * @param enabled If true, apply the color/style; otherwise, return the original text.
   * @return The text, potentially wrapped with ANSI codes.
   */
  private def colored(text: String, color: String, enabled: Boolean): String =
    if enabled then s"$color$text$RESET" else text

  // --- Public Coloring/Styling Methods ---

  /** Wraps text in red ANSI codes if enabled. */
  def red(text: String, enabled: Boolean = true): String = colored(text = text, color = RED, enabled = enabled)
  /** Wraps text in green ANSI codes if enabled. */
  def green(text: String, enabled: Boolean = true): String = colored(text = text, color = GREEN, enabled = enabled)
  /** Wraps text in yellow ANSI codes if enabled. */
  def yellow(text: String, enabled: Boolean = true): String = colored(text = text, color = YELLOW, enabled = enabled)
  /** Wraps text in cyan ANSI codes if enabled. */
  def cyan(text: String, enabled: Boolean = true): String = colored(text = text, color = CYAN, enabled = enabled)
  /** Wraps text in blue ANSI codes if enabled. */
  def blue(text: String, enabled: Boolean = true): String = colored(text = text, color = BLUE, enabled = enabled)
  /** Wraps text in bold ANSI codes if enabled. */
  def bold(text: String, enabled: Boolean = true): String = colored(text = text, color = BOLD, enabled = enabled)
  /** Wraps text in underline ANSI codes if enabled. */
  def underline(text: String, enabled: Boolean = true): String = colored(text = text, color = UNDERLINE, enabled = enabled)
