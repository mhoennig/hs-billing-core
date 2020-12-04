package org.hostsharing.hsadmin.billing.core

import org.apache.velocity.Template
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine
import org.apache.velocity.context.Context
import java.io.Writer
import java.math.BigDecimal
import java.util.*

/**
 * Merges a single invoice into the given template and
 * appends the formatted output to a given [java.io.Writer].
 */
internal class InvoicePrinter(templateFilename: String?) {

    interface VatGroup {
        val vatRate: BigDecimal
        val vatAmount: BigDecimal
        val netAmount: BigDecimal
        val grossAmount: BigDecimal
        val vatAccount: String
    }

    class VatGroupFormatter(vatGroup: VatGroup) : VatGroup by vatGroup {
        val vatRateFormatted = vatGroup.vatRate.format(Format.vatRate)
        val vatAmountFormatted = vatGroup.vatAmount.format(Format.money)
        val netAmountFormatted = vatGroup.netAmount.format(Format.money)
        val grossAmountFormatted = vatGroup.grossAmount.format(Format.money)
    }

    class InvoiceFormatter(invoice: Invoice) : Invoice by invoice {

        val referenceDateFormatted = invoice.referenceDate.format(Format.date)
        val referencePeriodFormatted = invoice.referenceDate.format(Format.datePeriod)
        val documentDateFormatted = invoice.documentDate.format(Format.date)
        val dueDateFormatted = invoice.dueDate.format(Format.date)

        override val customer: Customer = Customer()
    }

    private val template: Template

    /**
     * Makes the data of an invoice accessible from a Velocity template.
     * Root objects are "customer", "invoice" and "vatGroups".
     *
     * @param invoice to become accessible
     * @return Velocity-context with the invoice data
     */
    private fun createVelocityContext(invoice: Invoice): Context {
        val ctx: Context = VelocityContext()

        ctx.put("customer", invoice.customer)
        ctx.put("invoice", InvoiceFormatter(invoice))
        ctx.put("vatGroups",
            arrayListOf(
                VatGroupFormatter(object : VatGroup {
                    override val vatRate = BigDecimal("16.00")
                    override val vatAmount = BigDecimal("30.00")
                    override val netAmount = BigDecimal("206.66")
                    override val grossAmount = BigDecimal("236.66")
                    override val vatAccount = "440006"
                }),
                VatGroupFormatter(object : VatGroup {
                    override val vatRate = BigDecimal("0.00")
                    override val vatAmount = BigDecimal("0.00")
                    override val netAmount = BigDecimal("10.00")
                    override val grossAmount = BigDecimal("10.00")
                    override val vatAccount = "420000"
                })
            ))
        return ctx
    }

    /**
     * Merges the given [invoice] into the Velocity-template given in the constructor.
     *
     * @param invoice the invoice to process
     * @param writer will be used to append the formatted invoice.
     */
    fun printInvoice(invoice: Invoice, writer: Writer?) {
        template.merge(createVelocityContext(invoice), writer)
    }

    init {
        // initialize Velocity with the given template
        template = VelocityEngine().also { it.init() }.getTemplate(templateFilename, "UTF-8")
    }
}
