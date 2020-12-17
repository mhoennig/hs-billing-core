package org.hostsharing.hsadmin.billing.core.reader

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import org.hostsharing.hsadmin.billing.core.domain.VatChargeMode
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

internal class ParserTest : Parser("some test data") {

    @Test
    fun `will keep contextInfo`() {
        // without this test, pitest thinks it can't kill a mutant on `open class Parser(val contextInfo: String):
        // replaced return value with "" for org/hostsharing/hsadmin/billing/core/reader/Parser::getContextInfo → NO_COVERAGE
        assertThat(contextInfo).isEqualTo("some test data")
    }

    @Test
    fun `mandatoryString will return given value`() {
        val record: Map<String, String?> = mapOf(
            "someField" to "some value"
        )

        val actual = record.mandatoryString("someField")

        assertThat(actual).isEqualTo("some value")
    }

    @Test
    fun `mandatoryString will throw error for missing value`() {
        val record: Map<String, String?> = mapOf(
            "someField" to null
        )

        val actual = assertThrows<IllegalStateException> {
            record.mandatoryString("someField")
        }

        assertThat(actual.message).isEqualTo("some test data without someField")
    }

    @Test
    fun `optionalString will return given value`() {
        val record: Map<String, String?> = mapOf(
            "someField" to "some value"
        )

        val actual = record.optionalString("someField")

        assertThat(actual).isEqualTo("some value")
    }

    @Test
    fun `optionalString will return null value`() {
        val record: Map<String, String?> = mapOf(
            "someField" to null
        )

        val actual = record.optionalString("someField")

        assertThat(actual).isNull()
    }

    @Test
    fun `mandatoryInt will return given value`() {
        val record: Map<String, String?> = mapOf(
            "someField" to "42"
        )

        val actual = record.mandatoryInt("someField")

        assertThat(actual).isEqualTo(42)
    }

    @Test
    fun `mandatoryInt will throw error for missing value`() {
        val record: Map<String, String?> = mapOf(
            "someField" to null
        )

        val actual = assertThrows<IllegalStateException> {
            record.mandatoryInt("someField")
        }

        assertThat(actual.message).isEqualTo("some test data without someField")
    }

    @Test
    fun `mandatoryBigDecimal will return given value`() {
        val record: Map<String, String?> = mapOf(
            "someField" to "42019832018301832.777"
        )

        val actual = record.mandatoryBigDecimal("someField")

        assertThat(actual).isEqualTo(BigDecimal("42019832018301832.777"))
    }

    @Test
    fun `mandatoryBigDecimal will throw error for missing value`() {
        val record: Map<String, String?> = mapOf(
            "someField" to null
        )

        val actual = assertThrows<IllegalStateException> {
            record.mandatoryBigDecimal("someField")
        }

        assertThat(actual.message).isEqualTo("some test data without someField")
    }

    @Test
    fun `mandatoryBoolean will return given true value`() {
        val record: Map<String, String?> = mapOf(
            "someField" to "true"
        )

        val actual = record.mandatoryBoolean("someField")

        assertThat(actual).isEqualTo(true)
    }

    @Test
    fun `mandatoryBoolean will return given false value`() {
        val record: Map<String, String?> = mapOf(
            "someField" to "false"
        )

        val actual = record.mandatoryBoolean("someField")

        assertThat(actual).isEqualTo(false)
    }

    @Test
    fun `mandatoryBoolean will throw error for missing value`() {
        val record: Map<String, String?> = mapOf(
            "someField" to null
        )

        val actual = assertThrows<IllegalStateException> {
            record.mandatoryBoolean("someField")
        }

        assertThat(actual.message).isEqualTo("some test data without someField")
    }

    @Test
    fun `mandatoryCountryCode will return given valid value`() {
        val record: Map<String, String?> = mapOf(
            "someField" to "DE"
        )

        val actual = record.mandatoryCountryCode("someField")

        assertThat(actual).isEqualTo("DE")
    }

    @Test
    fun `mandatoryCountryCode will throw error for missing value`() {
        val record: Map<String, String?> = mapOf(
            "someField" to null
        )

        val actual = assertThrows<IllegalStateException> {
            record.mandatoryCountryCode("someField")
        }

        assertThat(actual.message).isEqualTo("some test data without someField")
    }

    @Test
    fun `mandatoryCountryCode will throw error for invalid value`() {
        val record: Map<String, String?> = mapOf(
            "someField" to "invalid value"
        )

        val actual = assertThrows<IllegalStateException> {
            record.mandatoryCountryCode("someField")
        }

        assertThat(actual.message).isEqualTo("some test data: someField='invalid value' not a valid country code")
    }

    @Test
    fun `mandatoryVatChargeMode will return given valid value`() {
        val record: Map<String, String?> = mapOf(
            "someField" to VatChargeMode.EU_REVERSE.code
        )

        val actual = record.mandatoryVatChargeMode("someField")

        assertThat(actual).isEqualTo(VatChargeMode.EU_REVERSE)
    }

    @Test
    fun `mandatoryVatChargeMode will throw error for invalid value`() {
        val record: Map<String, String?> = mapOf(
            "someField" to "invalid value"
        )

        val actual = assertThrows<IllegalStateException> {
            record.mandatoryVatChargeMode("someField")
        }

        assertThat(actual.message).isEqualTo("some test data: someField='invalid value' not a valid VAT charge code")
    }

    @Test
    fun `mandatoryVatChargeMode will throw error for missing value`() {
        val record: Map<String, String?> = mapOf(
            "someField" to null
        )

        val actual = assertThrows<IllegalStateException> {
            record.mandatoryVatChargeMode("someField")
        }

        assertThat(actual.message).isEqualTo("some test data without someField")
    }
}
