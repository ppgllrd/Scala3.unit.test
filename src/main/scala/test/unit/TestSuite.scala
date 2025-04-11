package test.unit

/**
 * Represents a named collection of related [[Test]] cases.
 * A `TestSuite` allows grouping tests and running them together, aggregating
 * their results.
 *
 * @param name The name identifying this suite of tests.
 * @param tests A sequence of [[Test]] instances that constitute this suite.
 * @author Pepe Gallardo & Gemini
 */
class TestSuite(private val name: String, private val tests: Test*): // Uses varargs for convenience

  /**
   * Runs all the [[Test]] cases contained within this suite sequentially,
   * using the provided [[Config]].
   *
   * It performs the following actions:
   * 1. Logs a formatted header indicating the start of the suite execution.
   * 2. Executes each `Test` instance using its `run` method (which handles individual test logging and execution).
   * 3. Collects the [[TestResult]] from each test.
   * 4. Creates and returns an aggregated [[Results]] object containing all individual outcomes.
   * 5. Logs a summary of the suite's results using `Results.mkString`.
   * 6. Flushes the logger.
   *
   * @param config The [[Config]] object providing the logger, language, default timeouts, etc., for this run.
   * @return A [[Results]] object summarizing the outcomes of all tests executed within this suite.
   */
  def run()(using config: Config): Results = {
    val logger = config.logger // Get logger from the implicit config

    // 1. Log Suite Header: Format using I18n and apply styling if supported
    val headerMessage = config.msg("suite.for", name) // Localized "Tests for [name]"
    if (logger.supportsAnsiColors) {
      // Blue, bold, underlined header for ANSI terminals
      logger.println(logger.underline(logger.bold(logger.blue(headerMessage))))
    } else {
      // Plain header with an underline for non-ANSI terminals
      logger.println(headerMessage)
      val underline = "=" * headerMessage.length // Simple underline
      logger.println(underline)
    }

    // 2. Run Individual Tests: Map over the tests sequence and run each one
    //    The run() method of each Test handles its own logging.
    val testResultsArray: Array[TestResult] = tests.map(_.run()).toArray

    // 3. Aggregate Results: Create the Results object from the array of outcomes
    val results = new Results(testResultsArray)

    // 4. Log Suite Summary: Print the formatted summary provided by the Results object
    //    The mkString requires the implicit config for localization/coloring.
    logger.println(s"\n${results.mkString()}\n") // Add newlines around summary

    // 5. Flush Logger: Ensure all output is written
    logger.flush()

    // 6. Return Aggregated Results
    results
  }

/**
 * Companion object for [[TestSuite]].
 * Provides utility methods for running multiple test suites and generating
 * an overall summary report.
 *
 * @author Pepe Gallardo & Gemini
 */
object TestSuite:

  /**
   * Prints a final summary report aggregating results across multiple test suites.
   * This method calculates overall statistics (total suites, tests, passed, failed, success rate)
   * and prints them using the provided `config` for formatting and localization.
   *
   * @param allResults A sequence of [[Results]] objects, typically one per [[TestSuite]] that was run.
   * @param config The [[Config]] used for formatting the summary output (localization, colors).
   */
  private def printAllResultsSummary(allResults: Seq[Results], config: Config): Unit = {
    // Calculate overall statistics
    val totalSuites = allResults.length
    val totalTests = allResults.map(_.getTotal).sum
    val totalPassed = allResults.map(_.getPassed).sum
    val totalFailed = allResults.map(_.getFailed).sum
    // Avoid division by zero if no tests were run
    val overallRate = if (totalTests == 0) 1.0 else totalPassed.toDouble / totalTests

    val logger = config.logger // Get logger for coloring

    // Print the formatted summary block
    logger.println(logger.bold(logger.blue("=" * 40))) // Header separator
    logger.println(logger.bold(logger.blue(config.msg("summary.tittle")))) // Localized title
    logger.println(logger.bold(logger.blue("=" * 40))) // Separator
    logger.println(config.msg("summary.suites.run", totalSuites)) // Suites run count
    logger.println(config.msg("summary.total.tests", totalTests)) // Total tests count
    // Passed/Failed counts with labels and colors
    logger.println(s"${config.msg("results.passed").capitalize}: ${logger.green(totalPassed.toString)}")
    logger.println(s"${config.msg("results.failed").capitalize}: ${logger.red(totalFailed.toString)}")
    // Success rate formatted as percentage
    logger.println(config.msg("summary.success.rate", overallRate * 100.0)) // Format rate
    logger.println(logger.bold(logger.blue("=" * 40))) // Footer separator
    logger.println() // Extra newline for spacing
    logger.flush() // Ensure summary is printed immediately
  }


  /**
   * Runs multiple [[TestSuite]] instances sequentially using a specified [[Config]].
   * After all suites have finished execution, it prints an overall summary report
   * aggregating the results from all executed suites.
   *
   * @param testSuites The [[TestSuite]] instances to run (provided as varargs).
   * @param config The [[Config]] to use for running all the test suites and for printing the final summary.
   * @return A sequence containing the [[Results]] object produced by each executed `TestSuite`.
   */
  private def runAllWithConfig(testSuites: TestSuite*)(config: Config) : Seq[Results] = {
    // Run each suite sequentially, collecting their Results objects
    // The run() method on each suite handles its own internal logging.
    val allResults: Seq[Results] = testSuites.map(suite => suite.run()(using config))

    // Print the final overall summary using the collected results
    printAllResultsSummary(allResults = allResults, config = config)

    // Return the sequence of individual suite results
    allResults
  }

  /**
   * Runs multiple [[TestSuite]] instances sequentially using the default configuration (`Config.Default`).
   * After all suites have finished execution, it prints an overall summary report
   * aggregating the results from all executed suites using the default configuration for formatting.
   *
   * @param testSuites The [[TestSuite]] instances to run (provided as varargs).
   * @return A sequence containing the [[Results]] object produced by each executed `TestSuite`.
   */
  def runAll(testSuites: TestSuite*): Seq[Results] = {
    // Delegate to the primary runAll method, explicitly passing Config.Default
    runAllWithConfig(testSuites = testSuites: _*)(Config.Default)
  }


  /**
   * Runs multiple [[TestSuite]] instances sequentially using a specified [[Config]].
   * After all suites have finished execution, it prints an overall summary report
   * aggregating the results from all executed suites using the default configuration for formatting.
   *
   * @param testSuites The [[TestSuite]] instances to run (provided as varargs).
   * @return A sequence containing the [[Results]] object produced by each executed `TestSuite`.
   */
  def runAll(testSuites: TestSuite*)(using config: Config): Seq[Results] = {
    // Delegate to the primary runAll method, explicitly passing Config.Default
    runAllWithConfig(testSuites = testSuites: _*)(config)
  }
