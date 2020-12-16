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

private object VatPercentageFormat {
    val decimalFormat = DecimalFormat(
        "#0.0#",
        DecimalFormatSymbols().apply {
            setGroupingSeparator('.')
            setDecimalSeparator(',')
        }
    ).apply {
        isParseBigDecimal = true
    }
}

class VatRate(val rate: String) {
    companion object {
        val NO_TAX: VatRate = VatRate("noTax")
        val DOMESTIC: VatRate = VatRate("domestic")
        val NOT_APPLICABLE: VatRate = VatRate("n/a")
        val NOT_IMPLEMENTED: VatRate = VatRate("n/i")
        val CENT: BigDecimal = BigDecimal(100).apply {
            setScale(2, RoundingMode.HALF_UP)
        }
    }

    val noTax: Boolean = rate == "noTax"
    val domestic: Boolean = rate == "domestic"
    val notApplicable: Boolean = rate == "n/a"
    val notImplemented: Boolean = rate == "n/i"
    val unknown: Boolean = notApplicable || notImplemented || domestic
    val percentage: BigDecimal
        get() = when (true) {
            noTax -> BigDecimal.ZERO
            domestic -> error("unresolved 'domestic' vat rate reference")
            notApplicable -> error("vat rate not applicable")
            notImplemented -> error("vat rate not implemented")
            else -> VatPercentageFormat.decimalFormat.parse(rate) as BigDecimal / CENT
        }

    init {
        // force to detect invalid numeric format within constructor
        if (!unknown) percentage
    }

    override fun toString(): String =
        when (true) {
            noTax -> "noTax"
            domestic -> "domestic"
            notApplicable -> "n/a"
            notImplemented -> "n/i"
            else -> percentage.toString()
        }

    override fun equals(other: Any?): Boolean =
        other is VatRate &&
            noTax == other.noTax &&
            domestic == other.domestic &&
            notApplicable == other.notApplicable &&
            notImplemented == other.notImplemented &&
            (unknown == other.unknown ||
                percentage.compareTo(other.percentage) == 0)
}
