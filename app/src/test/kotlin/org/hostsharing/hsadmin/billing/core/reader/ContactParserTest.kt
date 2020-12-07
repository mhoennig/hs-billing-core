package org.hostsharing.hsadmin.billing.core.reader

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.hostsharing.hsadmin.billing.core.lib.ContextException
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

internal class ContactParserTest {

    private val recordWithAllFieldValues: Map<String, String?> = mapOf(
        "company" to "Testmann GmbH",
        "salutation" to "Herr",
        "title" to "Dr.",
        "firstName" to "Tästi",
        "lastName" to "Tästmann",
        "co" to "Tästmann Holdings AG",
        "street" to "Teststraße 42",
        "zipCode" to "20144",
        "city" to "Hamburg",
        "country" to "Germany",
        "countryCode" to "DE",
        "email" to "taesti@taestmann.de")

    @Test
    fun `will create contact from record with all related fields set and valid`() {
        val givenRecord = recordWithAllFieldValues

        val actual = ContactParser.parse("contact", givenRecord)

        assertThat(actual.formatted()).isEqualTo("""
            company="Testmann GmbH"
            salutation="Herr"
            title="Dr."
            firstName="Tästi"
            lastName="Tästmann"
            co="Tästmann Holdings AG"
            street="Teststraße 42"
            zipCode="20144"
            city="Hamburg"
            country="Germany"
            countryCode="DE"
            email="taesti@taestmann.de"
            """.trimIndent())
    }

    @Test
    fun `will create contact from record without all optional field values null`() {
        val givenRecord = mapOf(
            "company" to null,
            "salutation" to "Herr",
            "title" to null,
            "firstName" to "Tästi",
            "lastName" to "Tästmann",
            "co" to null,
            "street" to "Teststraße 42",
            "zipCode" to "20144",
            "city" to "Hamburg",
            "country" to "Germany",
            "countryCode" to "DE",
            "email" to "taesti@taestmann.de")

        val actual = ContactParser.parse("contact", givenRecord)

        assertThat(actual.formatted()).isEqualTo("""
            company=null
            salutation="Herr"
            title=null
            firstName="Tästi"
            lastName="Tästmann"
            co=null
            street="Teststraße 42"
            zipCode="20144"
            city="Hamburg"
            country="Germany"
            countryCode="DE"
            email="taesti@taestmann.de"
            """.trimIndent())
    }

    @ParameterizedTest
    @ValueSource(strings = ["salutation", "firstName", "lastName", "street", "zipCode", "city", "country", "countryCode", "email"])
    fun `will throw error when parsing contact from record without mandatory field`(fieldName: String) {
        val givenRecord = recordWithAllFieldValues.toMutableMap().also {
            it.put(fieldName, null)
        }

        val actualException = org.junit.jupiter.api.assertThrows<ContextException> {
            ContactParser.parse("contact", givenRecord)
        }

        assertThat(actualException.message).isEqualTo("""
            contact without $fieldName
            - in parsing contact $givenRecord
            """.trimIndent())
    }

    @Test
    fun `will throw error when parsing contact from record with invalid country code`() {
        val givenRecord = recordWithAllFieldValues.toMutableMap().also {
            it.put("countryCode", "X")
        }

        val actualException = org.junit.jupiter.api.assertThrows<ContextException> {
            ContactParser.parse("contact", givenRecord)
        }

        assertThat(actualException.message).isEqualTo("""
            contact with countryCode='X' not a valid country code
            - in parsing contact $givenRecord
            """.trimIndent())
    }
}
