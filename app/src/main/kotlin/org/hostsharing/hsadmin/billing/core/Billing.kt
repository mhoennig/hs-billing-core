package org.hostsharing.hsadmin.billing.core

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import org.hostsharing.hsadmin.billing.core.domain.Contact
import org.hostsharing.hsadmin.billing.core.domain.Customer
import org.hostsharing.hsadmin.billing.core.domain.Invoice
import org.hostsharing.hsadmin.billing.core.domain.InvoiceItem
import org.hostsharing.hsadmin.billing.core.lib.Format
import org.hostsharing.hsadmin.billing.core.writer.InvoiceWriter
import java.io.File
import java.io.FileWriter
import java.math.BigDecimal
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

    val invoices: MutableList<Invoice> = mutableListOf()

    init {
        val customers: List<Customer> = readCustomers(customersCSV)

        val billingItems: List<InvoiceItem> = readBillingItems(billingItemsCSVs)

        customers.forEachIndexed { index, customer ->
            invoices.add(object : Invoice {
                override val documentNumber = "${billingDate.format(Format.year)}-${startInvoiceNumber + index}-${customer.number}"
                override val documentDate = billingDate
                override val customer = customer
                override val referenceDate = periodEndDate
                override val dueDate = this.documentDate.plusDays(30)
                override val directDebiting = this.customer.directDebiting
                override val items = billingItems.filter { it.customerCode == customer.code }
            })
        }
    }

    private fun readCustomers(customersCSV: File): List<Customer> =
        semicolonSeparatedFileReader().open(customersCSV) {
            readAllWithHeaderAsSequence()
                .map {
                    object : Customer {
                        override val uidVat = it["uidVat"]
                        override val number = Integer.parseInt(it["customerNumber"]
                            ?: error("customer-row without customerNumber"))
                        override val code = it["customerCode"] ?: error("customer-row without customerCode: ${it}")
                        override val billingContact = Contact()
                        override val directDebiting = true
                    }
                }
                .toList()
        }

    private fun readBillingItems(billingItemsCSVs: Array<out File>): List<InvoiceItem> =
        billingItemsCSVs.flatMap {
            semicolonSeparatedFileReader().open(it) {
                readAllWithHeaderAsSequence()
                    .map {
                        object : InvoiceItem {
                            override val customerCode = it["customerCode"]
                                ?: error("billing-item-row without customerCode: ${it}")
                            override val netAmount = BigDecimal(it["netAmount"])
                        }
                    }
                    .toList()
            }
        }


    private fun semicolonSeparatedFileReader() =
        csvReader {
            charset = "UTF-8"
            quoteChar = '"'
            delimiter = ';'
            escapeChar = '\\'
        }

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
