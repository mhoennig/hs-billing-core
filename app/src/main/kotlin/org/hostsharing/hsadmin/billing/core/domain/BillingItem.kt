package org.hostsharing.hsadmin.billing.core.domain

import java.math.BigDecimal

/**
 * Represents a single input item to be invoiced.
 */
interface BillingItem {
    val customerCode: String
    val netAmount: BigDecimal
    val vatGroupId: String
}
