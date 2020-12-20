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
        "#0.00#",
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
        private const val DIGITS = 4
        private const val NO_TAX_VALUE = "noTax"
        private const val DOMESTIC_VALUE = "domestic"
        private const val NOT_APPLICABLE_VALUE = "n/a"
        private const val NOT_IMPLEMENTED_VALUE = "n/i"

        val NO_TAX: VatRate = VatRate(NO_TAX_VALUE)
        val DOMESTIC: VatRate = VatRate(DOMESTIC_VALUE)
        val NOT_APPLICABLE: VatRate = VatRate(NOT_APPLICABLE_VALUE)
        val NOT_IMPLEMENTED: VatRate = VatRate(NOT_IMPLEMENTED_VALUE)
        val CENT: BigDecimal = BigDecimal(100).apply {
            setScale(4, RoundingMode.HALF_UP)
        }
    }

    val noTax: Boolean = rate == NO_TAX_VALUE
    val domestic: Boolean = rate == DOMESTIC_VALUE
    val notApplicable: Boolean = rate == NOT_APPLICABLE_VALUE
    val notImplemented: Boolean = rate == NOT_IMPLEMENTED_VALUE
    val unknown: Boolean = notApplicable || notImplemented || domestic
    val percentage: BigDecimal
        get() = when (true) {
            noTax -> BigDecimal.ZERO
            domestic -> error("unresolved 'domestic' vat rate reference")
            notApplicable -> error("vat rate not applicable")
            notImplemented -> error("vat rate not implemented")
            else -> {
                (VatPercentageFormat.decimalFormat.parse(rate) as BigDecimal)
                    .setScale(DIGITS) / CENT
            }
        }

    init {
        // force to detect invalid numeric format within constructor
        if (!unknown) percentage
    }

    override fun toString(): String =
        when (true) {
            noTax -> NO_TAX_VALUE
            domestic -> DOMESTIC_VALUE
            notApplicable -> NOT_APPLICABLE_VALUE
            notImplemented -> NOT_IMPLEMENTED_VALUE
            else -> percentage.toString()
        }

    override fun equals(other: Any?): Boolean =
        other is VatRate &&
            noTax == other.noTax &&
            domestic == other.domestic &&
            notApplicable == other.notApplicable &&
            notImplemented == other.notImplemented &&
            (unknown == other.unknown || percentage.compareTo(other.percentage) == 0)

    override fun hashCode(): Int = toString().hashCode()
}
