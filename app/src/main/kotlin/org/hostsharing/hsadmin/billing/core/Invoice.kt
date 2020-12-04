package org.hostsharing.hsadmin.billing.core

import java.time.LocalDate

interface Invoice {
    val documentNumber: String
    val documentDate: LocalDate
    val customer: Customer

    val referenceDate: LocalDate

    val dueDate: LocalDate
    val directDebiting: Boolean

    val items: List<InvoiceItem>
}
