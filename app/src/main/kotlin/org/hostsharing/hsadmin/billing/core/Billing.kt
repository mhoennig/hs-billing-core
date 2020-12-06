package org.hostsharing.hsadmin.billing.core

import org.hostsharing.hsadmin.billing.core.domain.*
import org.hostsharing.hsadmin.billing.core.lib.Context
import org.hostsharing.hsadmin.billing.core.lib.Format
import org.hostsharing.hsadmin.billing.core.lib.withContext
import org.hostsharing.hsadmin.billing.core.reader.*
import org.hostsharing.hsadmin.billing.core.reader.VatGroupDef
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
    val vatGroupDefDefs: Map<String, VatGroupDef> = readVatGroups(vatGroupsCSV)
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
                override val vatGroups = billingItems
                    .filter { it.customerCode == customer.code }
                    .map {
                        InvoiceItemData(
                            vatGroupDefDefs,
                            customerCountryCode = customer.countryCode,
                            billingItem = it
                        )
                    }
                    .groupBy { it.vatGroupId }
                    .map {
                        InvoiceVatGroup(
                            configuration,
                            vatGroupDefDefs[it.key] ?: error("vatGroup ${it.key} not found"),
                            customer,
                            vatAmount = it.value.fold(BigDecimal.ZERO) { acc, value -> acc + value.vatAmount },
                            netAmount = it.value.fold(BigDecimal.ZERO) { acc, value -> acc + value.netAmount },
                            grossAmount = it.value.fold(BigDecimal.ZERO) { acc, value -> acc + value.grossAmount },
                            items = it.value)
                    }
                    .toList()
            })
        }

    fun generateBookingsCsv(bookingsCSV: File): File {

        withContext("outputFile: " + bookingsCSV.name) {
            Context.log("generating")

            val invoicePrinter = InvoiceWriter(
                configuration.templatesDirectory + "/" + BOOKINGS_TEMPLATE)

            FileWriter(bookingsCSV).use { fileWriter ->
                invoices.forEach { invoicePrinter.printInvoice(it, fileWriter) }
            }
            return bookingsCSV
        }
    }
}

class InvoiceVatGroup(
    config: Configuration,
    vatGroupDef: VatGroupDef,
    customer: Customer,
    override val vatAmount: BigDecimal,
    override val netAmount: BigDecimal,
    override val grossAmount: BigDecimal,
    override val items: List<InvoiceItem>
) : VatGroup {
    override val vatRate: VatRate = vatGroupDef.rates[customer.countryCode]!!
    override val vatAccount =
        when (true) {
            vatRate.noTax -> config.accountBaseForNonTaxableRevenues
            customer.vatChargeCode == "domestic" -> config.accountBaseForTaxableDomesticRevenues
            customer.vatChargeCode == "EU" -> config.accountBaseForTaxableForeignEuRevenues
            customer.vatChargeCode == "EU:RC" -> config.accountBaseForTaxableForeignEuRevenuesReverseCharge
            customer.vatChargeCode == "NonEU" -> config.accountBaseForTaxableForeignEuRevenues
            customer.vatChargeCode == "NonEU:RC" -> config.accountBaseForTaxableForeignEuRevenuesReverseCharge
            else -> {
                throw RuntimeException("unknown vatChargeCode ${customer.vatChargeCode}")
            }
        } + vatGroupDef.id
}

class InvoiceItemData(
    val vatGroupDefDefs: Map<String, VatGroupDef>,
    val customerCountryCode: String,
    val billingItem: BillingItem
) : InvoiceItem, BillingItem by billingItem {
    val vatRate: VatRate = vatRate()
    val vatAmount: BigDecimal = netAmount * vatRate.percentage
    override val grossAmount = netAmount + vatAmount

    private fun vatRate(): VatRate =
        try {
            vatGroupDefDefs[billingItem.vatGroupId]!!.rates[customerCountryCode]!!
        } catch (exc: Exception) {
            throw RuntimeException("cannot find VAT for vatGroupId:${vatGroupId} and countryCode=${customerCountryCode}")
        }
}

