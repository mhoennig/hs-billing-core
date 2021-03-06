package org.hostsharing.hsadmin.billing.core.domain

interface Customer : Formattable {

    val number: Int
    val code: String
    val billingContact: Contact
    val sepa: Sepa
    val vatBase: CustomerVatBase

    override fun format(indent: Int): String = """
        |number=${number.quoted}
        |code=${code.quoted}
        |billingContact={
        |${billingContact.formatted(indent + 4)}
        |}
        |sepa={
        |${sepa.formatted(indent + 4)}
        |}
        |vatBase={
        |${vatBase.formatted(indent + 4)}
        |}
        """
}
