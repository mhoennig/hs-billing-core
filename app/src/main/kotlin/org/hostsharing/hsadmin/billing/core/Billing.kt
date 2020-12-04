package org.hostsharing.hsadmin.billing.core

import org.hostsharing.hsadmin.billing.core.domain.Contact
import org.hostsharing.hsadmin.billing.core.domain.Customer
import org.hostsharing.hsadmin.billing.core.domain.Invoice
import org.hostsharing.hsadmin.billing.core.domain.InvoiceItem
import org.hostsharing.hsadmin.billing.core.writer.InvoiceWriter
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
        private const val BOOKINGS_TEMPLATE = "bookings.csv.vm"
    }

    val invoices: List<Invoice> = arrayListOf(
        object : Invoice {
            override val documentNumber = "2020-2000-12345"
            override val documentDate = LocalDate.parse("2020-12-02")
            override val customer: Customer = object : Customer {
                override val uidVat = "DE1234567"
                override val number = 12345
                override val code = "xyz"
                override val billingContact = Contact()
            }

            override val referenceDate = LocalDate.parse("2020-11-20")

            override val dueDate = documentDate.plusDays(30)
            override val directDebiting = true

            override val items: List<InvoiceItem> = arrayListOf()
        }
    )

    fun generateBookingsCsv(bookingsCSV: File): File {

        println("Writing bookings file ${bookingsCSV.name} ...")
        val invoicePrinter = InvoiceWriter(
            configuration.templatesDirectory + "/" + BOOKINGS_TEMPLATE)

        FileWriter(bookingsCSV).use { fileWriter ->
            invoices.forEach { invoicePrinter.printInvoice(it, fileWriter) }
        }
        return bookingsCSV
    }
}
