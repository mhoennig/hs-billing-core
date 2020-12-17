package org.hostsharing.hsadmin.billing.core.generator

import org.hostsharing.hsadmin.billing.core.domain.Invoice
import org.hostsharing.hsadmin.billing.core.lib.Configuration
import org.hostsharing.hsadmin.billing.core.lib.withDomainContext
import org.hostsharing.hsadmin.billing.core.writer.InvoiceWriter
import java.io.File
import java.io.FileWriter

class AccountingRecordsGenerator(val configuration: Configuration) {

    private val template = "accounting-records.csv.vm"
    private val outputFileName = "accounting-records.csv"

    fun generate(invoices: List<Invoice>): File {
        val outputFile = File(configuration.outputDirectory, outputFileName)

        withDomainContext("outputFile: " + outputFileName) {
            val invoiceWriter = InvoiceWriter(
                configuration.templatesDirectory + "/" + template
            )

            FileWriter(outputFile).use { fileWriter ->
                invoices.forEach { invoiceWriter.printInvoice(it, fileWriter) }
            }
        }
        return outputFile
    }
}
