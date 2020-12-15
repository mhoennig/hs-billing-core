package org.hostsharing.hsadmin.billing.core.domain

interface VatBase : Formattable {
    val vatCountryCode: String
    val vatChargeMode: VatChargeMode

    val uidVat: String?
        get() = null

    override fun format(indent: Int): String = """
        |vatCountryCode=${vatCountryCode.quoted}
        |vatChargeMode=${vatChargeMode.quoted}
        |uidVat=${uidVat.quoted}
        """
}
