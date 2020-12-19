package org.hostsharing.hsadmin.billing.core.domain

import org.hostsharing.hsadmin.billing.core.lib.Configuration

class VatGroupDefs(
    private val configuration: Configuration,
    val vatGroupDefsByCountryCode: Map<CountryCode, Map<VatGroupId, VatGroupDef>>
) {
    fun lookup(countryCode: CountryCode, vatGroupId: VatGroupId): VatGroupDef {
        val vatGroupDefsForCountry = vatGroupDefsByCountryCode.get(countryCode)
            ?: error("no VAT group def found for '$countryCode' in '${vatGroupDefsByCountryCode.keys}'")
        return vatGroupDefsForCountry.get(vatGroupId)
            ?: error(
                "no VAT group def found for '$vatGroupId' in '${vatGroupDefsForCountry.keys}' " +
                    "for country '$countryCode'"
            )
    }

    /**
     * Resolves references to 'domestic' in vatRate fields.
     *
     * <p>The replacement value is taken from
     * {@link org.hostsharing.hsadmin.billing.core.lib.Configuration#domesticCountryCode}.</p>
     *
     * <p>Using 'domestic' reference instead of the value makes it easier to maintain the
     * VAT group definitions, especially if the value vor the domestic accidentally is the same
     * as some VAT rate for other countries, you could not even use search and replace if it changes.</p>
     *
     * @return a new instance with all references resolved
     */
    fun resolveVatRateReferences(): VatGroupDefs =
        VatGroupDefs(
            configuration,
            vatGroupDefsByCountryCode.map { countryEntry ->
                countryEntry.key to countryEntry.value.mapValues {
                    if (it.value.vatRate.domestic) {
                        it.value.copy(vatRate = lookup(configuration.domesticCountryCode, it.value.id).vatRate)
                    } else {
                        it.value
                    }
                }
            }.toMap()
        )

    fun byCountryCode(vatCountryCode: String): Map<VatGroupId, VatGroupDef> =
        vatGroupDefsByCountryCode[vatCountryCode]
            ?: vatGroupDefsByCountryCode[VatGroupDef.FALLBACK_VAT_COUNTRY_CODE]
            ?: error("vatCountryCode '${vatCountryCode}' not found in ${vatGroupDefsByCountryCode.keys}")
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
        const val FALLBACK_VAT_COUNTRY_CODE = "*"
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
            values().firstOrNull { it.code == code }
                ?: error("unknown vatChargeMode '$code'")
    }
}
