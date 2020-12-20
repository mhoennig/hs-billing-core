package org.hostsharing.hsadmin.billing.core.domain

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test

internal class VatGroupDefTest {

    @Test
    fun `will format values`() {
        val given = VatGroupDef(
            countryCode = "DE",
            id = "10",
            description = "some description",
            placeOfSupply = PlaceOfSupply.SUPPLIER,
            vatRate = VatRate("20,50"),
            dcAccount = "440010",
            rcAccount = "n/a"
        )

        val actual = given.formatted()

        assertThat(actual).isEqualTo(
            """
            countryCode="DE"
            id="10"
            description="some description"
            placeOfSupply=SUPPLIER
            vatRate="0.2050"
            dcAccount="440010"
            rcAccount="n/a"
            """.trimIndent()
        )
    }

    @Test
    fun `properties will contain values from constructor`() {
        val given = VatGroupDef(
            countryCode = "DE",
            id = "10",
            description = "some description",
            placeOfSupply = PlaceOfSupply.SUPPLIER,
            vatRate = VatRate("20.50"),
            dcAccount = "440010",
            rcAccount = "n/a"
        )

        assertThat(given.countryCode).isEqualTo("DE")
        assertThat(given.id).isEqualTo("10")
        assertThat(given.description).isEqualTo("some description")
        assertThat(given.placeOfSupply).isEqualTo(PlaceOfSupply.SUPPLIER)
        assertThat(given.vatRate).isEqualTo(VatRate("20.50"))
        assertThat(given.dcAccount).isEqualTo("440010")
        assertThat(given.rcAccount).isEqualTo("n/a")
    }
}
