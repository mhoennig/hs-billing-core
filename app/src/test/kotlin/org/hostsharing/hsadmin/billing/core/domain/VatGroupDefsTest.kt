package org.hostsharing.hsadmin.billing.core.domain

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isSameAs
import assertk.assertions.prop
import org.hostsharing.hsadmin.billing.core.lib.Configuration
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class VatGroupDefsTest {

    private val config = object : Configuration {
        override val domesticVatCountryCode = "DE"
        override val domesticCountryCodes = arrayOf("DE")
    }

    /* ktlint-disable */// @formatter:off
    private val vatGroupDefDE00 = VatGroupDef(
        "DE", "00",
        "Membership", PlaceOfSupply.NOT_APPLICABLE, VatRate.NO_TAX, dcAccount = "420000", rcAccount = "n/a"
    )
    private val vatGroupDefDE60 = VatGroupDef(
        "DE", "60",
        "Webmaster", PlaceOfSupply.SUPPLIER, VatRate("16,00"), dcAccount = "440060", rcAccount = "n/a"
    )
    private val vatGroupDefAT00 = VatGroupDef(
        "AT", "00",
        "Membership", PlaceOfSupply.NOT_APPLICABLE, VatRate.NO_TAX, dcAccount = "420000", rcAccount = "420000"
    )
    private val vatGroupDefAT60 = VatGroupDef(
        "AT", "60",
        "Webmaster", PlaceOfSupply.SUPPLIER, VatRate("domestic"), dcAccount = "433860", rcAccount = "433660"
    )
    /* ktlint-enable */ // @formatter:off

    /* ktlint-disable */// @formatter:off
    val givenVatGroupDefs = VatGroupDefs(
        config,
        mapOf(
            "DE" to mapOf(
                "00" to vatGroupDefDE00,
                "60" to vatGroupDefDE60,
            ),
            "AT" to mapOf(
                "00" to vatGroupDefAT00,
                "60" to vatGroupDefAT60,
            ),
        )
    )
    /* ktlint-enable */ // @formatter:off

    @Test
    fun `lookup will return VatGroupDef for existing countryCode and vatGroupId`() {
        assertThat(givenVatGroupDefs.lookup("DE", "00")).isSameAs(vatGroupDefDE00)
        assertThat(givenVatGroupDefs.lookup("DE", "60")).isSameAs(vatGroupDefDE60)
        assertThat(givenVatGroupDefs.lookup("AT", "00")).isSameAs(vatGroupDefAT00)
        assertThat(givenVatGroupDefs.lookup("AT", "60")).isSameAs(vatGroupDefAT60)
    }

    @Test
    fun `lookup will throw exception with error message for not existing country code`() {
        val actual = assertThrows<IllegalStateException> {
            givenVatGroupDefs.lookup("XX", "00")
        }
        assertThat(actual).isNotNull().prop(Exception::message)
            .isEqualTo("no VAT group def found for 'XX' in '[DE, AT]'")
    }

    @Test
    fun `lookup will throw exception with error message for not existing VAT group ID`() {
        val actual = assertThrows<IllegalStateException> {
            givenVatGroupDefs.lookup("DE", "99")
        }
        assertThat(actual).isNotNull().prop(Exception::message)
            .isEqualTo("no VAT group def found for '99' in '[00, 60]' for country 'DE'")
    }

    @Test
    fun `resolveVatRateReferences will return all vat rates with resolved 'domestic' vat rate references`() {
        val actual = givenVatGroupDefs.resolveVatRateReferences()

        assertThat(actual.lookup("DE", "00").vatRate)
            .isEqualTo(VatRate.NO_TAX)
        assertThat(actual.lookup("AT", "60").vatRate)
            .isEqualTo(VatRate("16,00"))
    }
}
