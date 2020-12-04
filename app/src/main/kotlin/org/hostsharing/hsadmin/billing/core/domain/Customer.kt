package org.hostsharing.hsadmin.billing.core.domain

interface Customer {

    val uidVat: String?
    val number: Int
    val code: String
    val billingContact: Contact
}
