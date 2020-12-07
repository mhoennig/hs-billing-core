package org.hostsharing.hsadmin.billing.domain

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isSameAs
import org.hostsharing.hsadmin.billing.core.lib.Configuration
import org.hostsharing.hsadmin.billing.core.domain.VatChargeCode
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.lang.RuntimeException

internal class VatChargeCodeTest {

    object configuration : Configuration {
        override val templatesDirectory = "/src/main/resources/templates"
        override val accountBaseForNonTaxableRevenues = "1000"
        override val accountBaseForTaxableDomesticRevenues = "1001"
        override val accountBaseForTaxableForeignEuRevenues = "1002"
        override val accountBaseForTaxableForeignEuRevenuesReverseCharge = "1003"
        override val accountBaseForTaxableAbroadEuRevenuesReverseCharge = "1004"
    }

    @ParameterizedTest
    @EnumSource(VatChargeCode::class)
    fun `will resolve valid values`(vatChargeCode: VatChargeCode) {
        assertThat(VatChargeCode.ofCode(vatChargeCode.code)).isSameAs(vatChargeCode)
    }

    @Test
    fun `will throw error on unknown code`() {
        val actualException = assertThrows<RuntimeException> {
            VatChargeCode.ofCode("unknown")
        }

        assertThat(actualException.message).isEqualTo("unknown vatChargeCode 'unknown'")
    }

    @ParameterizedTest
    @EnumSource(VatChargeCode::class)
    fun `will resolve account base`(vatChargeCode: VatChargeCode) {
        assertThat(vatChargeCode.accountBase(configuration)).isEqualTo((vatChargeCode.ordinal+1001).toString())
    }
}
