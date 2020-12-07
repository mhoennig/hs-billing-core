package org.hostsharing.hsadmin.billing.core.reader

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.hostsharing.hsadmin.billing.core.lib.ContextException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class SepaParserTest {

    @Test
    fun `will create SEPA data from record with all related fields set and valid`() {
        val givenRecord = mapOf(
            "directDebiting" to "true",
            "bankCustomer" to "Tästmann GmbH",
            "bankIBAN" to "DE987654321",
            "bankBIC" to "GENODEF1HH2",
            "mandatRef" to "HS-10001-20140801",
        )

        val actual = SepaParser.parse(givenRecord)

        assertThat(actual.formatted()).isEqualTo("""
            directDebiting="true"
            bankCustomer="Tästmann GmbH"
            bankIBAN="DE987654321"
            bankBIC="GENODEF1HH2"
            mandatRef="HS-10001-20140801"
            """.trimIndent())
    }

    @Test
    fun `will create SEPA data from record with directDebiting disabled and all other fields null`() {
        val givenRecord = mapOf(
            "directDebiting" to "false",
            "bankCustomer" to null,
            "bankIBAN" to null,
            "bankBIC" to null,
            "mandatRef" to null,
        )

        val actual = SepaParser.parse(givenRecord)

        assertThat(actual.formatted()).isEqualTo("""
            directDebiting="false"
            bankCustomer=null
            bankIBAN=null
            bankBIC=null
            mandatRef=null
            """.trimIndent())
    }

    @Test
    fun `will throw error when parsing SEPA data from record without directDebiting`() {
        val givenRecord = mapOf(
            "directDebiting" to null,
            "bankCustomer" to "Tästmann GmbH",
            "bankIBAN" to "DE987654321",
            "bankBIC" to "GENODEF1HH2",
            "mandatRef" to "HS-10001-20140801",
        )

        val actualException = assertThrows<ContextException> {
            SepaParser.parse(givenRecord)
        }

        assertThat(actualException.message).isEqualTo("""
            SEPA data without directDebiting
            - while parsing SEPA data {directDebiting=null, bankCustomer=Tästmann GmbH, bankIBAN=DE987654321, bankBIC=GENODEF1HH2, mandatRef=HS-10001-20140801}
            """.trimIndent())
    }
}
