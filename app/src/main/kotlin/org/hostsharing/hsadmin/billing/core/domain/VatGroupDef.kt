package org.hostsharing.hsadmin.billing.core.domain

data class VatGroupDef(

val countryCode: CountryCode,
val id: VatGroupId,
val description: String,
val placeOfSupply: PlaceOfSupply,
val vatRate: VatRate,
val dcAccount: Account,
val rcAccount: Account
) : Formattable {

    companion object {
        val FALLBACK_VAT_COUNTRY_CODE = "*"
    }

    override fun format(indent: Int): String = """
        |countryCode=${countryCode.quoted}
        |id=${id.quoted}
        |description=${description.quoted}
        |placeOfSupply=$placeOfSupply
        |vatRate=${vatRate.quoted}
        |dcAccount=${dcAccount.quoted}
        |rcAccount=${rcAccount.quoted}
        """
}

typealias VatGroupId = String

enum class PlaceOfSupply(val code: String) {
    NOT_APPLICABLE("n/a"),
    SUPPLIER("supplier"),
    RECEIVER("receiver");

    companion object {
        fun ofCode(code: String): PlaceOfSupply =
            PlaceOfSupply.values().firstOrNull { it.code == code }
                ?: error("unknown vatChargeMode '$code'")
    }
}
