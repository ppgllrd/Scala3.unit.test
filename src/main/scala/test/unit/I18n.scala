package test.unit

/**
 * Handles internationalization (i18n) by providing localized message patterns.
 * Used by [[Config.msg]] to retrieve language-specific strings.
 */
object I18n:
  // Stores message patterns keyed by Language, then by message key.
  private val messages: Map[Language, Map[String, String]] = Map(
    Language.English -> Map(
      "failed" -> "TEST FAILED!",
      "passed" -> "TEST PASSED SUCCESSFULLY!",
      "property.failure" -> "Does not verify expected property%s",  // %s for optional help detail (e.g., ": must be positive")
      "property.must.be.true" -> "property should be true",         // Used in Assert help message generation
      "property.must.be.false" -> "property should be false",       // Used in Refute help message generation
      "property.was.true" -> "property was true",                   // Used in Assert/Refute mkString generation
      "property.was.false" -> "property was false",                 // Used in Assert/Refute mkString generation
      "expected" -> "Expected result was: %s",                      // Used in EqualityFailure message, EqualBy description
      "obtained" -> "Obtained result was: %s",                      // Used in EqualityFailure, PropertyFailure, NoExceptionFailure messages
      // --- Exception Failure Message Keys ---
      "no.exception.basic" -> "Expected exception but none was thrown. The expected exception was %s", // %s is the colored expected description
      "wrong.exception.type.basic" -> "Test threw exception %s",    // %s is the colored thrown type name
      "wrong.exception.message.basic" -> "Test threw expected exception %s but message was %s", // %1$s colored thrown type, %2$s colored thrown message
      "wrong.exception.and.message.basic" -> "Test threw exception %s with message %s", // %1$s colored thrown type, %2$s colored thrown message
      "but.expected" -> "But %s was expected",                      // %s is the colored expected description (appended to wrong type/message failures)
      // --- Exception Description Keys (used by ExceptionBy subclasses to build the 'formattedHelp' string) ---
      "exception.description" -> "%s",                              // %s is the colored type name (e.g., "RuntimeException")
      "exception.not.implemented.error" -> "NotImplementedError",   // Specific type name (can be overridden if needed, but good default)
      "exception.except.description" -> "Any exception except %s",  // %s is the colored excluded type name (e.g., "Any exception except IOException")
      "exception.with.message.description" -> "%s with message %s", // %1$s colored type, %2$s colored message (e.g., "IllegalArgumentException with message "Invalid value"")
      // --- Other Message Keys ---
      "timeout" -> "%s\n   Timeout: test took more than %d seconds to complete", // %1$s description of what was expected, %2$d seconds
      "unexpected.exception" -> "%s\n   Raised unexpected exception %s with message %s", // %1$s description, %2$s colored thrown type, %3$s colored thrown message
      "suite.for" -> "Tests for %s",                                // %s is suite name
      "results.passed" -> "Passed",
      "results.failed" -> "Failed",
      "results.total" -> "Total",
      "results.detail" -> "Detail", 
      "summary.tittle" -> "Overall Summary",
      "summary.suites.run" -> "Suites run: %d",                     // %d is the number of suites
      "summary.total.tests" -> "Total tests: %d",                   // %d is the number of tests
      "summary.success.rate" -> "Success rate: %.2f%%"              // %.2f is the success rate percentage
    ),
    Language.Spanish -> Map(
      // ... (Spanish translations - same keys as English)
      "failed" -> "¡PRUEBA FALLIDA!",
      "passed" -> "¡PRUEBA SUPERADA CON ÉXITO!",
      "property.failure" -> "No verifica la propiedad esperada%s",
      "property.must.be.true" -> "la propiedad debe ser verdadera",
      "property.must.be.false" -> "la propiedad debe ser falsa",
      "property.was.true" -> "la propiedad fue verdadera",
      "property.was.false" -> "la propiedad fue falsa",
      "expected" -> "El resultado esperado era: %s",
      "obtained" -> "El resultado obtenido fue: %s",
      "no.exception.basic" -> "Se esperaba una excepción pero no se lanzó ninguna. La excepción esperada era %s",
      "wrong.exception.type.basic" -> "La prueba lanzó la excepción %s",
      "wrong.exception.message.basic" -> "La prueba lanzó la excepción esperada %s pero con mensaje %s",
      "wrong.exception.and.message.basic" -> "La prueba lanzó la excepción %s con mensaje %s",
      "but.expected" -> "Pero se esperaba %s",
      "exception.description" -> "%s",
      "exception.not.implemented.error" -> "NotImplementedError",
      "exception.except.description" -> "Cualquier excepción excepto %s",
      "exception.with.message.description" -> "%s con mensaje %s",
      "timeout" -> "%s\n   Tiempo excedido: la prueba tardó más de %d segundos en completarse",
      "unexpected.exception" -> "%s\n   Se lanzó la excepción inesperada %s con mensaje %s",
      "suite.for" -> "Pruebas para %s",
      "results.passed" -> "Superadas",
      "results.failed" -> "Fallidas",
      "results.total" -> "Total",
      "results.detail" -> "Detalle",
      "summary.tittle" -> "Resumen General",
      "summary.suites.run" -> "Suites ejecutadas: %d",
      "summary.total.tests" -> "Total de pruebas: %d",
      "summary.success.rate" -> "Tasa de éxito: %.2f%%" 
    ),
    Language.French -> Map(
      // ... (French translations - same keys as English)
       "failed" -> "ÉCHEC DU TEST!",
       "passed" -> "TEST RÉUSSI AVEC SUCCÈS!",
       "property.failure" -> "Ne vérifie pas la propriété attendue%s",
       "property.must.be.true" -> "la propriété doit être vraie",
       "property.must.be.false" -> "la propriété doit être fausse",
       "property.was.true" -> "la propriété était vraie",
       "property.was.false" -> "la propriété était fausse",
       "obtained" -> "Le résultat obtenu était: %s",
       "expected" -> "Le résultat attendu était: %s",
       "no.exception.basic" -> "Exception attendue mais aucune n'a été lancée. L'exception attendue était %s",
       "wrong.exception.type.basic" -> "Le test a lancé l'exception %s",
       "wrong.exception.message.basic" -> "Le test a lancé l'exception attendue %s mais le message était %s",
       "wrong.exception.and.message.basic" -> "Le test a lancé l'exception %s avec le message %s",
       "but.expected" -> "Mais %s était attendu",
       "exception.description" -> "%s",
       "exception.not.implemented.error" -> "NotImplementedError",
       "exception.except.description" -> "Toute exception sauf %s",
       "exception.with.message.description" -> "%s avec le message %s",
       "timeout" -> "%s\n   Délai dépassé: le test a mis plus de %d secondes à se terminer",
       "unexpected.exception" -> "%s\n   L'exception inattendue %s a été levée avec le message %s",
       "suite.for" -> "Tests pour %s",
       "results.passed" -> "Réussis",
       "results.failed" -> "Échoués",
       "results.total" -> "Total",
       "results.detail" -> "Détail",
       "summary.tittle" -> "Résumé Général",
       "summary.suites.run" -> "Suites exécutées: %d",
       "summary.total.tests" -> "Total des tests: %d",
       "summary.success.rate" -> "Taux de réussite: %.2f%%" 
    )
  )

  /**
   * Gets the localized message pattern for a given key and language.
   * Falls back to English if the specified language or the key within that language is not found.
   *
   * @param key The message key (e.g., "test.failed").
   * @param language The desired [[Language]].
   * @return The localized message pattern string. Returns the `key` itself if not found even in English.
   */
  def getMessage(key: String, language: Language): String =
    messages.getOrElse(language, messages(Language.English)).getOrElse(key, key) // Fallback chain: Specific Lang -> English -> Key

