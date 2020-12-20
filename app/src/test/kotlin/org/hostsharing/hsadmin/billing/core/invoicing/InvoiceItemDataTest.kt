package org.hostsharing.hsadmin.billing.core.invoicing

import assertk.assertThat
import assertk.assertions.isEqualByComparingTo
import assertk.assertions.isEqualTo
import org.hostsharing.hsadmin.billing.core.domain.BillingItem
import org.hostsharing.hsadmin.billing.core.domain.VatChargeMode
import org.junit.jupiter.api.Test
import java.math.BigDecimal

internal class InvoiceItemDataTest {

    private val given = InvoiceItemData(
        vatCountryCode = "NL",
        vatChargeMode = VatChargeMode.EU_DIRECT,
        vatRate = BigDecimal("0.10"),
        vatAccount = "123456",
        billingItem = BillingItem(
            customerCode = "xyz",
            netAmount = BigDecimal("10.00"),
            vatGroupId = "00"
        )
    )

    @Test
    fun `returns all property values`() {
        assertThat(given.vatCountryCode).isEqualTo("NL")
        assertThat(given.vatChargeMode).isEqualTo(VatChargeMode.EU_DIRECT)
        assertThat(given.vatRate).isEqualTo(BigDecimal("0.10"))
        assertThat(given.vatAccount).isEqualTo("123456")
        assertThat(given.customerCode).isEqualTo("xyz")
        assertThat(given.netAmount).isEqualTo(BigDecimal("10.00"))
        assertThat(given.vatGroupId).isEqualTo("00")
    }

    @Test
    fun `calculates vatAmount`() {
        assertThat(given.vatAmount).isEqualTo(BigDecimal("1.0000"))
    }

    @Test
    fun `calculates grossAmount`() {
        assertThat(given.grossAmount).isEqualByComparingTo(BigDecimal("11.00"))
    }
}
