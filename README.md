# Scala 3 Unit Test Framework

A lightweight, feature-rich unit testing framework for Scala 3, providing intuitive APIs for writing and running tests with comprehensive reporting.

## Features

- **Multiple Test Types**: Equality tests, property tests, assertion tests, and exception tests
- **Flexible Configuration**: Customizable timeouts, loggers, and internationalization (English, Spanish, French)
- **Rich Exception Testing**: Test for specific exceptions, multiple exception types, or any exception except specific ones
- **ANSI Color Support**: Optional colored console output for better readability
- **Timeout Management**: Global and per-test timeout configuration
- **Localized Messages**: Support for multiple languages in test output

## Quick Start

### Basic Test Example

```scala
import test.unit.*

// Define configuration
given config: Config = Config.Default

// Create and run a test suite
TestSuite.runAll(
  TestSuite("My Tests",
    TestFactory.equal("Addition", 2 + 2, 4),
    TestFactory.assertTest("True Assertion", 5 > 1),
    TestFactory.expectException[Int, ArithmeticException](
      "Division by zero",
      1 / 0
    )
  )
)
```

## Test Types

### Equality Tests

Test if two values are equal:

```scala
TestFactory.equal("Addition Test", 1 + 1, 2)

// With custom equality function
TestFactory.equalBy[String](
  "Case Insensitive",
  "Scala",
  "scala",
  (s1, s2) => s1.equalsIgnoreCase(s2)
)
```

### Property Tests

Test if a value satisfies a predicate:

```scala
TestFactory.property[Int](
  "Positive Result",
  5 * 5,
  _ > 0,
  help = Some("result should be positive")
)
```

### Assertion Tests

Test if a boolean expression is true:

```scala
TestFactory.assertTest("True Test", 10 > 5)
```

### Refutation Tests

Test if a boolean expression is false:

```scala
TestFactory.refuteTest("False Test", 5 > 10)
```

### Exception Tests

Test if code throws a specific exception:

```scala
// Specific exception type
TestFactory.expectException[Int, ArithmeticException](
  "Division by Zero",
  1 / 0
)

// With exact message check
TestFactory.expectException[Unit, IllegalArgumentException](
  "Specific Message",
  throw new IllegalArgumentException("Invalid value"),
  expectedMessage = "Invalid value"
)

// One of multiple exception types
TestFactory.expectExceptionOneOf[String](
  "IO or SQL Exception",
  throwSomething()
)(implicitly[ClassTag[IOException]], implicitly[ClassTag[SQLException]])

// Any exception except specific type
TestFactory.expectExceptionExcept[String, NullPointerException](
  "Not NPE",
  throw new RuntimeException("Error")
)

// Any exception except NotImplementedError
TestFactory.anyExceptionButNotImplementedError[Int](
  "Not Not Implemented",
  throw new ArithmeticException("Error")
)
```

## Configuration

### Custom Configuration

```scala
val myConfig = Config(
  logger = AnsiConsoleLogger(),
  language = Language.English,
  timeout = 5 // seconds
)
given config: Config = myConfig
```

### Logger Options

- `AnsiConsoleLogger()`: Colored console output
- `ConsoleLogger()`: Plain console output
- `SilentLogger()`: No output

### Language Options

- `Language.English`
- `Language.Spanish`
- `Language.French`

### Timeout Configuration

```scala
// Global timeout in config
val config = Config.Default.copy(timeout = 10)

// Per-test timeout override
TestFactory.equal("Slow Test", slowOperation(), expected, timeoutOverride = Some(30))
```

## Running Tests

### Single Test Suite

```scala
val suite = TestSuite("My Suite", test1, test2, test3)
suite.run()(using config)
```

### Multiple Test Suites

```scala
TestSuite.runAll(suite1, suite2, suite3)(using config)
```

## Test Results

The framework provides detailed results including:
- Number of tests passed/failed
- Individual test outcomes
- Success rate
- Colored output (when enabled)

Example output:
```
Tests for Example Suite
========================

 Test 1: PASSED
 Test 2: FAILED
   expected result was 5
   obtained result was 4

Passed: 1, Failed: 1, Total: 2, Detail: +-
```

## Project Structure

```
src/main/scala/
  Example.scala              # Basic example
  MainExample.scala          # Comprehensive example
  ExceptionTests.scala       # Exception testing examples
  ExceptionTestSuite.scala   # Exception failure scenarios
  test/unit/
    Test.scala               # Base test class
    TestSuite.scala          # Test suite management
    TestFactory.scala        # Factory methods for test creation
    Config.scala             # Configuration
    Logger.scala             # Logging implementations
    TestResult.scala         # Test result types
    Results.scala            # Aggregated results
    I18n.scala               # Internationalization
    Equal.scala              # Equality tests
    EqualBy.scala            # Custom equality tests
    Property.scala           # Property tests
    Assert.scala             # Assertion tests
    Refute.scala             # Refutation tests
    Exception.scala          # Single exception tests
    ExceptionOneOf.scala     # Multiple exception tests
    ExceptionExcept.scala    # Exception exclusion tests
    ExceptionBy.scala        # Base exception test
    AnyExceptionButNotImplementedError.scala
    HelpArg.scala            # Help argument types
    Language.scala           # Language enumeration
    AnsiColor.scala          # ANSI color utilities
```

## Building

This project uses sbt:

```bash
sbt compile
sbt run
```

## Examples

See the included example files:
- `Example.scala`: Basic usage
- `MainExample.scala`: Comprehensive examples
- `ExceptionTests.scala`: Exception handling
- `ExceptionTestSuite.scala`: Exception failure scenarios

## Contributing

Contributions are welcome! Please feel free to submit issues or pull requests.

## Author

Pepe Gallardo & Gemini

## Version

0.1.0-SNAPSHOT
