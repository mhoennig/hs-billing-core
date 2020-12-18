package org.hostsharing.hsadmin.billing.core.domain

import java.math.BigDecimal

/**
 * Represents a single input item to be invoiced.
 */
data class BillingItem(
    override val customerCode: String,
    override val netAmount: BigDecimal,
    override val vatGroupId: String
) : Item, Formattable {
    override fun format(indent: Int): String = """
        |customerCode=${customerCode.quoted}
        |netAmount=${netAmount.quoted}
        |vatGroupId=${vatGroupId.quoted}
        """
}
