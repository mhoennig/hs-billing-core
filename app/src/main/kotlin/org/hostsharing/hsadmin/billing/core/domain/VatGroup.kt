package org.hostsharing.hsadmin.billing.core.domain

import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

interface VatGroup {
    val vatRate: VatRate
    val vatAmount: BigDecimal
    val netAmount: BigDecimal
    val grossAmount: BigDecimal
    val vatAccount: String
    val items: List<InvoiceItem>
}

class VatRate(value: String) {

    companion object {
        val decimalFormat = DecimalFormat("#0.0#",
            DecimalFormatSymbols().also {
                it.setGroupingSeparator('.')
                it.setDecimalSeparator(',')
            }).also {
            it.setParseBigDecimal(true)
        }
    }

    val noTax: Boolean = value == "noTax"
    val percentage: BigDecimal =
        if (noTax) BigDecimal.ZERO else decimalFormat.parse(value) as BigDecimal / BigDecimal(100)

    override fun toString(): String =
        if (noTax ) "noTax" else decimalFormat.format(percentage * BigDecimal(100))
}

