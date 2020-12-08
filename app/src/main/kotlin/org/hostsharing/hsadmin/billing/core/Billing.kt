package org.hostsharing.hsadmin.billing.core

import org.hostsharing.hsadmin.billing.core.domain.*
import org.hostsharing.hsadmin.billing.core.generator.AccountingRecordsGenerator
import org.hostsharing.hsadmin.billing.core.invoicing.InvoiceGenerator
import org.hostsharing.hsadmin.billing.core.lib.Configuration
import org.hostsharing.hsadmin.billing.core.reader.*
import java.io.File
import java.time.LocalDate
import java.util.*

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
