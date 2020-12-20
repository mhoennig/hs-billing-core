package org.hostsharing.hsadmin.billing.core.lib

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class ConfigurationTest {

    @Test
    fun `getting property templatesDirectory throws exception if not overridden`() {
        val given = object : Configuration {}
        val actual = assertThrows<IllegalStateException> {
            given.templatesDirectory
        }
        assertThat(actual.message).isEqualTo("not configured")
    }

    @Test
    fun `getting property outputDirectory throws exception if not overridden`() {
        val given = object : Configuration {}
        val actual = assertThrows<IllegalStateException> {
            given.outputDirectory
        }
        assertThat(actual.message).isEqualTo("not configured")
    }

    @Test
    fun `default for property domesticCountryCode is 'DE'`() {
        val given = object : Configuration {}
        val actual = given.domesticCountryCode
        assertThat(actual).isEqualTo("DE")
    }

    @Test
    fun `default for property paymentTermInDays is 30`() {
        val given = object : Configuration {}
        val actual = given.paymentTermInDays
        assertThat(actual).isEqualTo(30)
    }
}
