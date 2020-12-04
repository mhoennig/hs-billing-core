package org.hostsharing.hsadmin.billing.core.domain

class Contact {

    val company = "Tästmann GmbH"
    val firstName = "Tästi"
    val salutation = "Herr"
    val title: String? = null
    val lastName = "Tästmann"
    val co: String? = null
    val street: String = ""
    val zipCode: String = ""
    val city: String = ""
    val country: String = ""
    val email: String = ""

    fun isInGermany() = country == "DE"
}
