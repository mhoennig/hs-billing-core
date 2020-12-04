package org.hostsharing.hsadmin.billing.core

import java.math.BigDecimal
import java.text.DateFormat
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class Format {

    companion object {
        val date: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        val datePeriod: DateTimeFormatter = DateTimeFormatter.ofPattern("MM/yyyy")
        val money = DecimalFormat("#,##0.00")
        val vatRate = DecimalFormat("#,##0.00")

        init {
            val decimalFormatSymbols = DecimalFormatSymbols()
            decimalFormatSymbols.decimalSeparator = ','
            decimalFormatSymbols.groupingSeparator = '.'

            money.decimalFormatSymbols = decimalFormatSymbols
            money.isDecimalSeparatorAlwaysShown = true
            money.isGroupingUsed = true

            vatRate.decimalFormatSymbols = decimalFormatSymbols
            vatRate.isDecimalSeparatorAlwaysShown = true
            vatRate.isGroupingUsed = true
        }
    }
}

fun LocalDate.format(format: DateFormat): String = format.format(this)
fun BigDecimal.format(format: DecimalFormat): String = format.format(this)
