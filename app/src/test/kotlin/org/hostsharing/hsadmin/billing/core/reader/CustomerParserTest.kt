package org.hostsharing.hsadmin.billing.core.reader

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.hostsharing.hsadmin.billing.core.lib.DomainException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class CustomerParserTest {

    private val recordWithAllFieldValues: Map<String, String?> = mapOf(
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
        "countryCode" to "DE",
        "email" to "taesti@taestmann.de",
        "uidVat" to "DE81201900030012345678",
        "directDebiting" to "true",
        "bankCustomer" to "Tästmann GmbH",
        "bankIBAN" to "DE987654321",
        "bankBIC" to "GENODEF1HH2",
        "mandateRef" to "HS-10001-20140801",
        "vatCountryCode" to "DE",
        "vatChargeMode" to "domestic",
    )

    @Test
    fun `will create Customer from record with all fields set and valid`() {
        val givenRecord = recordWithAllFieldValues

        val actual = CustomerParser.parse(givenRecord)

        assertThat(actual.formatted()).isEqualTo(
            """
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
                email="taesti@taestmann.de"
            }
            sepa={
                directDebiting="true"
                bankCustomer="Tästmann GmbH"
                bankIBAN="DE987654321"
                bankBIC="GENODEF1HH2"
                mandateRef="HS-10001-20140801"
            }
            vatBase={
                vatCountryCode="DE"
                vatChargeMode="DOMESTIC"
                uidVat="DE81201900030012345678"
            }
            """.trimIndent()
        )
    }

    @Test
    fun `will throw error when parsing contact from record with invalid country code`() {
        val givenRecord = recordWithAllFieldValues.toMutableMap().apply {
            put("vatCountryCode", "X")
        }

        val actualException = assertThrows<DomainException> {
            CustomerParser.parse(givenRecord)
        }

        assertThat(actualException.message).isEqualTo(
            """
            CustomerVatBase: vatCountryCode='X' not a valid country code
            - in parsing CustomerVatBase data {customerNumber=10001, customerCode=hsh00-dee, company=Testmann GmbH, salutation=Herr, title=Dr., firstName=Tästi, lastName=Tästmann, co=Tästmann Holdings AG, street=Teststraße 42, zipCode=20144, city=Hamburg, country=Germany, countryCode=DE, email=taesti@taestmann.de, uidVat=DE81201900030012345678, directDebiting=true, bankCustomer=Tästmann GmbH, bankIBAN=DE987654321, bankBIC=GENODEF1HH2, mandateRef=HS-10001-20140801, vatCountryCode=X, vatChargeMode=domestic}
            - in parsing customer $givenRecord
            """.trimIndent()
        )
    }

    @Test
    fun `will throw error when parsing Customer from record with invalid vatChargeMode`() {
        val givenRecord = recordWithAllFieldValues.toMutableMap().apply {
            put("vatChargeMode", "garbage")
        }

        val actual = assertThrows<DomainException> {
            CustomerParser.parse(givenRecord)
        }

        assertThat(actual.message).isEqualTo(
            """
            CustomerVatBase: vatChargeMode='garbage' not a valid VAT charge code
            - in parsing CustomerVatBase data {customerNumber=10001, customerCode=hsh00-dee, company=Testmann GmbH, salutation=Herr, title=Dr., firstName=Tästi, lastName=Tästmann, co=Tästmann Holdings AG, street=Teststraße 42, zipCode=20144, city=Hamburg, country=Germany, countryCode=DE, email=taesti@taestmann.de, uidVat=DE81201900030012345678, directDebiting=true, bankCustomer=Tästmann GmbH, bankIBAN=DE987654321, bankBIC=GENODEF1HH2, mandateRef=HS-10001-20140801, vatCountryCode=DE, vatChargeMode=garbage}
            - in parsing customer $givenRecord
            """.trimIndent()
        )
    }
}
