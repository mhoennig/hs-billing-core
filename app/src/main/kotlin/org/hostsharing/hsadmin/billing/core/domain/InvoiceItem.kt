package org.hostsharing.hsadmin.billing.core.domain

import java.math.BigDecimal

interface InvoiceItem {
    val customerCode: String
    val netAmount: BigDecimal
}
