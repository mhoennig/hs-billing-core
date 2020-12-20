package org.hostsharing.hsadmin.billing.core.domain

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isSameAs
import org.hostsharing.hsadmin.billing.core.lib.Configuration
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.lang.RuntimeException

internal class VatChargeModeTest {

    object FakeConfig : Configuration {
        override val domesticVatCountryCode: String
            get() = "DE"
        override val domesticCountryCodes: Array<String>
            get() = arrayOf("DE")
        override val euCountryCodes: Array<String>
            get() = arrayOf("AT")
    }

    @ParameterizedTest
    @EnumSource(VatChargeMode::class)
    fun `will resolve valid values`(vatChargeMode: VatChargeMode) {
        assertThat(VatChargeMode.ofCode(vatChargeMode.code)).isSameAs(vatChargeMode)
    }

    @Test
    fun `will throw error on unknown code`() {
        val actualException = assertThrows<RuntimeException> {
            VatChargeMode.ofCode("unknown")
        }

        assertThat(actualException.message).isEqualTo("unknown vatChargeMode 'unknown'")
    }

    @Test
    fun `will validate DOMESTIC country code`() {

        assertThat(
            assertThrows<IllegalStateException> {
                VatChargeMode.DOMESTIC.validate(FakeConfig, "AT")
            }.message
        ).isEqualTo("vatCountryCode 'AT' is invalid for vatChargeMode DOMESTIC")

        assertThat(
            assertThrows<IllegalStateException> {
                VatChargeMode.DOMESTIC.validate(FakeConfig, "CH")
            }.message
        ).isEqualTo("vatCountryCode 'CH' is invalid for vatChargeMode DOMESTIC")

        VatChargeMode.DOMESTIC.validate(FakeConfig, "DE")
    }

    @ParameterizedTest
    @EnumSource(value = VatChargeMode::class, names = arrayOf("EU_DIRECT", "EU_REVERSE"))
    fun `will validate EU country code`(vatChargeMode: VatChargeMode) {

        assertThat(
            assertThrows<IllegalStateException> {
                vatChargeMode.validate(FakeConfig, "DE")
            }.message
        ).isEqualTo("vatCountryCode 'DE' is invalid for vatChargeMode $vatChargeMode")

        assertThat(
            assertThrows<IllegalStateException> {
                vatChargeMode.validate(FakeConfig, "CH")
            }.message
        ).isEqualTo("vatCountryCode 'CH' is invalid for vatChargeMode $vatChargeMode")

        vatChargeMode.validate(FakeConfig, "AT")
    }

    @Test
    fun `will validate NON_EU_REVERSE country code`() {

        assertThat(
            assertThrows<IllegalStateException> {
                VatChargeMode.NON_EU_REVERSE.validate(FakeConfig, "DE")
            }.message
        ).isEqualTo("vatCountryCode 'DE' is invalid for vatChargeMode NON_EU_REVERSE")

        assertThat(
            assertThrows<IllegalStateException> {
                VatChargeMode.NON_EU_REVERSE.validate(FakeConfig, "AT")
            }.message
        ).isEqualTo("vatCountryCode 'AT' is invalid for vatChargeMode NON_EU_REVERSE")

        VatChargeMode.NON_EU_REVERSE.validate(FakeConfig, "CH")
    }
}
