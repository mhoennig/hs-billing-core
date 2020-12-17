package org.hostsharing.hsadmin.billing.core.invoicing

import org.hostsharing.hsadmin.billing.core.domain.*
import org.hostsharing.hsadmin.billing.core.lib.Configuration
import org.hostsharing.hsadmin.billing.core.lib.Format
import org.hostsharing.hsadmin.billing.core.reader.VatCalculator
import java.math.BigDecimal
import java.time.LocalDate

class InvoiceGenerator(
    val configuration: Configuration,
    val periodEndDate: LocalDate,
    val billingDate: LocalDate,
    val startInvoiceNumber: Int,
    val vatGroupDefs: VatGroupDefs,
    val customers: List<Customer>,
    val billingItems: List<BillingItem>
) {
    fun generateInvoices(): List<Invoice> =
        customers.foldIndexed(emptyList()) { index, invoices, customer ->
            invoices +
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
                            val vatResult = VatCalculator(configuration)
                                .determineEffectiveRate(
                                    vatGroupDefs,
                                    it.vatGroupId,
                                    customer.vatBase
                                )
                            InvoiceItemData(
                                vatCountryCode = customer.vatBase.vatCountryCode,
                                vatChargeMode = customer.vatBase.vatChargeMode,
                                vatRate = vatResult.vatRate.percentage,
                                vatAccount = vatResult.vatAccount,
                                billingItem = it
                            )
                        }
                        .groupBy { it.vatGroupId }
                        .map {
                            InvoiceVatGroup(
                                config = configuration,
                                vatGroupDef = vatGroupDefs[customer.vatBase.vatCountryCode]
                                    ?.get(it.key) ?: error("vatGroupDef ${customer.vatBase.vatCountryCode}.${it.key} not found"),
                                customer = customer,
                                vatAmount = it.value.fold(BigDecimal.ZERO) { acc, value -> acc + value.vatAmount },
                                netAmount = it.value.fold(BigDecimal.ZERO) { acc, value -> acc + value.netAmount },
                                grossAmount = it.value.fold(BigDecimal.ZERO) { acc, value -> acc + value.grossAmount },
                                items = it.value
                            )
                        }
                        .toList()
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
    val vatResult = VatCalculator(config)
        .determineEffectiveRate(vatGroupDef, customer.vatBase)
    override val vatRate: BigDecimal = vatResult.vatRate.percentage
    override val vatAccount: String = vatResult.vatAccount
}

class InvoiceItemData(
    val vatCountryCode: String,
    val vatChargeMode: VatChargeMode,
    val vatRate: BigDecimal,
    val vatAccount: String,
    val billingItem: BillingItem
) : InvoiceItem, BillingItem by billingItem {
    val vatAmount: BigDecimal = netAmount * vatRate
    override val grossAmount = netAmount + vatAmount
}
