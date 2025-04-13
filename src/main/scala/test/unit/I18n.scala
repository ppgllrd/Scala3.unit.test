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
      "but.expected" -> "but %s was expected", // Used for wrong type/message failures
      // --- Timeout Key ---
      // %1$s = Description of the overall expectation (e.g., "the exception IOException", "result to be 5")
      // %2$d = Timeout duration in seconds
      "timeout" -> "%s\n   timeout: test took more than %d seconds to complete",
      // --- Other Keys ---
      "unexpected.exception" -> "%s\n   raised unexpected exception %s with message %s", // %1$=original expectation, %2$=thrown type, %3$=thrown message
      "connector.or" -> " or ",
      "failed" -> "TEST FAILED!",
      "passed" -> "TEST PASSED SUCCESSFULLY!",
      "expected" -> "%s was expected", // %1$=expected value
      "expected.result" -> "expected result was %s", // %1$=expected value
      "obtained.result" -> "obtained result was %s", // %1$=actual value
      "no.exception.basic" -> "expected exception but none was thrown. %s was expected", // %1$=expected exception description
      "wrong.exception.type.basic" -> "test threw the exception %s", // %1$=actual thrown type
      "wrong.exception.message.basic" -> "test threw expected exception type %s but message was %s", // %1$=expected type, %2$=actual message
      "wrong.exception.and.message.basic" -> "test threw exception %s with message %s", // %1$=actual type, %2$=actual message
      "exception.description" -> "the exception %s", // %1$=type name(s)
      "exception.with.message.description" -> "the exception %s with message %s", // %1$=type name(s), %2$=exact message
      "exception.with.predicate.description" -> "the exception %s with message satisfying: %s", // %1$=type name(s), %2$=predicate help
      "exception.oneof.description" -> "one of exceptions %s", // %1$=type name list
      "exception.oneof.with.message.description" -> "one of exceptions %s with message %s", // %1$=type name list, %2$=exact message
      "exception.oneof.with.predicate.description" -> "one of exceptions %s with message satisfying: %s", // %1$=type name list, %2$=predicate help
      "exception.except.description" -> "any exception except %s", // %1$=excluded type name
      "exception.except.with.message.description" -> "any exception except %s, with message %s", // %1$=excluded type name, %2$=exact message
      "exception.except.with.predicate.description" -> "any exception except %s, with message satisfying: %s",  // %1$=excluded type name, %2$=predicate help
      "detail.expected_exact_message" -> "expected message was %s", // %1$=exact message detail
      "detail.expected_predicate" -> "message should satisfy: %s", // %1$=predicate help detail
      "property.failure.base" -> "does not verify expected property", // Base message for property failures
      "property.failure.suffix" -> ": %s", // Suffix added when property description is available, %1$=property description
      "property.must.be.true" -> "should be true", // Help text for Assert
      "property.must.be.false" -> "should be false", // Help text for Refute
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
      "but.expected" -> "pero se esperaba %s",
      // --- Timeout Key ---
      // %1$s = Description of the overall expectation (e.g., "the exception IOException", "result to be 5")
      // %2$d = Timeout duration in seconds
      "timeout" -> "%s\n   tiempo excedido: la prueba tardó más de %d segundos en completarse",
      // --- Other Keys ---
      "unexpected.exception" -> "%s\n   se lanzó la excepción inesperada %s con mensaje %s", // %1$=original expectation, %2$=thrown type, %3$=thrown message
      "connector.or" -> " o ",
      "failed" -> "¡PRUEBA FALLIDA!", // Mantenido en mayúsculas por énfasis/estado
      "passed" -> "¡PRUEBA SUPERADA CON ÉXITO!", // Mantenido en mayúsculas por énfasis/estado
      "expected" -> "%s se esperaba", // %1$=expected value
      "expected.result" -> "el resultado esperado era %s", // %1$=expected value
      "obtained.result" -> "el resultado obtenido fue %s", // %1$=actual value
      "no.exception.basic" -> "se esperaba una excepción pero no se lanzó ninguna. %s se esperaba", // %1$=expected exception description
      "wrong.exception.type.basic" -> "la prueba lanzó la excepción %s", // %1$=actual thrown type
      "wrong.exception.message.basic" -> "la prueba lanzó el tipo de excepción esperado %s pero el mensaje fue %s", // %1$=expected type, %2$=actual message
      "wrong.exception.and.message.basic" -> "la prueba lanzó la excepción %s con mensaje %s", // %1$=actual type, %2$=actual message
      "exception.description" -> "la excepción %s", // %1$=type name(s)
      "exception.with.message.description" -> "la excepción %s con mensaje %s", // %1$=type name(s), %2$=exact message
      "exception.with.predicate.description" -> "la excepción %s con mensaje satisfaciendo: %s", // %1$=type name(s), %2$=predicate help
      "exception.oneof.description" -> "una de las excepciones %s", // %1$=type name list
      "exception.oneof.with.message.description" -> "una de las excepciones %s con mensaje %s", // %1$=type name list, %2$=exact message
      "exception.oneof.with.predicate.description" -> "una de las excepciones %s con mensaje satisfaciendo: %s", // %1$=type name list, %2$=predicate help
      "exception.except.description" -> "cualquier excepción excepto %s", // %1$=excluded type name
      "exception.except.with.message.description" -> "cualquier excepción excepto %s, con mensaje %s", // %1$=excluded type name, %2$=exact message
      "exception.except.with.predicate.description" -> "cualquier excepción excepto %s, con mensaje satisfaciendo: %s",  // %1$=excluded type name, %2$=predicate help
      "detail.expected_exact_message" -> "se esperaba el mensaje %s", // %1$=exact message detail
      "detail.expected_predicate" -> "el mensaje debía satisfacer: %s", // %1$=predicate help detail
      "property.failure.base" -> "no verifica la propiedad esperada", // Base message for property failures
      "property.failure.suffix" -> ": %s", // Suffix added when property description is available, %1$=property description
      "property.must.be.true" -> "debe ser verdadera", // Help text for Assert (propiedad es femenino)
      "property.must.be.false" -> "debe ser falsa", // Help text for Refute (propiedad es femenino)
      "property.was.true" -> "la propiedad fue verdadera", // Result formatting for Assert/Refute failures
      "property.was.false" -> "la propiedad fue falsa", // Result formatting for Assert/Refute failures
      "suite.for" -> "Pruebas para %s", // %1$=suite name
      "results.passed" -> "Superadas", // (pruebas superadas)
      "results.failed" -> "Fallidas", // (pruebas fallidas)
      "results.total" -> "Total",
      "results.detail" -> "Detalle",
      "summary.tittle" -> "Resumen general",
      "summary.suites.run" -> "Suites ejecutadas: %d", // %1$=number of suites
      "summary.total.tests" -> "Total de pruebas: %d", // %1$=total tests
      "summary.success.rate" -> "Tasa de éxito: %.2f%%" // %1$=success rate percentage
    ),
    Language.French -> Map(
      "but.expected" -> "mais %s était attendu",
      // --- Timeout Key ---
      // %1$s = Description of the overall expectation (e.g., "the exception IOException", "result to be 5")
      // %2$d = Timeout duration in seconds
      "timeout" -> "%s\n   délai dépassé : le test a mis plus de %d secondes à se terminer",
      // --- Other Keys ---
      "unexpected.exception" -> "%s\n   a levé l'exception inattendue %s avec le message %s", // %1$=original expectation, %2$=thrown type, %3$=thrown message
      "connector.or" -> " ou ",
      "failed" -> "ÉCHEC DU TEST !", // Maintenu en majuscules pour l'emphase/le statut
      "passed" -> "TEST RÉUSSI AVEC SUCCÈS !", // Maintenu en majuscules pour l'emphase/le statut
      "expected" -> "%s était attendu", // %1$=expected value
      "expected.result" -> "le résultat attendu était %s", // %1$=expected value
      "obtained.result" -> "le résultat obtenu était %s", // %1$=actual value
      "no.exception.basic" -> "exception attendue mais aucune n'a été levée. %s était attendu", // %1$=expected exception description
      "wrong.exception.type.basic" -> "le test a levé l'exception %s", // %1$=actual thrown type
      "wrong.exception.message.basic" -> "le test a levé le type d'exception attendu %s mais le message était %s", // %1$=expected type, %2$=actual message
      "wrong.exception.and.message.basic" -> "le test a levé l'exception %s avec le message %s", // %1$=actual type, %2$=actual message
      "exception.description" -> "l'exception %s", // %1$=type name(s)
      "exception.with.message.description" -> "l'exception %s avec le message %s", // %1$=type name(s), %2$=exact message
      "exception.with.predicate.description" -> "l'exception %s avec message satisfaisant : %s", // %1$=type name(s), %2$=predicate help
      "exception.oneof.description" -> "une des exceptions %s", // %1$=type name list
      "exception.oneof.with.message.description" -> "une des exceptions %s avec le message %s", // %1$=type name list, %2$=exact message
      "exception.oneof.with.predicate.description" -> "une des exceptions %s avec message satisfaisant : %s", // %1$=type name list, %2$=predicate help
      "exception.except.description" -> "toute exception sauf %s", // %1$=excluded type name
      "exception.except.with.message.description" -> "toute exception sauf %s, avec le message %s", // %1$=excluded type name, %2$=exact message
      "exception.except.with.predicate.description" -> "toute exception sauf %s, avec message satisfaisant : %s",  // %1$=excluded type name, %2$=predicate help
      "detail.expected_exact_message" -> "le message attendu était %s", // %1$=exact message detail
      "detail.expected_predicate" -> "le message devait satisfaire : %s", // %1$=predicate help detail
      "property.failure.base" -> "ne vérifie pas la propriété attendue", // Base message for property failures
      "property.failure.suffix" -> " : %s", // Suffix added when property description is available, %1$=property description
      "property.must.be.true" -> "doit être vraie", // Help text for Assert (propriété is feminine)
      "property.must.be.false" -> "doit être fausse", // Help text for Refute (propriété is feminine)
      "property.was.true" -> "la propriété était vraie", // Result formatting for Assert/Refute failures
      "property.was.false" -> "la propriété était fausse", // Result formatting for Assert/Refute failures
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
