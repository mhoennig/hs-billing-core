package org.hostsharing.hsadmin.billing.core.domain

import assertk.assertThat
import assertk.assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.IllegalStateException
import java.math.BigDecimal

internal class VatRateTest {
    @Test
    fun `will process percent value`() {
        val given = VatRate("12,34")
        assertThat(given.percentage).isEqualByComparingTo(BigDecimal("0.1234"))
        assertThat(given.noTax).isFalse()
        assertThat(given.domestic).isFalse()
        assertThat(given.notApplicable).isFalse()
        assertThat(given.notImplemented).isFalse()
        assertThat(given.unknown).isFalse()
        assertThat(given.toString()).isEqualTo("0.1234")
        assertThat(given.hashCode()).isEqualTo(VatRate("12,34").hashCode())
        assertThat(given).isEqualTo(VatRate("12,34"))
        assertThat(given).isNotEqualTo(VatRate.NO_TAX)
    }

    @Test
    fun `will process 'noTax' value`() {
        val given = VatRate("noTax")
        assertThat(given.percentage).isEqualByComparingTo(BigDecimal("0"))
        assertThat(given.noTax).isTrue()
        assertThat(given.domestic).isFalse()
        assertThat(given.notApplicable).isFalse()
        assertThat(given.notImplemented).isFalse()
        assertThat(given.unknown).isFalse()
        assertThat(given.toString()).isEqualTo("noTax")
        assertThat(given.hashCode()).isEqualTo(VatRate.NO_TAX.hashCode())
        assertThat(given).isEqualTo(VatRate.NO_TAX)
        assertThat(given).isNotEqualTo(VatRate("12,34"))
    }

    @Test
    fun `will process 'domestic' value`() {
        val given = VatRate("domestic")
        assertThat(assertThrows<IllegalStateException> { given.percentage }.message)
            .isEqualTo("unresolved 'domestic' vat rate reference")
        assertThat(given.noTax).isFalse()
        assertThat(given.domestic).isTrue()
        assertThat(given.notApplicable).isFalse()
        assertThat(given.notImplemented).isFalse()
        assertThat(given.unknown).isTrue()
        assertThat(given.toString()).isEqualTo("domestic")
        assertThat(given.hashCode()).isEqualTo(VatRate.DOMESTIC.hashCode())
        assertThat(given).isEqualTo(VatRate.DOMESTIC)
        assertThat(given).isNotEqualTo(VatRate("12,34"))
    }

    @Test
    fun `will process not-applicable value`() {
        val given = VatRate("n/a")
        assertThat(assertThrows<IllegalStateException> { given.percentage }.message)
            .isEqualTo("vat rate not applicable")
        assertThat(given.noTax).isFalse()
        assertThat(given.domestic).isFalse()
        assertThat(given.notApplicable).isTrue()
        assertThat(given.notImplemented).isFalse()
        assertThat(given.unknown).isTrue()
        assertThat(given.toString()).isEqualTo("n/a")
        assertThat(given.hashCode()).isEqualTo(VatRate.NOT_APPLICABLE.hashCode())
        assertThat(given).isEqualTo(VatRate.NOT_APPLICABLE)
        assertThat(given).isNotEqualTo(VatRate("12,34"))
    }

    @Test
    fun `will process not-implemented value`() {
        val given = VatRate("n/i")
        assertThat(assertThrows<IllegalStateException> { given.percentage }.message)
            .isEqualTo("vat rate not implemented")
        assertThat(given.noTax).isFalse()
        assertThat(given.domestic).isFalse()
        assertThat(given.notApplicable).isFalse()
        assertThat(given.notImplemented).isTrue()
        assertThat(given.unknown).isTrue()
        assertThat(given.toString()).isEqualTo("n/i")
        assertThat(given.hashCode()).isEqualTo(VatRate.NOT_IMPLEMENTED.hashCode())
        assertThat(given).isEqualTo(VatRate.NOT_IMPLEMENTED)
        assertThat(given).isNotEqualTo(VatRate("12,34"))
    }
}
