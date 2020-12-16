package org.hostsharing.hsadmin.billing.core.generator

import org.hostsharing.hsadmin.billing.core.domain.Invoice
import org.hostsharing.hsadmin.billing.core.lib.Configuration
import org.hostsharing.hsadmin.billing.core.lib.withDomainContext
import org.hostsharing.hsadmin.billing.core.writer.InvoiceWriter
import java.io.File
import java.io.FileWriter

class AccountingRecordsGenerator(val configuration: Configuration) {

    private val TEMPLATE = "accounting-records.csv.vm"
    private val OUTPUT_FILE_NAME = "accounting-records.csv"

    fun generate(invoices: List<Invoice>): File {
        val outputFile = File(configuration.outputDirectory, OUTPUT_FILE_NAME)

        withDomainContext("outputFile: " + OUTPUT_FILE_NAME) {
            val invoiceWriter = InvoiceWriter(
                configuration.templatesDirectory + "/" + TEMPLATE
            )

            FileWriter(outputFile).use { fileWriter ->
                invoices.forEach { invoiceWriter.printInvoice(it, fileWriter) }
            }
        }
        return outputFile
    }
}
