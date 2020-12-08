package org.hostsharing.hsadmin.billing.core.invoicing

import org.hostsharing.hsadmin.billing.core.domain.*
import org.hostsharing.hsadmin.billing.core.lib.Configuration
import org.hostsharing.hsadmin.billing.core.lib.Format
import org.hostsharing.hsadmin.billing.core.reader.*
import org.hostsharing.hsadmin.billing.core.reader.VatGroupDef
import java.lang.RuntimeException
import java.math.BigDecimal
import java.time.LocalDate

class InvoiceGenerator(
    val configuration: Configuration,
    val periodEndDate: LocalDate,
    val billingDate: LocalDate,
    val startInvoiceNumber: Int,
    val vatGroupDefDefs: Map<String, VatGroupDef>,
    val customers: List<Customer>,
    val billingItems: List<BillingItem>
) {
    fun generateInvoices(): List<Invoice> =
        customers.foldIndexed(emptyList()) { index, invoices, customer ->
            invoices + (
                object : Invoice {
                    override val documentNumber = "${billingDate.format(Format.year)}-${startInvoiceNumber + index}-${customer.number}"
                    override val documentDate = billingDate
                    override val customer = customer
                    override val referenceDate = periodEndDate
                    override val dueDate = this.documentDate.plusDays(30)
                    override val directDebiting = this.customer.sepa.directDebiting
                    override val vatGroups = billingItems
                        .filter { it.customerCode == customer.code }
                        .map {
                            InvoiceItemData(
                                vatGroupDefDefs,
                                customerCountryCode = customer.billingContact.countryCode,
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
                                items = it.value
                            )
                        }
                        .toList()
                }
                )
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
    override val vatRate: VatRate = vatGroupDef.rates[customer.billingContact.countryCode]!!
    override val vatAccount =
        if (vatRate.noTax) {
            config.accountBaseForNonTaxableRevenues
        } else {
            customer.vatChargeCode.accountBase(config)
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
            throw RuntimeException("cannot find VAT for vatGroupId:$vatGroupId and countryCode=$customerCountryCode")
        }
}
