package test.unit

/**
 * Utilities for ANSI colors in console output.
 * Provides escape codes for common colors and styles.
 */
object AnsiColor:
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
   * Wraps text with ANSI color/style codes if enabled.
   *
   * @param text The text to color.
   * @param color The ANSI escape code string for the color/style.
   * @param enabled If true, apply coloring; otherwise, return original text.
   * @return The potentially colored text.
   */
  private def colored(text: String, color: String, enabled: Boolean): String =
    if enabled then s"$color$text$RESET" else text

  /** Applies red color. */
  def red(text: String, enabled: Boolean = true): String = colored(text, RED, enabled)
  /** Applies green color. */
  def green(text: String, enabled: Boolean = true): String = colored(text, GREEN, enabled)
  /** Applies yellow color. */
  def yellow(text: String, enabled: Boolean = true): String = colored(text, YELLOW, enabled)
  /** Applies cyan color. */
  def cyan(text: String, enabled: Boolean = true): String = colored(text, CYAN, enabled)
  /** Applies blue color. */
  def blue(text: String, enabled: Boolean = true): String = colored(text, BLUE, enabled)
  /** Applies bold style. */
  def bold(text: String, enabled: Boolean = true): String = colored(text, BOLD, enabled)
  /** Applies underline style. */
  def underline(text: String, enabled: Boolean = true): String = colored(text, UNDERLINE, enabled)
