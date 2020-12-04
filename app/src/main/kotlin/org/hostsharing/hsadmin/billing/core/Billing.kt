package org.hostsharing.hsadmin.billing.core

import java.io.File
import java.io.FileWriter
import java.time.LocalDate

class Billing(
    val configuration: Configuration,
    periodEndDate: LocalDate,
    billingDate: LocalDate,
    startInvoiceNumber: Int,
    vatGroupsCSV: File,
    customersCSV: File,
    vararg billingItemsCSVs: File,
) {
    companion object {
        private const val BOOKINGS_TEMPLATE = "bookings.csv"
    }

    val invoices: List<Invoice> = arrayListOf(
        object : Invoice {
            override val documentNumber = "2020-2000-12345"
            override val documentDate = LocalDate.parse("2020-12-02")
            override val customer: Customer = Customer()

            override val referenceDate = LocalDate.parse("2020-11-20")

            override val dueDate = documentDate.plusDays(30)
            override val directDebiting = true

            override val items: List<InvoiceItem> = arrayListOf()
        }
    )

    fun generateBookingsCsv(bookingsCSV: File): File {

        println("Writing bookings file ${bookingsCSV.name} ...")
        val invoicePrinter = InvoicePrinter(
            configuration.templatesDirectory + "/" + BOOKINGS_TEMPLATE)

        FileWriter(bookingsCSV).use { fileWriter ->
            invoices.forEach { invoicePrinter.printInvoice(it, fileWriter) }
        }
        return bookingsCSV
    }
}
