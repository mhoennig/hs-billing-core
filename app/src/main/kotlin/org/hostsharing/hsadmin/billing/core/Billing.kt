package org.hostsharing.hsadmin.billing.core

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import org.hostsharing.hsadmin.billing.core.domain.*
import org.hostsharing.hsadmin.billing.core.lib.Format
import org.hostsharing.hsadmin.billing.core.reader.VatGroup
import org.hostsharing.hsadmin.billing.core.reader.readBillingItems
import org.hostsharing.hsadmin.billing.core.reader.readCustomers
import org.hostsharing.hsadmin.billing.core.reader.readVatGroups
import org.hostsharing.hsadmin.billing.core.writer.InvoiceWriter
import java.io.File
import java.io.FileWriter
import java.lang.RuntimeException
import java.math.BigDecimal
import java.time.LocalDate

class Billing(
    val configuration: Configuration,
    val periodEndDate: LocalDate,
    val billingDate: LocalDate,
    val startInvoiceNumber: Int,
    val vatGroupsCSV: File,
    val customersCSV: File,
    vararg billingItemsCSVs: File,
) {
    companion object {
        private const val BOOKINGS_TEMPLATE = "bookings.csv.vm"
    }

    val customers: List<Customer> = readCustomers(customersCSV)
    val billingItems: List<BillingItem> = readBillingItems(billingItemsCSVs)
    val vatGroups: Map<String, VatGroup> = readVatGroups(vatGroupsCSV)
    val invoices: List<Invoice> = generateInvoices(customers, billingItems)

    private fun generateInvoices(customers: List<Customer>, billingItems: List<BillingItem>): List<Invoice> =
        customers.foldIndexed(emptyList()) { index, invoices, customer ->
            invoices + (object : Invoice {
                override val documentNumber = "${billingDate.format(Format.year)}-${startInvoiceNumber + index}-${customer.number}"
                override val documentDate = billingDate
                override val customer = customer
                override val referenceDate = periodEndDate
                override val dueDate = this.documentDate.plusDays(30)
                override val directDebiting = this.customer.directDebiting
                override val items = billingItems
                    .filter { it.customerCode == customer.code }
                    .map { object: InvoiceItem, BillingItem by it {
                        override val grossAmount = netAmount * vatFactor(it.vatGroupId, customer.countryCode)
                    }}
            })
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

    private fun vatFactor(vatGroupId: String, countryCode: String): BigDecimal =
        try {
            BigDecimal.ONE + vatGroups[vatGroupId]!!.rates[countryCode]!!.percentage / BigDecimal(100)
        } catch (exc: Exception) {
            throw RuntimeException( "cannot find VAT for vatGroupId:${vatGroupId} and countryCode=${countryCode}")
        }
}
