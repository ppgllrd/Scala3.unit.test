import test.unit.* // Import necessary components

object MainExample:

  def main(args: Array[String]): Unit =

    // 1. Configure the test runner (optional, defaults exist)
    // Create an immutable configuration using English (default, but explicit here).
    val englishConfig: Config = Config.Default.copy(
      language = Language.English, // Ensure English messages
      timeout = 5 // Increase default timeout slightly to 5 seconds
    )

    // Make the config available implicitly for test creation and suite execution
    given config: Config = englishConfig

    // 2. Create Tests using TestFactory (implicit config is picked up)

    // --- Equality Tests ---
    val testAddPass = TestFactory.equal("Correct Sum", 2 + 3, 5)
    val testAddFail = TestFactory.equal("Incorrect Sum", 2 + 3, 6) // Fails

    // Custom Equality (case-insensitive string comparison)
    val testStringEqPass = TestFactory.equalBy[String](
      "String Equality (Ignore Case)",
      "Scala Rocks", // Actual
      "scala rocks", // Expected
      (s1, s2) => s1.equalsIgnoreCase(s2) // Custom equals
    )

    // --- Property Tests ---
    val testListNotEmpty = TestFactory.property[List[Int]](
      "Non-Empty List",
      List(1, 2, 3),
      property = _.nonEmpty,
      help = Some("The generated list should not be empty") // User-provided help
    )

    val testPositiveSum = TestFactory.property[Int](
      "Positive Sum",
      List(5, 10, -2).sum,
      property = _ > 0,
      mkString = i => s"The sum was $i" // Custom formatting for result
    )

    val testEvenFail = TestFactory.property[Int](
      "Should Be Even",
      7,
      property = _ % 2 == 0,
      help = Some("The resulting number must be divisible by 2") // Fails
    )

    // --- Assert/Refute Tests ---
    val testAssertTrue = TestFactory.assertTest("True Assertion", 5 > 1)
    val testAssertFail = TestFactory.assertTest("False Assertion", List.empty == List(1)) // Fails

    val testRefuteFalse = TestFactory.refuteTest("Correct Refutation", 10 == 20)
    val testRefuteFail = TestFactory.refuteTest("Incorrect Refutation", 10 < 20) // Fails (because 10 < 20 is true)

    // --- Exception Tests ---
    val testArithmeticEx = TestFactory.expectException[Int, ArithmeticException]( 
      "Exception: Division by Zero",
      1 / 0
    )

    val testExWithMessage = TestFactory.expectException[Unit, IllegalArgumentException](
      "Exception: Specific Message",
      throw new IllegalArgumentException("Invalid value"),
      expectedMessage = "Invalid value" // Check message content
    )

    val testExNoThrow = TestFactory.expectException[Int, RuntimeException](
      "Exception: Not Thrown",
      42 // Doesn't throw, will fail.
    )

    val testExWrongType = TestFactory.expectException[Int, NullPointerException]( 
      "Exception: Wrong Type",
      1 / 0 // Throws ArithmeticException, will fail.
    )

    val testExWrongMessage = TestFactory.expectException[Nothing, IllegalArgumentException](
      "Exception: Wrong Message",
      throw new IllegalArgumentException("Another message"),
      expectedMessage = "Invalid value" // Fails on message check
    )

    // --- ExceptionExcept Tests ---
    val testExExceptPass = TestFactory.expectExceptionExcept[Nothing, NullPointerException](
      "Exception: Anything But NullPointerException",
      throw new RuntimeException("Something happened") // Throws RuntimeException, passes.
    )

    val testExExceptFail = TestFactory.expectExceptionExcept[Nothing, IllegalArgumentException](
      "Exception: Should Not Be IllegalArgument",
      throw new IllegalArgumentException("Thrown anyway") // Throws the excluded type, fails.
    )

    // Example using the specific factory for "not NotImplementedError"
    val testAnyButNIE = TestFactory.anyExceptionButNotImplementedError[Int](
      "Exception: Anything But Not Implemented",
      { if (true) throw new ArithmeticException("div zero") else ??? }
    )

    // --- Timeout Test ---
    val testTimeoutFail = TestFactory.equal[Int](
      "Timeout Exceeded",
      { Thread.sleep(6000); 1 }, // Sleeps 6s, default timeout is 5s.
      1
      // timeoutOverride = Some(2) // Can also override specific test timeout
    ) // Fails due to timeout


    // 3. Create a Test Suite
    val mainSuite = TestSuite(
      "Main Example Suite", // Suite Name
      // Add all created tests
      testAddPass,
      testAddFail,
      testStringEqPass,
      testListNotEmpty,
      testPositiveSum,
      testEvenFail,
      testAssertTrue,
      testAssertFail,
      testRefuteFalse,
      testRefuteFail,
      testArithmeticEx,
      testExWithMessage,
      testExNoThrow,
      testExWrongType,
      testExWrongMessage,
      testExExceptPass,
      testExExceptFail,
      testAnyButNIE,
      testTimeoutFail
    ) // Implicit config is passed to TestSuite constructor


    // 4. Run the Test Suite
    println("Running tests with English configuration...")
    // Run the suite(s) - runAll requires the config explicitly
    // The results object and summary will use the English messages from the config
    TestSuite.runAll(mainSuite)(using config)

    println("\n" + "="*50 + "\n")

    // Example with a different config (e.g., silent logger)
    println("Running tests with Silent Logger configuration...")
    // Create a config with a silent logger
    val silentConfig: Config = englishConfig.copy(logger = SilentLogger())
    // Recreate suite or use existing tests; pass the new config to runAll
    // We'll run the same suite definition but the output will be suppressed
    val resultsSilent = TestSuite.runAll(mainSuite)(using silentConfig)
    println(s"(Silent run completed with ${resultsSilent.head.getPassed}/${resultsSilent.head.getTotal} passed)")


    println("\nEnd of tests.")

