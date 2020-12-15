package org.hostsharing.hsadmin.billing.core.domain

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

typealias Account = String

interface VatGroup {
    val vatRate: BigDecimal
    val vatAmount: BigDecimal
    val netAmount: BigDecimal
    val grossAmount: BigDecimal
    val vatAccount: String
    val items: List<InvoiceItem>
}

class VatRate(rate: String) {

    companion object {
        val NO_TAX: VatRate = VatRate("noTax")
        val NOT_APPLICABLE: VatRate = VatRate("n/a")
        val NOT_IMPLEMENTED: VatRate = VatRate("n/i")
        val CENT: BigDecimal = BigDecimal(100).apply {
            setScale(2, RoundingMode.HALF_UP)
        }
        val decimalFormat = DecimalFormat(
            "#0.0#",
            DecimalFormatSymbols().apply {
                setGroupingSeparator('.')
                setDecimalSeparator(',')
            }
        ).apply {
            setParseBigDecimal(true)
        }
    }

    val noTax: Boolean = rate == "noTax"
    val notApplicable: Boolean = rate == "n/a"
    val notImplemented: Boolean = rate == "n/i"
    val unknown: Boolean = notApplicable || notImplemented
    val percentage: BigDecimal? =
        when (true) {
            noTax -> BigDecimal.ZERO
            notApplicable -> null
            notImplemented -> null
            else -> decimalFormat.parse(rate) as BigDecimal / CENT
        }

    override fun toString(): String =
        when (true) {
            noTax -> "noTax"
            notApplicable -> "n/a"
            notImplemented -> "n/i"
            else -> percentage.toString()
        }

    override fun equals(other: Any?): Boolean =
        other is VatRate &&
            noTax == other.noTax &&
            notApplicable == other.notApplicable &&
            notImplemented == other.notImplemented &&
            (
                if (percentage == null)
                    percentage == other.percentage
                else
                    percentage.compareTo(other.percentage) == 0
                )
}
