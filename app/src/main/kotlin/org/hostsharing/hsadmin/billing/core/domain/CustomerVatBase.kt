package org.hostsharing.hsadmin.billing.core.domain

data class CustomerVatBase(
    val vatCountryCode: String,
    val vatChargeMode: VatChargeMode,
    val uidVat: String? = null
) : Formattable {

    init {
        if (!uidVat.isNullOrEmpty() && !uidVat.startsWith(vatCountryCode))
            error("UID-VAT $uidVat does not match vatCountryCode $vatCountryCode")
    }

    override fun format(indent: Int): String = """
        |vatCountryCode=${vatCountryCode.quoted}
        |vatChargeMode=${vatChargeMode.quoted}
        |uidVat=${uidVat.quoted}
        """
}
