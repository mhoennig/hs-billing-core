package org.hostsharing.hsadmin.billing.core.reader

import com.github.doyaaaaaken.kotlincsv.client.CsvFileReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import org.hostsharing.hsadmin.billing.core.domain.BillingItem
import org.hostsharing.hsadmin.billing.core.domain.Customer
import org.hostsharing.hsadmin.billing.core.lib.withDomainContext
import java.io.File

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
                .map { BillingItemParser.parse(it) }
                .toList()
        }
    }

fun <T> inputFileReader(title: String, file: File, read: CsvFileReader.() -> T): T =
    withDomainContext("$title: ${file.name}") {
        csvReader {
            delimiter = ';'
        }.open(file) { read() }
    }
