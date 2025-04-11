package test.unit

/**
 * Encapsulates the aggregated results of running a [[TestSuite]].
 * It stores the individual [[TestResult]] outcomes and provides methods
 * to query statistics like passed/failed counts and success rate.
 *
 * Generating a string representation (`mkString` or `toString`) requires
 * an implicit [[Config]] instance to handle localization and coloring of the summary.
 *
 * @param results An array containing the individual [[TestResult]] outcomes from the test suite run.
 * @author Pepe Gallardo & Gemini
 */
class Results(private val results: Array[TestResult]):

  /** Returns the total number of tests that passed successfully. */
  def getPassed: Int =
    results.count(_.isSuccess)

  /** Returns the total number of tests that failed. */
  def getFailed: Int =
    results.count(!_.isSuccess) // Count where isSuccess is false

  /** Returns the total number of tests executed in the suite. */
  def getTotal: Int =
    results.length

  /**
   * Returns a compact string showing the outcome of each test.
   * Uses '+' for success and '-' for failure. Does not include color.
   * Example: `"+-++-"`
   *
   * @return A string summarizing individual test outcomes.
   */
  def getDetails: String =
    results.map(result => if result.isSuccess then "+" else "-").mkString

  /**
   * Checks if all tests within the suite passed.
   *
   * @return `true` if `getFailed` is 0, `false` otherwise.
   */
  def isSuccessful: Boolean =
    getFailed == 0

  /**
   * Calculates the success rate as a fraction between 0.0 and 1.0.
   * Returns 1.0 if no tests were run (to avoid division by zero).
   *
   * @return The success rate (passed / total).
   */
  def getSuccessRate: Double =
    val total = getTotal
    if total == 0 then 1.0 else getPassed.toDouble / total

  /**
   * Generates a formatted, localized, and potentially colored string summarizing
   * the test suite results (passed, failed, total counts, and details).
   *
   * Requires an implicit `Config` to access the logger (for coloring) and
   * localization messages (for labels like "Passed", "Failed").
   * Example output (with colors): "Passed: [green]5[reset], Failed: [red]1[reset], Total: 6, Detail: [green]+++[reset][red]-[reset][green]++[reset]"
   *
   * @param config The implicit configuration context.
   * @return The formatted summary string.
   */
  def mkString()(using config: Config): String = {
    val logger = config.logger // Get logger for coloring

    val passedCount = getPassed
    val failedCount = getFailed
    val totalCount = getTotal

    // Get localized labels from config
    val passedLabel = config.msg("results.passed")
    val failedLabel = config.msg("results.failed")
    val totalLabel = config.msg("results.total")
    val detailLabel = config.msg("results.detail")

    // Format parts with appropriate colors using the logger
    val passedPart = s"${logger.green(passedLabel)}: ${logger.green(passedCount.toString)}"
    val failedPart = s"${logger.red(failedLabel)}: ${logger.red(failedCount.toString)}"

    // Color the detail string (+/-) based on individual results
    val detailValue = results.map { result =>
      if result.isSuccess then logger.green("+") else logger.red("-")
    }.mkString

    // Combine all parts into the final summary string
    s"$passedPart, $failedPart, $totalLabel: $totalCount, $detailLabel: $detailValue"
  }

  /**
   * Provides a default string representation using `mkString` with [[Config.Default]].
   * Note: This will use default language (English) and ANSI colors if the default logger supports them.
   * For controlled formatting, use `mkString()(using specificConfig)`.
   *
   * @return The formatted summary string using the default configuration.
   */
  override def toString: String =
    mkString()(using Config.Default) // Use default config for basic toString

  /**
   * Checks for equality with another object.
   * Two `Results` instances are considered equal if they contain the same sequence
   * of `TestResult` objects in the same order.
   *
   * @param other The object to compare against.
   * @return `true` if `other` is a `Results` instance with identical content, `false` otherwise.
   */
  override def equals(other: Any): Boolean = other match {
    case that: Results =>
      // Check for reference equality first for efficiency
      (this eq that) ||
      // Deep comparison: check if the underlying arrays have the same elements in the same order
      (this.results.length == that.results.length &&
       this.results.sameElements(that.results))
    case _ => false
  }

  /**
   * Computes the hash code based on the content of the `results` array.
   * Ensures that equal `Results` instances have the same hash code.
   *
   * @return The hash code.
   */
  override def hashCode(): Int = {
    val prime = 31
    var result = 1
    // Use Arrays.hashCode for content-based hash code of the array
    result = prime * result + java.util.Arrays.hashCode(results.asInstanceOf[Array[Object]])
    result
  }
