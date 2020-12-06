package org.hostsharing.hsadmin.billing.core.domain

interface Contact {

    val company: String?
    val firstName: String
    val salutation: String
    val title: String?
    val lastName: String
    val co: String?
    val street: String
    val zipCode: String
    val city: String
    val country: String
    val email: String
}
