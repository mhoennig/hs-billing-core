package org.hostsharing.hsadmin.billing.core

import org.hostsharing.hsadmin.billing.core.domain.*
import org.hostsharing.hsadmin.billing.core.generator.AccountingRecordsGenerator
import org.hostsharing.hsadmin.billing.core.invoicing.InvoiceGenerator
import org.hostsharing.hsadmin.billing.core.lib.Configuration
import org.hostsharing.hsadmin.billing.core.lib.withDomainContext
import org.hostsharing.hsadmin.billing.core.reader.*
import java.io.File
import java.io.FileInputStream
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
    private val customers: List<Customer> = readCustomers(customersCSV)

    private val invoices: List<Invoice> by lazy {
        InvoiceGenerator(
            configuration = configuration,
            periodEndDate = periodEndDate,
            billingDate = billingDate,
            startInvoiceNumber = startInvoiceNumber,
            vatGroupDefs = VatGroupDefs(configuration, readVatGroups(vatGroupsCSV)).resolveVatRateReferences(),
            customers = customers,
            billingItems = readBillingItems(billingItemsCSVs)
        ).generateInvoices()
    }

    fun run() {
        generateAccountingRecordsCsv()
    }

    fun generateAccountingRecordsCsv(): File =
        AccountingRecordsGenerator(configuration).generate(invoices)
}
