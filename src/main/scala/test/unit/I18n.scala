package test.unit

/**
 * Provides internationalization (I18n) support by storing and retrieving
 * localized message strings based on a key and a [[Language]].
 * Used by [[Config.msg]] to format messages for test output.
 *
 * @author Pepe Gallardo & Gemini
 */
object I18n:
  /** Internal storage mapping Language -> (Message Key -> Message Pattern). */
  private val messages: Map[Language, Map[String, String]] = Map(
    Language.English -> Map(
      "but.expected" -> "But %s was expected", // Used for wrong type/message failures
      // --- Timeout Key ---
      // %1$s = Description of the overall expectation (e.g., "the exception IOException", "result to be 5")
      // %2$d = Timeout duration in seconds
      "timeout" -> "%s\n   Timeout: test took more than %d seconds to complete",
      // --- Other Keys ---
      "unexpected.exception" -> "%s\n   Raised unexpected exception %s with message %s", // %1$=original expectation, %2$=thrown type, %3$=thrown message
      "connector.or" -> " or ",
      "failed" -> "TEST FAILED!",
      "passed" -> "TEST PASSED SUCCESSFULLY!",
      "expected" -> "%s was expected", // %1$=expected value
      "expected.result" -> "Expected result was: %s", // %1$=expected value
      "obtained.result" -> "Obtained result was: %s", // %1$=actual value
      "no.exception.basic" -> "Expected exception but none was thrown. %s was expected", // %1$=expected exception description
      "wrong.exception.type.basic" -> "Test threw exception %s", // %1$=actual thrown type
      "wrong.exception.message.basic" -> "Test threw expected exception type %s but message was %s", // %1$=expected type, %2$=actual message
      "wrong.exception.and.message.basic" -> "Test threw exception %s with message %s", // %1$=actual type, %2$=actual message
      "exception.description" -> "The exception %s", // %1$=type name(s)
      "exception.with.message.description" -> "The exception %s with message %s", // %1$=type name(s), %2$=exact message
      "exception.with.predicate.description" -> "The exception %s with message satisfying: %s", // %1$=type name(s), %2$=predicate help
      "exception.oneof.description" -> "One of exceptions %s", // %1$=type name list
      "exception.oneof.with.message.description" -> "One of exceptions %s with message %s", // %1$=type name list, %2$=exact message
      "exception.oneof.with.predicate.description" -> "One of exceptions %s with message satisfying: %s", // %1$=type name list, %2$=predicate help
      "exception.except.description" -> "Any exception except %s", // %1$=excluded type name
      "exception.except.with.message.description" -> "Any exception except %s, with message %s", // %1$=excluded type name, %2$=exact message
      "exception.except.with.predicate.description" -> "Any exception except %s, with message satisfying: %s",  // %1$=excluded type name, %2$=predicate help
      "detail.expected_exact_message" -> "Expected message was %s", // %1$=exact message detail
      "detail.expected_predicate" -> "Message should satisfy: %s", // %1$=predicate help detail
      "property.failure.base" -> "Does not verify expected property", // Base message for property failures
      "property.failure.suffix" -> ": %s", // Suffix added when property description is available, %1$=property description
      "property.must.be.true" -> "property should be true", // Help text for Assert
      "property.must.be.false" -> "property should be false", // Help text for Refute
      "property.was.true" -> "property was true", // Result formatting for Assert/Refute failures
      "property.was.false" -> "property was false", // Result formatting for Assert/Refute failures
      "suite.for" -> "Tests for %s", // %1$=suite name
      "results.passed" -> "Passed",
      "results.failed" -> "Failed",
      "results.total" -> "Total",
      "results.detail" -> "Detail",
      "summary.tittle" -> "Overall Summary",
      "summary.suites.run" -> "Suites run: %d", // %1$=number of suites
      "summary.total.tests" -> "Total tests: %d", // %1$=total tests
      "summary.success.rate" -> "Success rate: %.2f%%" // %1$=success rate percentage
    ),
    Language.Spanish -> Map(
      "but.expected" -> "Pero se esperaba %s",
      "timeout" -> "%s\n   Tiempo excedido: la prueba tardó más de %d segundos en completarse",
      "unexpected.exception" -> "%s\n   Se lanzó la excepción inesperada %s con mensaje %s",
      "connector.or" -> " o ",
      "failed" -> "¡PRUEBA FALLIDA!",
      "passed" -> "¡PRUEBA SUPERADA CON ÉXITO!",
      "expected" -> "%s se esperaba",
      "expected.result" -> "El resultado esperado era: %s",
      "obtained.result" -> "El resultado obtenido fue: %s",
      "no.exception.basic" -> "Se esperaba una excepción pero no se lanzó ninguna. %s se esperaba",
      "wrong.exception.type.basic" -> "La prueba lanzó la excepción %s",
      "wrong.exception.message.basic" -> "La prueba lanzó el tipo de excepción esperado %s pero con mensaje %s",
      "wrong.exception.and.message.basic" -> "La prueba lanzó la excepción %s con mensaje %s",
      "exception.description" -> "La excepción %s",
      "exception.with.message.description" -> "La excepción %s con mensaje %s",
      "exception.with.predicate.description" -> "La excepción %s con mensaje satisfaciendo: %s",
      "exception.oneof.description" -> "Una de las excepciones %s",
      "exception.oneof.with.message.description" -> "Una de las excepciones %s con mensaje %s",
      "exception.oneof.with.predicate.description" -> "Una de las excepciones %s con mensaje satisfaciendo: %s",
      "exception.except.description" -> "Cualquier excepción excepto %s",
      "exception.except.with.message.description" -> "Cualquier excepción excepto %s, con mensaje %s",
      "exception.except.with.predicate.description" -> "Cualquier excepción excepto %s, con mensaje satisfaciendo: %s",
      "detail.expected_exact_message" -> "Se esperaba el mensaje %s",
      "detail.expected_predicate" -> "Se esperaba un mensaje satisfaciendo: %s",
      "property.failure.base" -> "No verifica la propiedad esperada",
      "property.failure.suffix" -> ": %s",
      "property.must.be.true" -> "la propiedad debe ser verdadera",
      "property.must.be.false" -> "la propiedad debe ser falsa",
      "property.was.true" -> "la propiedad fue verdadera",
      "property.was.false" -> "la propiedad fue falsa",
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
       "but.expected" -> "Mais %s était attendu",
       "timeout" -> "%s\n   Délai dépassé: le test a mis plus de %d secondes à se terminer",
       "unexpected.exception" -> "%s\n   L''exception inattendue %s a été levée avec le message %s",
       "connector.or" -> " ou ",
       "failed" -> "ÉCHEC DU TEST!",
       "passed" -> "TEST RÉUSSI AVEC SUCCÈS!",
       "expected" -> "%s était attendu",
       "expected.result" -> "Le résultat attendu était: %s",
       "obtained.result" -> "Le résultat obtenu était: %s",
       "no.exception.basic" -> "Exception attendue mais aucune n''a été lancée. %s était attendu",
       "wrong.exception.type.basic" -> "Le test a lancé l''exception %s",
       "wrong.exception.message.basic" -> "Le test a lancé le type d''exception attendu %s mais le message était %s",
       "wrong.exception.and.message.basic" -> "Le test a lancé l''exception %s avec le message %s",
       "exception.description" -> "L''exception %s",
       "exception.with.message.description" -> "L''exception %s avec le message %s",
       "exception.with.predicate.description" -> "L''exception %s avec message satisfaisant : %s",
       "exception.oneof.description" -> "Une des exceptions %s",
       "exception.oneof.with.message.description" -> "Une des exceptions %s avec le message %s",
       "exception.oneof.with.predicate.description" -> "Une des exceptions %s avec message satisfaisant : %s",
       "exception.except.description" -> "Toute exception sauf %s",
       "exception.except.with.message.description" -> "Toute exception sauf %s, avec le message %s",
       "exception.except.with.predicate.description" -> "Toute exception sauf %s, avec message satisfaisant : %s",
       "detail.expected_exact_message" -> "Message attendu : %s",
       "detail.expected_predicate" -> "Attendu message satisfaisant : %s",
       "property.failure.base" -> "Ne vérifie pas la propriété attendue",
       "property.failure.suffix" -> " : %s",
       "property.must.be.true" -> "la propriété doit être vraie",
       "property.must.be.false" -> "la propriété doit être fausse",
       "property.was.true" -> "la propriété était vraie",
       "property.was.false" -> "la propriété était fausse",
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
   * Retrieves the message pattern associated with the given key for the specified language.
   * If the key or language is not found, it falls back to English. If the key is still
   * not found in English, the key itself is returned.
   *
   * @param key The key identifying the desired message pattern.
   * @param language The target [[Language]].
   * @return The localized message pattern string, or the key if not found.
   */
  def getMessage(key: String, language: Language): String =
    // Get the map for the requested language, falling back to English if not found.
    val langMap = messages.getOrElse(language, messages(Language.English))
    // Get the message for the key from the selected map, falling back to the key itself if not found.
    langMap.getOrElse(key, key)
