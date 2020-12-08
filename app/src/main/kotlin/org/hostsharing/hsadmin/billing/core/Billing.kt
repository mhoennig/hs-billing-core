package org.hostsharing.hsadmin.billing.core

import org.hostsharing.hsadmin.billing.core.domain.Customer
import org.hostsharing.hsadmin.billing.core.domain.Invoice
import org.hostsharing.hsadmin.billing.core.generator.AccountingRecordsGenerator
import org.hostsharing.hsadmin.billing.core.invoicing.InvoiceGenerator
import org.hostsharing.hsadmin.billing.core.lib.Configuration
import org.hostsharing.hsadmin.billing.core.reader.readBillingItems
import org.hostsharing.hsadmin.billing.core.reader.readCustomers
import org.hostsharing.hsadmin.billing.core.reader.readVatGroups
import java.io.File
import java.time.LocalDate

class Billing(
    val configuration: Configuration,
    val periodEndDate: LocalDate,
    val billingDate: LocalDate,
    val startInvoiceNumber: Int,
    val vatGroupsCSV: File,
    val customersCSV: File,
    vararg val billingItemsCSVs: File,
) {
    val customers: List<Customer> = readCustomers(customersCSV)

    val invoices: List<Invoice> by lazy {
        InvoiceGenerator(
            configuration,
            periodEndDate,
            billingDate,
            startInvoiceNumber,
            readVatGroups(vatGroupsCSV),
            customers,
            readBillingItems(billingItemsCSVs)
        ).generateInvoices()
    }

    fun run() {
        generateAccountingRecordsCsv()
    }

    fun generateAccountingRecordsCsv(): File =
        AccountingRecordsGenerator(configuration).generate(invoices)
}
