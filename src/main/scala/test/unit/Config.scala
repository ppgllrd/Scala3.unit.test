package test.unit

/**
 * Configuration for running tests.
 * Specifies the logger, language for messages, and default timeout.
 * This object is passed to the `run` methods of `Test` and `TestSuite`.
 *
 * @param logger The logger implementation to use for output.
 * @param language The language for localizing test messages.
 * @param timeout The default timeout in seconds for tests that don't specify an override.
 */
case class Config(
  logger: Logger = AnsiConsoleLogger(),
  language: Language = Language.English,
  timeout: Int = 3
):
  /**
   * Retrieves a localized message pattern for the given key and language,
   * and formats it with the provided arguments.
   *
   * @param key The localization key (e.g., "test.passed").
   * @param args The arguments to substitute into the message pattern.
   * @return The formatted, localized message string.
   *         Returns an error string if formatting fails.
   */
  def msg(key: String, args: Any*): String =
    val pattern = I18n.getMessage(key, language)
    if args.isEmpty then pattern
    else try {
      // Use ROOT locale for consistent formatting regardless of system locale
      String.format(java.util.Locale.ROOT, pattern, args.map(_.asInstanceOf[Object]): _*)
    } catch {
      case e: java.util.MissingFormatArgumentException =>
        s"ERROR: Formatting error for key '$key' [${language.toString.toLowerCase}]: ${e.getMessage}. Pattern: '$pattern', Args: ${args.mkString("[", ", ", "]")}"
      case e: java.lang.Exception =>
        s"ERROR: Generic formatting error for key '$key' [${language.toString.toLowerCase}]: ${e.getMessage}. Pattern: '$pattern', Args: ${args.mkString("[", ", ", "]")}"
    }

/**
 * Companion object for [[Config]].
 * Provides default configurations and convenience factories.
 */
object Config:
  /** A default configuration instance (AnsiConsoleLogger, English, 3 seconds timeout). */
  val Default: Config = Config()

  /**
   * Creates a new Config instance based on an existing one, but with
   * logging settings adjusted.
   *
   * @param logging If false, uses `SilentLogger`. If true, uses ANSI or plain console logger.
   * @param useAnsi If true and `logging` is true, uses `AnsiConsoleLogger`. Otherwise uses `ConsoleLogger`.
   * @param baseConfig The configuration to base the new one on. Defaults to `Config.Default`.
   * @return A new `Config` instance with the specified logging setup.
   */
  def withLogging(
    logging: Boolean,
    useAnsi: Boolean = true,
    baseConfig: Config = Default
  ): Config =
    if (!logging)
      baseConfig.copy(logger = SilentLogger())
    else if (useAnsi)
      baseConfig.copy(logger = AnsiConsoleLogger())
    else
      baseConfig.copy(logger = ConsoleLogger())
