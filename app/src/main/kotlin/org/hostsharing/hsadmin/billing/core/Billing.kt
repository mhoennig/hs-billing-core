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
    val customers: List<Customer> = readCustomers(customersCSV)

    val invoices: List<Invoice> by lazy {
        InvoiceGenerator(
            configuration = configuration,
            periodEndDate = periodEndDate,
            billingDate = billingDate,
            startInvoiceNumber = startInvoiceNumber,
            vatGroupDefs = readVatGroups(vatGroupsCSV),
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

private class FileBasedConfiguration(val configFile: File) : Configuration {
    private val config: Properties = Properties().also { config ->
        withDomainContext("reading configuration: $configFile") {
            FileInputStream(configFile).use { configFile ->
                config.load(configFile)
            }
        }
    }

    override val templatesDirectory =
        config.getProperty("templatesDirectory")
    override val outputDirectory =
        config.getProperty("outputDirectory")
}

fun main(args: Array<String>) {
    println("Hello, World!")
}
