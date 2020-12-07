package org.hostsharing.hsadmin.billing.core.domain

interface Contact: Formattable {

    val company: String?
    val salutation: String
    val title: String?
    val firstName: String
    val lastName: String
    val co: String?
    val street: String
    val zipCode: String
    val city: String
    val country: String
    val countryCode: String
    val email: String

    override fun format(indent: Int): String = """
        |company=${company.quoted}
        |salutation=${salutation.quoted}
        |title=${title.quoted}
        |firstName=${firstName.quoted}
        |lastName=${lastName.quoted}
        |co=${co.quoted}
        |street=${street.quoted}
        |zipCode=${zipCode.quoted}
        |city=${city.quoted}
        |country=${country.quoted}
        |countryCode=${countryCode.quoted}
        |email=${email.quoted}
        """
}
