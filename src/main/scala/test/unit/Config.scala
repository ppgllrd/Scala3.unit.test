package test.unit

/**
 * Holds configuration settings for running tests, including the logger,
 * language for messages, and the default timeout duration.
 * An instance of `Config` is typically passed to the `run` methods of [[Test]] and [[TestSuite]].
 *
 * @param logger The [[Logger]] implementation to use for outputting test progress and results.
 * @param language The [[Language]] used for localizing messages from the [[I18n]] resource bundle.
 * @param timeout The default timeout duration in seconds for individual tests, used when a test doesn't specify its own `timeoutOverride`.
 * @author Pepe Gallardo & Gemini
 */
case class Config(
  logger: Logger = AnsiConsoleLogger(),
  language: Language = Language.English,
  timeout: Int = 3
):
  /**
   * Retrieves a localized message pattern for the given key and language,
   * then formats it using the provided arguments.
   *
   * It uses `java.lang.String.format` with the `ROOT` locale for consistent
   * formatting behavior regardless of the system's default locale.
   *
   * @param key The key identifying the message pattern in the [[I18n]] resource bundle (e.g., "test.passed").
   * @param args The arguments to be substituted into the message pattern placeholders (e.g., %s, %d).
   * @return The formatted, localized message string.
   *         If a formatting error occurs (e.g., missing arguments), an error message string is returned.
   */
  def msg(key: String, args: Any*): String =
    val pattern = I18n.getMessage(key = key, language = language)
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
 * Companion object for the [[Config]] case class.
 * Provides a default configuration instance and factory methods for creating
 * configurations with specific logging settings.
 *
 * @author Pepe Gallardo & Gemini
 */
object Config:
  /** A default configuration using `AnsiConsoleLogger`, `English` language, and a 3-second timeout. */
  val Default: Config = Config()

  /**
   * Creates a new `Config` instance based on an existing one, but with potentially modified logging settings.
   *
   * @param logging If `false`, the logger is set to [[SilentLogger]], disabling all output.
   *                If `true`, a console logger is used.
   * @param useAnsi If `logging` is `true`, this determines whether to use [[AnsiConsoleLogger]] (if `true`)
   *                or [[ConsoleLogger]] (if `false`).
   * @param baseConfig The `Config` instance to use as a base. Defaults to [[Config.Default]].
   * @return A new `Config` instance with the specified logger, inheriting other settings from `baseConfig`.
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
