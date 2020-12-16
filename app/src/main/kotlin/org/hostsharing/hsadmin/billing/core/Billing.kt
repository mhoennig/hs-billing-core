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
            vatGroupDefs = readVatGroups(vatGroupsCSV).resolveReferences(),
            customers = customers,
            billingItems = readBillingItems(billingItemsCSVs)
        ).generateInvoices()
    }

    fun run() {
        generateAccountingRecordsCsv()
    }

    fun generateAccountingRecordsCsv(): File =
        AccountingRecordsGenerator(configuration).generate(invoices)

    private fun Map<CountryCode, Map<VatGroupId, VatGroupDef>>.resolveReferences(): Map<CountryCode, Map<VatGroupId, VatGroupDef>> =
        map { countryEntry ->
            countryEntry.key to countryEntry.value.mapValues {
                if (it.value.vatRate.domestic)
                    // TODO: get rid of !!, eg by wrapping in a class and lookup through a method
                    it.value.copy(vatRate=this[configuration.domesticCountryCode]!!.get(it.value.id)!!.vatRate)
                else
                    it.value
            }
        }.toMap()
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
