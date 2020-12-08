package org.hostsharing.hsadmin.billing.core.writer

import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine
import org.apache.velocity.context.Context
import org.hostsharing.hsadmin.billing.core.domain.*
import org.hostsharing.hsadmin.billing.core.lib.Format
import org.hostsharing.hsadmin.billing.core.lib.format
import java.io.Writer
import java.math.BigDecimal

/**
 * Merges a single invoice into the given template and
 * appends the formatted output to a given [java.io.Writer].
 */
internal class InvoiceWriter(templateFilename: String) {

    class VatGroupFormatter(vatGroup: VatGroup) : VatGroup by vatGroup {
        val vatRateFormatted = (vatGroup.vatRate.percentage * BigDecimal(100)).format(Format.vatRate)
        val vatAmountFormatted = vatGroup.vatAmount.format(Format.money)
        val netAmountFormatted = vatGroup.netAmount.format(Format.money)
        val grossAmountFormatted = vatGroup.grossAmount.format(Format.money)
    }

    class InvoiceFormatter(invoice: Invoice) : Invoice by invoice {

        val referenceDateFormatted = invoice.referenceDate.format(Format.date)
        val referencePeriodFormatted = invoice.referenceDate.format(Format.datePeriod)
        val documentDateFormatted = invoice.documentDate.format(Format.date)
        val dueDateFormatted = invoice.dueDate.format(Format.date)

        override val customer: Customer = CustomerFormatter(invoice.customer)
    }

    class CustomerFormatter(customer: Customer) : Customer by customer

    private val template = initializeVelocity(templateFilename)

    private var chunk = 0

    /**
     * Makes the data of an invoice accessible from a Velocity template.
     * Root objects are "customer", "invoice" and "vatGroups".
     *
     * @param invoice to become accessible
     * @return Velocity-context with the invoice data
     */
    private fun createVelocityContext(invoice: Invoice): Context {
        val ctx: Context = VelocityContext()

        ctx.put("chunk", chunk)
        ctx.put("customer", invoice.customer)
        ctx.put("invoice", InvoiceFormatter(invoice))
        ctx.put("vatGroups", invoice.vatGroups.map { VatGroupFormatter(it) })
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
        chunk++
    }
}

private fun initializeVelocity(templateFilename: String) =
    VelocityEngine().also { it.init() }.getTemplate(templateFilename, "UTF-8")
