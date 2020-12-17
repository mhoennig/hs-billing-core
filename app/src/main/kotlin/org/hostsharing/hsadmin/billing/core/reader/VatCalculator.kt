// TODO: wrong package
package org.hostsharing.hsadmin.billing.core.reader

import org.hostsharing.hsadmin.billing.core.domain.*
import org.hostsharing.hsadmin.billing.core.lib.Configuration
import org.hostsharing.hsadmin.billing.core.lib.withDomainContext

class VatCalculator(val config: Configuration) {

    fun determineEffectiveRate(
        vatGroupDefs: VatGroupDefs,
        vatGroupId: VatGroupId,
        customerVatBase: CustomerVatBase
    ): VatResult =
        withDomainContext("calculating VAT by vatGroupId='$vatGroupId', vatCountryCode='${customerVatBase.vatCountryCode}', vatChargeMode='${customerVatBase.vatChargeMode}'") {
            val vatCountryGroup = vatGroupDefs[customerVatBase.vatCountryCode]
                ?: vatGroupDefs[VatGroupDef.FALLBACK_VAT_COUNTRY_CODE]
                ?: error("vatCountryCode '${customerVatBase.vatCountryCode}' not found in ${vatGroupDefs.keys}")
            val vatGroupDef = vatCountryGroup[vatGroupId]
                ?: error("vatGroupId '$vatGroupId' not found in ${vatCountryGroup.keys}")
            customerVatBase.vatChargeMode
                .calculateVat(config, vatGroupDef, customerVatBase.vatCountryCode)
        }

    fun determineEffectiveRate(vatGroupDef: VatGroupDef, customerVatBase: CustomerVatBase): VatResult =
        withDomainContext("calculating VAT by vatGroupId='${vatGroupDef.id}', vatCountryCode='${customerVatBase.vatCountryCode}', vatChargeMode='${customerVatBase.vatChargeMode}'") {
            customerVatBase.vatChargeMode.calculateVat(config, vatGroupDef, customerVatBase.vatCountryCode)
        }
}
