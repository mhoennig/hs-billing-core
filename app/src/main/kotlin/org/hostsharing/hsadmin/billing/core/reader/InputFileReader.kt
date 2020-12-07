package org.hostsharing.hsadmin.billing.core.reader

import com.github.doyaaaaaken.kotlincsv.client.CsvFileReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import org.hostsharing.hsadmin.billing.core.domain.BillingItem
import org.hostsharing.hsadmin.billing.core.domain.Customer
import org.hostsharing.hsadmin.billing.core.domain.VatRate
import org.hostsharing.hsadmin.billing.core.domain.isCountryCode
import org.hostsharing.hsadmin.billing.core.lib.withContext
import java.io.File
import java.math.BigDecimal

fun readCustomers(customersCSV: File): List<Customer> =
    inputFileReader("reading customers", customersCSV) {
        readAllWithHeaderAsSequence()
            .map { CustomerParser.parse(it) }
            .toList()
    }

fun readVatGroups(vatGroupsCSV: File): Map<String, VatGroupDef> =
    inputFileReader("readVatGroups: ", vatGroupsCSV) {
        readAllWithHeaderAsSequence()
            .map { VatGroupDefParser.parse(it) }
            .map { it.id to it }
            .toMap()
    }

fun readBillingItems(billingItemsCSVs: Array<out File>): List<BillingItem> =
    billingItemsCSVs.flatMap {
        inputFileReader("reading billing items", it) {
            readAllWithHeaderAsSequence()
                .map {
                    object : BillingItem {
                        override val customerCode = it["customerCode"]
                            ?: error("billing-item-row without customerCode: ${it}")
                        override val vatGroupId = it["vatGroupId"]
                            ?: error("billing-item-row without vatGroupId: ${it}")
                        override val netAmount = BigDecimal(it["netAmount"])
                    }
                }
                .toList()
        }
    }

fun <T> inputFileReader(title: String, file: File, read: CsvFileReader.() -> T): T =
    withContext("${title}: ${file.name}") {
        csvReader {
            charset = "UTF-8"
            quoteChar = '"'
            delimiter = ';'
            escapeChar = '\\'
        }.open(file) { read() }
    }

