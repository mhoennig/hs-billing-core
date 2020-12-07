package org.hostsharing.hsadmin.billing.core.reader

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test

internal class CustomerParserTest {

    @Test
    fun `will create Customer from record with all fields set and valid`() {
        val givenRecord = mapOf(
            "customerNumber" to "10001",
            "customerCode" to "hsh00-dee",
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
            "email" to "taesti@taestmann.de",
            "uidVat" to "DE81201900030012345678",
            "directDebiting" to "true",
            "bankCustomer" to "Tästmann GmbH",
            "bankIBAN" to "DE987654321",
            "bankBIC" to "GENODEF1HH2",
            "mandatRef" to "HS-10001-20140801",
            "countryCode" to "DE",
            "vatChargeCode" to "domestic",
        )

        val actual = CustomerParser.parse(givenRecord)

        assertThat(actual.formatted()).isEqualTo("""
            number="10001"
            code="hsh00-dee"
            billingContact={
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
            }
            sepa={
                directDebiting="true"
                bankCustomer="Tästmann GmbH"
                bankIBAN="DE987654321"
                bankBIC="GENODEF1HH2"
                mandatRef="HS-10001-20140801"
            }
            vatChargeCode="DOMESTIC"
            uidVat="DE81201900030012345678"
            """.trimIndent())
    }
}
