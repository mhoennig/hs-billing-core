package org.hostsharing.hsadmin.billing.core.domain

interface Customer: Formattable {

    val number: Int
    val code: String
    val billingContact: Contact
    val sepa: Sepa
    val vatChargeCode: VatChargeCode
    val uidVat: String?

    override fun format(indent: Int): String = """
        |number="${number}"
        |code="${code}"
        |billingContact={
        |${billingContact.formatted(indent+4)}
        |}
        |sepa={
        |${sepa.formatted(indent+4)}
        |}
        |vatChargeCode="${vatChargeCode}"
        |uidVat="${uidVat}"
        """
}
