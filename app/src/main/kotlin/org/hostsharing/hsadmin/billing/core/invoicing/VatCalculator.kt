package org.hostsharing.hsadmin.billing.core.invoicing

import org.hostsharing.hsadmin.billing.core.domain.CustomerVatBase
import org.hostsharing.hsadmin.billing.core.domain.VatGroupDef
import org.hostsharing.hsadmin.billing.core.domain.VatGroupDefs
import org.hostsharing.hsadmin.billing.core.domain.VatGroupId
import org.hostsharing.hsadmin.billing.core.domain.VatResult
import org.hostsharing.hsadmin.billing.core.lib.Configuration
import org.hostsharing.hsadmin.billing.core.lib.withDomainContext

class VatCalculator(val config: Configuration) {

    fun determineEffectiveRate(
        vatGroupDefs: VatGroupDefs,
        vatGroupId: VatGroupId,
        customerVatBase: CustomerVatBase
    ): VatResult =
        withDomainContext("calculating VAT by vatGroupId='$vatGroupId', vatCountryCode='${customerVatBase.vatCountryCode}', vatChargeMode='${customerVatBase.vatChargeMode}'") {
            val vatCountryGroup = vatGroupDefs.byCountryCode(customerVatBase.vatCountryCode)
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
