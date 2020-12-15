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

    object configuration : Configuration {
        override val templatesDirectory = "/src/main/resources/templates"
        override val outputDirectory = "/unused/"
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
}
