package org.hostsharing.hsadmin.billing.core.domain

import java.math.BigDecimal

/**
 * Represents a single output item belonging to an invoice.
 */
interface InvoiceItem : BillingItem {
    val grossAmount: BigDecimal
}
