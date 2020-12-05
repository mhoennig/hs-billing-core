package org.hostsharing.hsadmin.billing.core.domain

import java.time.LocalDate

interface Invoice {
    val documentNumber: String
    val documentDate: LocalDate
    val customer: Customer

    val referenceDate: LocalDate

    val dueDate: LocalDate
    val directDebiting: Boolean

    val vatGroups: List<VatGroup>
}
