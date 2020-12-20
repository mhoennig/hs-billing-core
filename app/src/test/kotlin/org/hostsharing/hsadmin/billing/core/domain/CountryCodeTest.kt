package org.hostsharing.hsadmin.billing.core.domain

import assertk.assertThat
import assertk.assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

internal class CountryCodeTest {
    @ParameterizedTest
    @ValueSource(strings = arrayOf("DE", "AT"))
    fun `will recognize valid country codes as valid`(value: String) {
        assertThat(value.isCountryCode()).isTrue()
    }

    @ParameterizedTest
    @ValueSource(strings = arrayOf("", "D", "d", "de", "DEX", "D1", "1D", "12"))
    fun `will recognize invalid country codes as invalid`(value: String) {
        assertThat(value.isCountryCode()).isFalse()
    }
}
