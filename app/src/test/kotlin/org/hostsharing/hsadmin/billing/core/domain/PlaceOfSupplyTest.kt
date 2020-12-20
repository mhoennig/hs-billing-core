package org.hostsharing.hsadmin.billing.core.domain

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class PlaceOfSupplyTest {

    @Test
    fun `processes NOT_APPLICABLE`() {
        assertThat(PlaceOfSupply.ofCode("n/a")).isEqualTo(PlaceOfSupply.NOT_APPLICABLE)
        assertThat(PlaceOfSupply.NOT_APPLICABLE.code).isEqualTo("n/a")
    }

    @Test
    fun `processes SUPPLIER`() {
        assertThat(PlaceOfSupply.ofCode("supplier")).isEqualTo(PlaceOfSupply.SUPPLIER)
        assertThat(PlaceOfSupply.SUPPLIER.code).isEqualTo("supplier")
    }

    @Test
    fun `processes RECEIVER`() {
        assertThat(PlaceOfSupply.ofCode("receiver")).isEqualTo(PlaceOfSupply.RECEIVER)
        assertThat(PlaceOfSupply.RECEIVER.code).isEqualTo("receiver")
    }

    @Test
    fun `ofCode() throws exception with error message for unknown code`() {
        assertThat( assertThrows<IllegalStateException> {
            PlaceOfSupply.ofCode("unknown")
        }.message).isEqualTo("unknown vatChargeMode 'unknown'")
    }
}
