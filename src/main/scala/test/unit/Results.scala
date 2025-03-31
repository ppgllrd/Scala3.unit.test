package test.unit

/**
 * Holds the aggregated results of running a [[TestSuite]].
 * Requires a `Config` implicitly when generating its string representation (`toString`)
 * to ensure correct localization and coloring of the summary.
 *
 * @param results Array of individual `TestResult` objects (enum cases) from the suite run.
 * @author Pepe Gallardo
 */
class Results(private val results: Array[TestResult]):

  /** Returns the number of tests that passed. */
  def getPassed: Int =
    results.count(_.isSuccess)

  /** Returns the number of tests that failed. */
  def getFailed: Int =
    results.count(!_.isSuccess)

  /** Returns the total number of tests executed. */
  def getTotal: Int =
    results.length

  /**
   * Returns a compact string representation of outcomes (e.g., "+-++-").
   * '+' for success, '-' for failure. Does not use color.
   */
  def getDetails: String =
    results.map(result => if result.isSuccess then "+" else "-").mkString

  /** Returns true if all tests in the suite passed, false otherwise. */
  def isSuccessful: Boolean =
    getFailed == 0

  /** Returns the success rate as a fraction (0.0 to 1.0). */
  def getSuccessRate: Double =
    val total = getTotal
    if total == 0 then 1.0 else getPassed.toDouble / total // Avoid division by zero

  /**
   * Generates a formatted string summary of the test suite results.
   * Requires a `Config` implicitly to handle localization and coloring.
   * Example: "Passed: 5, Failed: 1, Total: 6, Detail: +++-++" (with colors)
   *
   * @param config The configuration context providing localization and logger for coloring.
   * @return The formatted summary string.
   */
  def mkString()(using config: Config): String = {
    val logger = config.logger // Get logger from config for coloring

    val passed = getPassed
    val failed = getFailed
    val total = getTotal

    // Use config for localization
    val passedLabel = config.msg("results.passed")
    val failedLabel = config.msg("results.failed")
    val totalLabel = config.msg("results.total")
    val detailLabel = config.msg("results.detail")

    // Use logger for coloring numbers and the detail string
    val passedPart = s"${logger.green(passedLabel)}: ${logger.green(passed.toString)}"
    val failedPart = s"${logger.red(failedLabel)}: ${logger.red(failed.toString)}"

    // Color the detail string (+/-) based on individual results
    val detailValue = results.map { result =>
      if result.isSuccess then logger.green("+") else logger.red("-")
    }.mkString

    s"$passedPart, $failedPart, $totalLabel: $total, $detailLabel: $detailValue"
  }

  override def toString: String =
    mkString()(using Config.Default) // Use default config for string representation

  override def equals(other: Any): Boolean = other match {
    case that: Results =>
      (this eq that) || (this.results sameElements that.results)
    case _ => false
  }

  override def hashCode(): Int = {
    val prime = 31
    var result = 1
    result = prime * result + java.util.Arrays.hashCode(results.asInstanceOf[Array[Object]])
    result
  }

