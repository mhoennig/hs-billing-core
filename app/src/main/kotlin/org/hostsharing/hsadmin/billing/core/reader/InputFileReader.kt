package org.hostsharing.hsadmin.billing.core.reader

import com.github.doyaaaaaken.kotlincsv.client.CsvFileReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import org.hostsharing.hsadmin.billing.core.domain.BillingItem
import org.hostsharing.hsadmin.billing.core.domain.Contact
import org.hostsharing.hsadmin.billing.core.domain.Customer
import org.hostsharing.hsadmin.billing.core.domain.VatRate
import org.hostsharing.hsadmin.billing.core.lib.withContext
import java.io.File
import java.math.BigDecimal

fun readCustomers(customersCSV: File): List<Customer> =
    inputFileReader("reading customers", customersCSV) {
        readAllWithHeaderAsSequence()
            .map {
                object : Customer {
                    // TODO: avoid need of separate error(...) for each + avoid !!
                    // TODO: maybe automatically iterate over properties?
                    override val uidVat = it["uidVat"]
                    override val number = (it["customerNumber"]
                        ?: error("customer-row without customerNumber")).toInt()
                    override val code = it["customerCode"]
                        ?: error("customer-row without customerCode: ${it}")
                    override val billingContact = object : Contact {
                        override val company = it["company"]
                        override val salutation = it["salutation"]!!
                        override val title = it["title"]
                        override val firstName = it["firstName"]
                            ?: error("customer-row without firstName: ${it}")
                        override val lastName = it["lastName"]!!
                        override val co = it["co"]
                        override val street = it["street"]!!
                        override val zipCode = it["zipCode"]!!
                        override val city = it["city"]!!
                        override val country = it["country"]!!
                        override val email = it["email"]!!
                    }
                    override val directDebiting = (it["directDebiting"]
                        ?: error("customer-row without directDebiting: ${it}")).toBoolean()
                    override val countryCode = it["countryCode"]
                        ?: error("customer-row without countryCode: ${it}")
                    override val vatChargeCode = it["vatChargeCode"]
                        ?: error("customer-row without vatChargeCode: ${it}")
                }
            }
            .toList()
    }

fun readVatGroups(vatGroupsCSV: File): Map<String, VatGroupDef> =
    inputFileReader("readVatGroups: ", vatGroupsCSV) {
        readAllWithHeaderAsSequence()
            .map {
                object : VatGroupDef {
                    override val id = it["id"]
                        ?: error("vat-group-row without 'id' value")
                    override val description = it["description"]
                        ?: error("vat-group-row without 'description' value")
                    override val electronicService = it["electronicService"].toBoolean()
                    override val rates = it.filter { it.key.isCoutryCode() }
                        .map { it.key to VatRate(it.value) }
                        .toMap()

                }
            }
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


private fun String.isCoutryCode() =
    this.length == 2 && this == this.toUpperCase() && this[0].isLetter() && this[1].isLetter()
