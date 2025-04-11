package test.unit

/**
 * Abstract base class representing a single, executable test case.
 * Each concrete test class (e.g., [[Equal]], [[Assert]], [[ExceptionOneOf]]) extends this class.
 *
 * It defines the common structure: a name, an optional timeout override, and the `run` method
 * which handles the execution lifecycle (logging start/end, timeout management) by calling
 * the abstract `executeTest` method implemented by subclasses.
 *
 * @author Pepe Gallardo & Gemini
 */
abstract class Test {
  /** The descriptive name identifying this specific test case. */
  val name: String

  /**
   * An optional timeout duration in seconds specific to this test.
   * If `Some(duration)`, this value overrides the default timeout from the [[Config]].
   * If `None`, the default timeout from the `Config` provided to `run` will be used.
   */
  protected val timeoutOverride: Option[Int]

  /**
   * Executes this test case using the provided configuration.
   * This method orchestrates the test execution:
   * 1. Logs the start of the test using the configured logger.
   * 2. Determines the actual timeout value (using override or config default).
   * 3. Calls the specific `executeTest` method (implemented by subclasses)
   *    within the context of the resolved timeout and configuration.
   * 4. Logs the final [[TestResult]].
   * 5. Flushes the logger.
   *
   * @param config The [[Config]] object providing the logger, language, default timeout, etc., for this run.
   * @return The [[TestResult]] indicating the outcome (Success or a specific Failure type).
   */
  final def run()(using config: Config): TestResult = {
    val logger = config.logger // Get logger from the implicit config

    // 1. Log Start: Use bold style for the test name prefix
    logger.logStart(logger.bold(" " + name)) // Add space for visual separation

    // 2. Resolve Timeout: Use override if present, otherwise use config default
    val resolvedTimeout: Int = timeoutOverride.getOrElse(config.timeout)

    // 3. Execute Core Logic: Call the abstract method, passing a config
    //    with the *resolved* timeout. This ensures executeTest uses the correct limit.
    val result: TestResult = executeTest(using config.copy(timeout = resolvedTimeout))

    // 4. Log Result: Use the logger to print the formatted result message
    logger.logResult(result)
    logger.println() // Add a blank line after each test result for readability

    // 5. Flush Logger: Ensure output is visible immediately
    logger.flush()

    // Return the outcome
    result
  }

  /**
   * Abstract method containing the core logic specific to this type of test.
   * Subclasses must implement this method to perform the actual test evaluation
   * (e.g., evaluate an expression, compare values, check for exceptions) and
   * return the appropriate [[TestResult]].
   *
   * This method is called by the `run` method and receives the final configuration context,
   * including the *resolved* timeout value for this specific test execution.
   *
   * @param config The configuration context for this test run, including the actual timeout to use.
   * @return A [[TestResult]] (e.g., [[TestResult.Success]], [[TestResult.EqualityFailure]]) based on the evaluation.
   */
  protected def executeTest(using config: Config): TestResult
}
