package org.hostsharing.hsadmin.billing.core.lib

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate

internal class FormatTest {

    @Test
    fun `formats the year of a LocalDate`() {
        val given = LocalDate.parse("1996-12-26")
        val actual = given.format(Format.year)
        assertThat(actual).isEqualTo("1996")
    }

    @Test
    fun `formats a LocalDate`() {
        val given = LocalDate.parse("1996-12-26")
        val actual = given.format(Format.date)
        assertThat(actual).isEqualTo("26.12.1996")
    }

    @Test
    fun `formats the billing period (month) of a LocalDate`() {
        val given = LocalDate.parse("1996-12-26")
        val actual = given.format(Format.datePeriod)
        assertThat(actual).isEqualTo("12/1996")
    }

    @Test
    fun `formats a monetary value`() {
        val given = BigDecimal("12.34")
        val actual = given.format(Format.money)
        assertThat(actual).isEqualTo("12,34")
    }

    @Test
    fun `formats a VAT rate value`() {
        val given = BigDecimal("12.34")
        val actual = given.format(Format.vatRate)
        assertThat(actual).isEqualTo("12,34")
    }
}
