package org.hostsharing.hsadmin.billing.core.reader

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.prop
import org.hostsharing.hsadmin.billing.core.domain.CustomerVatBase
import org.hostsharing.hsadmin.billing.core.domain.VatChargeMode
import org.hostsharing.hsadmin.billing.core.lib.DomainException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class CustomerVatBaseParserTest {
    @Test
    fun `will parse a valid record`() {
        val givenRecord = mapOf(
            "vatCountryCode" to "DE",
            "vatChargeMode" to "domestic",
            "uidVat" to "DE1234567890"
        )

        val actual = CustomerVatBaseParser.parse(givenRecord)

        assertThat(actual).isEqualTo(
            CustomerVatBase(
                vatCountryCode = "DE",
                vatChargeMode = VatChargeMode.DOMESTIC,
                uidVat = "DE1234567890"
            )
        )
    }

    @Test
    fun `will throw exception with context information when parsing an invalid record`() {
        val givenRecord = mapOf(
            "vatCountryCode" to "DE",
            "vatChargeMode" to "unknown",
            "uidVat" to "DE1234567890"
        )

        val actual = assertThrows<DomainException> {
            CustomerVatBaseParser.parse(givenRecord)
        }

        assertThat(actual).isNotNull()
            .prop(Exception::message).isEqualTo("""
            CustomerVatBase: vatChargeMode='unknown' not a valid VAT charge code
            - in parsing CustomerVatBase data $givenRecord
            """.trimIndent())
    }
}
