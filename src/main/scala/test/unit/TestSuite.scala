package test.unit

/**
 * Represents a collection of related [[Test]] cases.
 * A TestSuite instance holds its name and the sequence of tests it contains.
 * It requires a `Config` object when its `run` method is called to execute the tests.
 *
 * @param name The name of the test suite.
 * @param tests The sequence of `Test` instances belonging to this suite.
 * @author Pepe Gallardo
 */
class TestSuite(private val name: String, private val tests: Test*):

  /**
   * Runs all tests within this suite using the provided configuration.
   * Logs a header for the suite, executes each test, logs the overall results,
   * and returns an aggregated [[Results]] object.
   *
   * @param config The configuration (logger, language, default timeout) to use for this run.
   * @return A `Results` object summarizing the outcomes of the tests in this suite.
   */
  def run()(using config: Config): Results = {
    val logger = config.logger // Get logger from config

    // Format and log the suite header
    val headerMessage = config.msg("suite.for", name)

    if config.logger.supportsAnsiColors then
      logger.println(logger.underline(logger.bold(logger.blue(headerMessage))))
    else
      logger.println(headerMessage)
      val underline = "=" * headerMessage.length
      logger.println(underline)

    // Run each test within the suite
    val testResultsArray: Array[TestResult] = tests.map(_.run()).toArray

    // Create the Results object
    val results = new Results(testResultsArray)

    // Log the summary string provided by results.toString()
    logger.println(s"\n${results.mkString()}\n")
    logger.flush() // Ensure all output is written

    results // Return the aggregated results
  }

/**
 * Companion object for [[TestSuite]].
 * Provides utility methods for running multiple test suites and summarizing results.
 */
object TestSuite:

  /**
   * Prints a summary report for the results of multiple test suites.
   * Requires an explicit `Config` to format the summary correctly (localization, coloring).
   *
   * @param allResults A sequence of `Results` objects, one for each suite run.
   * @param config The configuration used to format the summary output.
   */
  private def printAllResultsSummary(allResults: Seq[Results], config: Config): Unit = {
    val totalSuites = allResults.length
    val totalTests = allResults.map(_.getTotal).sum
    val totalPassed = allResults.map(_.getPassed).sum
    val totalFailed = allResults.map(_.getFailed).sum
    val overallRate = if (totalTests == 0) 1.0 else totalPassed.toDouble / totalTests

    val logger = config.logger // Use logger from the provided config

    // Print a formatted overall summary block
    logger.println(logger.bold(logger.blue("=" * 40)))
    logger.println(logger.bold(logger.blue(config.msg("summary.tittle"))))
    logger.println(logger.bold(logger.blue("=" * 40)))
    logger.println(s"${config.msg("summary.suites.run", totalSuites)}")
    logger.println(s"${config.msg("summary.total.tests", totalTests)}")
    logger.println(s"${config.msg("results.passed").capitalize}: ${logger.green(totalPassed.toString)}")
    logger.println(s"${config.msg("results.failed").capitalize}: ${logger.red(totalFailed.toString)}")
    logger.println(f"${config.msg("summary.success.rate", overallRate * 100)}")
    logger.println(logger.bold(logger.blue("=" * 40)))
    logger.flush() // Ensure summary is printed
  }


  /**
   * Runs multiple test suites sequentially using a given `Config`.
   * After running all suites, it prints an overall summary report.
   *
   * @param config The configuration to use for running all suites and printing the summary.
   * @param testSuites The `TestSuite` instances to run (variable argument list).
   * @return A sequence containing the `Results` object for each executed suite.
   */
  def runAll(config: Config, testSuites: TestSuite*): Seq[Results] = {
    // Run each suite
    val allResults: Seq[Results] = testSuites.map(suite => suite.run()(using config))

    // Print the summary
    printAllResultsSummary(allResults, config)

    allResults // Return the sequence of results
  }

  /**
   * Runs multiple test suites sequentially using the `Config.Default` configuration.
   * After running all suites, it prints an overall summary report.
   *
   * @param testSuites The `TestSuite` instances to run (variable argument list).
   * @return A sequence containing the `Results` object for each executed suite.
   */
  def runAll(testSuites: TestSuite*): Seq[Results] = {
    // Call the primary runAll method with the default configuration
    runAll(Config.Default, testSuites*)
  }
