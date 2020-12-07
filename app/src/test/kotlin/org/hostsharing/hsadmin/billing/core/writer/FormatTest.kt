package org.hostsharing.hsadmin.billing.core.writer

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.hostsharing.hsadmin.billing.core.lib.Format
import org.hostsharing.hsadmin.billing.core.lib.format
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate

internal class FormatTest {

    @Test
    fun `will format year`() {
        val given = LocalDate.parse("2020-12-06")
        val actual = given.format(Format.year)
        assertThat(actual).isEqualTo("2020")
    }

    @Test
    fun `will format date`() {
        val given = LocalDate.parse("2020-12-06")
        val actual = given.format(Format.date)
        assertThat(actual).isEqualTo("06.12.2020")
    }

    @Test
    fun `will format datePeriod`() {
        val given = LocalDate.parse("2020-12-06")
        val actual = given.format(Format.datePeriod)
        assertThat(actual).isEqualTo("12/2020")
    }

    @Test
    fun `will format money`() {
        val given = BigDecimal("16")
        val actual = given.format(Format.money)
        assertThat(actual).isEqualTo("16,00")
    }

    @Test
    fun `will format vatRate`() {
        val given = BigDecimal("16")
        val actual = given.format(Format.vatRate)
        assertThat(actual).isEqualTo("16,00")
    }
}
