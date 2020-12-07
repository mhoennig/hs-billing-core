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
        |company="${company}"
        |salutation="${salutation}"
        |title="${title}"
        |firstName="${firstName}"
        |lastName="${lastName}"
        |co="${co}"
        |street="${street}"
        |zipCode="${zipCode}"
        |city="${city}"
        |country="${country}"
        |countryCode="${countryCode}"
        |email="${email}"
        """
}
