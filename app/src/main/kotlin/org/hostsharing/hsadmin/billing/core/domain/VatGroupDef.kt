package org.hostsharing.hsadmin.billing.core.domain

import org.hostsharing.hsadmin.billing.core.lib.Configuration


class VatGroupDefs(
    val configuration: Configuration,
    map: Map<CountryCode, out Map<VatGroupId, VatGroupDef>>
) : Map<CountryCode, Map<VatGroupId, VatGroupDef>> by map {

    fun lookup(countryCode: CountryCode, vatGroupId: VatGroupId): VatGroupDef {
        val vatGroupDefsForCountry = get(countryCode)
            ?: error("no VAT group def found for '$countryCode' in '${keys}'")
        return vatGroupDefsForCountry.get(vatGroupId)
            ?: error("no VAT group def found for '$countryCode'.'$vatGroupId' in '${vatGroupDefsForCountry.keys}'")
    }

    /**
     * Resolves references, curren
     *
     * @return a new instance with all references resolved
     */
    fun resolveVatRateReferences(): VatGroupDefs =
        VatGroupDefs(configuration,
            map { countryEntry ->
            countryEntry.key to countryEntry.value.mapValues {
                if (it.value.vatRate.domestic)
                    it.value.copy(vatRate = lookup(configuration.domesticCountryCode, it.value.id).vatRate)
                else
                    it.value
            }
        }.toMap())
}

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
