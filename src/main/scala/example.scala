import test.unit.{Config, TestFactory, TestSuite} // Still need Config

// Define your configuration (or use Config.Default)
given config: Config = Config.Default.copy(language = test.unit.Language.English)
// Or: given config: Config = Config.Default

// Create tests using the factory methods
val eqTest = TestFactory.equal("Addition", 1 + 1, 2)

val propTest = TestFactory.property[Int](
  "Positive Result",
  5 * 5,
  _ > 0,
)

val assertTest = TestFactory.assertTest("Truthiness", 1 < 2)

val exTest = TestFactory.expectException[Int, ArithmeticException](
  "Division by zero",
  1 / 0
)

val exExceptTest = TestFactory.expectExceptionExcept[Nothing, NullPointerException](
  "Non-NPE Exception",
  throw new IllegalArgumentException("Wrong argument")
)

// Then use these tests in a TestSuite
// TestSuite("My Tests", eqTest, propTest, assertTest, exTest, exExceptTest).run() // runAll requires explicit config now
@main def main(): Unit =
  // Run all tests in a suite
  TestSuite.runAll(config, TestSuite("My Tests", eqTest, propTest, assertTest, exTest, exExceptTest))
