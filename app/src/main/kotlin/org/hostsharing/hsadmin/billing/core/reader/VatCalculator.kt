package org.hostsharing.hsadmin.billing.core.reader

import org.hostsharing.hsadmin.billing.core.domain.*
import org.hostsharing.hsadmin.billing.core.lib.Configuration
import org.hostsharing.hsadmin.billing.core.lib.withDomainContext

class VatCalculator(val config: Configuration) {

    fun calculateEffectiveRate(
        vatGroupDefs: Map<CountryCode, Map<VatGroupId, VatGroupDef>>,
        vatGroupId: VatGroupId,
        vatBase: VatBase
    ): VatResult =
        withDomainContext("calculating VAT by vatGroupId='$vatGroupId', vatCountryCode='${vatBase.vatCountryCode}', vatChargeMode='${vatBase.vatChargeMode}'") {
            val vatCountryGroup = vatGroupDefs[vatBase.vatCountryCode]
                ?: vatGroupDefs[VatGroupDef.FALLBACK_VAT_COUNTRY_CODE]
                ?: error("vatCountryCod '${vatBase.vatCountryCode}' not found in ${vatGroupDefs.keys}")
            val vatGroupDef = vatCountryGroup[vatGroupId]
                ?: error("vatGroupId '$vatGroupId' not found in ${vatGroupDefs.keys}")
            vatBase.vatChargeMode
                .calculateVat(config, vatGroupDef, vatBase.vatCountryCode)
        }

    fun calculateEffectiveRate(vatGroupDef: VatGroupDef, vatBase: VatBase): VatResult =
        withDomainContext("calculating VAT by vatGroupId='${vatGroupDef.id}', vatCountryCode='${vatBase.vatCountryCode}', vatChargeMode='${vatBase.vatChargeMode}'") {
            vatBase.vatChargeMode.calculateVat(config, vatGroupDef, vatBase.vatCountryCode)
        }
}

