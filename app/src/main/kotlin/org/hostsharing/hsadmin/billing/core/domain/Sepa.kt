package org.hostsharing.hsadmin.billing.core.domain

interface Sepa : Formattable {
    val directDebiting: Boolean
    val bankCustomer: String?
    val bankIBAN: String?
    val bankBIC: String?
    val mandateRef: String?

    override fun format(indent: Int): String = """
        |directDebiting=${directDebiting.quoted}
        |bankCustomer=${bankCustomer.quoted}
        |bankIBAN=${bankIBAN.quoted}
        |bankBIC=${bankBIC.quoted}
        |mandateRef=${mandateRef.quoted}
        """
}
