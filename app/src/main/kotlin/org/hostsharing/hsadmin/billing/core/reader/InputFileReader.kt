package org.hostsharing.hsadmin.billing.core.reader

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import org.hostsharing.hsadmin.billing.core.domain.Contact
import org.hostsharing.hsadmin.billing.core.domain.Customer
import org.hostsharing.hsadmin.billing.core.domain.InvoiceItem
import java.io.File
import java.math.BigDecimal

fun readCustomers(customersCSV: File): List<Customer> =
    semicolonSeparatedFileReader().open(customersCSV) {
        readAllWithHeaderAsSequence()
            .map {
                object : Customer {
                    override val uidVat = it["uidVat"]
                    override val number = Integer.parseInt(it["customerNumber"]
                        ?: error("customer-row without customerNumber"))
                    override val code = it["customerCode"] ?: error("customer-row without customerCode: ${it}")
                    override val billingContact = Contact()
                    override val directDebiting = true
                }
            }
            .toList()
    }

fun readBillingItems(billingItemsCSVs: Array<out File>): List<InvoiceItem> =
    billingItemsCSVs.flatMap {
        semicolonSeparatedFileReader().open(it) {
            readAllWithHeaderAsSequence()
                .map {
                    object : InvoiceItem {
                        override val customerCode = it["customerCode"]
                            ?: error("billing-item-row without customerCode: ${it}")
                        override val netAmount = BigDecimal(it["netAmount"])
                    }
                }
                .toList()
        }
    }


fun semicolonSeparatedFileReader() =
    csvReader {
        charset = "UTF-8"
        quoteChar = '"'
        delimiter = ';'
        escapeChar = '\\'
    }
