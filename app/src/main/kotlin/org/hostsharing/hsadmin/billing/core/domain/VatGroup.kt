package org.hostsharing.hsadmin.billing.core.domain

import java.math.BigDecimal

interface VatGroup {
    val vatRate: VatRate
    val vatAmount: BigDecimal
    val netAmount: BigDecimal
    val grossAmount: BigDecimal
    val vatAccount: String
    val items: List<InvoiceItem>
}

class VatRate(value: String) {
    val noTax: Boolean = value == "noTax"
    val percentage: BigDecimal =
        if (noTax) BigDecimal.ZERO else BigDecimal(value) / BigDecimal(100)
}

