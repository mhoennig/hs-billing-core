package org.hostsharing.hsadmin.billing.core.domain

import org.hostsharing.hsadmin.billing.core.reader.VatRate
import java.math.BigDecimal

interface VatGroup {
    val vatRate: VatRate
    val vatAmount: BigDecimal
    val netAmount: BigDecimal
    val grossAmount: BigDecimal
    val vatAccount: String
    val items: List<InvoiceItem>
}

