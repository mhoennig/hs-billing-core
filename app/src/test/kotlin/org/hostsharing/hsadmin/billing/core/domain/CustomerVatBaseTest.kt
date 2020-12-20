package org.hostsharing.hsadmin.billing.core.domain

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class CustomerVatBaseTest {

    @Test
    fun `will format valid CustomerVatBase`() {
        val given = CustomerVatBase("NL", VatChargeMode.EU_REVERSE, "NL12345678")
        val actual = given.formatted(4)
        assertThat(actual).isEqualTo(
            """
            vatCountryCode="NL"
            vatChargeMode="EU_REVERSE"
            uidVat="NL12345678"
            """.replaceIndent("    ")
        )
    }

    @Test
    fun `will format valid CustomerVatBase without UID-VAT`() {
        val given = CustomerVatBase("NL", VatChargeMode.EU_DIRECT)
        val actual = given.formatted(4)
        assertThat(actual).isEqualTo(
            """
            vatCountryCode="NL"
            vatChargeMode="EU_DIRECT"
            uidVat=null
            """.replaceIndent("    ")
        )
    }

    @Test
    fun `will throw exception for invalid UID-VAT`() {
        val actual = assertThrows<IllegalStateException> {
            CustomerVatBase("NL", VatChargeMode.EU_DIRECT, "DE12345678")
        }

        assertThat(actual.message).isEqualTo("UID-VAT DE12345678 does not match vatCountryCode NL")
    }
}
