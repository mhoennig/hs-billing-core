package org.hostsharing.hsadmin.billing.core

import java.io.File
import java.time.LocalDate

class Billing(
        periodEndDate: LocalDate,
        billingDate: LocalDate,
        startInvoiceNumber: Int,
        articleGroupsCSV: File,
        customersCSV: File,
        vararg billingItemsCSVs: File,
) {

    fun generateInvoicesCsv(invoicesCSV: File): File {
        invoicesCSV.writeText("""
            customerNumber;documentNumber;documentDate;referenceDate;referencePeriod;dueDate;directDebiting;vat.vatRate;vat.gross"
            "12345";"2020-2000-12345";"02.12.2020";"30.11.2020";"11/2020";"20.12.2020";"true";"16";"206,66"
            """.trimIndent()
        )
        return invoicesCSV
    }
}
