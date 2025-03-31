package test.unit

import java.util.concurrent.{CompletableFuture, ExecutionException, TimeUnit, TimeoutException}

/**
 * Abstract base class for a single, executable test case.
 * Defines the common structure for running a test and handling timeouts.
 * A test instance holds its definition (name, logic, optional timeout override).
 * It requires a `Config` object when executed via the `run` method.
 *
 * @author Pepe Gallardo
 */
abstract class Test {
  /** The name of this test case. */
  val name: String

  /**
   * An optional timeout duration (in seconds) specific to this test.
   * If `None`, the `defaultTimeout` from the `Config` provided at runtime will be used.
   */
  protected val timeoutOverride: Option[Int]

  /**
   * Executes the test case using the provided configuration.
   * Handles logging, timeout calculation, and calls the specific `executeTest` logic.
   *
   * @param config The configuration (logger, language, default timeout) for this run.
   * @return A `TestResult` (enum case) indicating the outcome of the test.
   */
  final def run()(using config: Config): TestResult = {
    val logger = config.logger // Get logger from the provided config

    // Log the start of the test
    logger.logStart(logger.bold(" " + name))

    // Calculate the actual timeout for this run
    val resolvedTimeout: Int = timeoutOverride.getOrElse(config.timeout)

    // Execute the specific test logic
    val result: TestResult = executeTest(using config.copy(timeout = resolvedTimeout))

    // Log the final result
    logger.logResult(result)
    logger.println() // Add a new line for better readability
    logger.flush() // Ensure output is visible

    result // Return the result enum case
  }

  /**
   * Abstract method to be implemented by concrete test subclasses.
   * Contains the core logic for evaluating the test's condition.
   * It receives the resolved timeout and the configuration context.
   *
   * @param config The configuration context for this test run.
   * @return A `TestResult` (enum case) based on the evaluation.
   */
  protected def executeTest(using config: Config): TestResult
}
