package org.hostsharing.hsadmin.billing.core.invoicing

import org.hostsharing.hsadmin.billing.core.domain.CustomerVatBase
import org.hostsharing.hsadmin.billing.core.domain.VatChargeMode
import org.hostsharing.hsadmin.billing.core.domain.VatGroupDef
import org.hostsharing.hsadmin.billing.core.domain.VatGroupDefs
import org.hostsharing.hsadmin.billing.core.domain.VatGroupId
import org.hostsharing.hsadmin.billing.core.domain.VatRate
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
            determineEffectiveRateImpl(customerVatBase, vatGroupDef)
        }

    fun determineEffectiveRate(vatGroupDef: VatGroupDef, customerVatBase: CustomerVatBase): VatResult =
        withDomainContext("calculating VAT by vatGroupId='${vatGroupDef.id}', vatCountryCode='${customerVatBase.vatCountryCode}', vatChargeMode='${customerVatBase.vatChargeMode}'") {
            determineEffectiveRateImpl(customerVatBase, vatGroupDef)
        }

    private fun determineEffectiveRateImpl(
        customerVatBase: CustomerVatBase,
        vatGroupDef: VatGroupDef
    ): VatResult {
        customerVatBase.vatChargeMode.validate(config, customerVatBase.vatCountryCode)
        return when (customerVatBase.vatChargeMode) {
            VatChargeMode.DOMESTIC -> {
                determineDirectChargeVat(vatGroupDef)
            }
            VatChargeMode.EU_DIRECT -> {
                determineDirectChargeVat(vatGroupDef)
            }
            VatChargeMode.EU_REVERSE -> {
                if (vatGroupDef.vatRate.noTax) {
                    determineDirectChargeVat(vatGroupDef)
                } else {
                    determineReverseChargeVat(vatGroupDef)
                }
            }
            VatChargeMode.NON_EU_REVERSE -> {
                if (vatGroupDef.vatRate.noTax) {
                    determineDirectChargeVat(vatGroupDef)
                } else {
                    determineReverseChargeVat(vatGroupDef)
                }
            }
        }
    }

    private fun determineDirectChargeVat(vatGroupDef: VatGroupDef): VatResult {
        if (vatGroupDef.vatRate.unknown) {
            error("VAT rate cannot be determined from ${vatGroupDef.format(0)}")
        }
        return VatResult(vatGroupDef.vatRate, vatGroupDef.dcAccount)
    }

    private fun determineReverseChargeVat(vatGroupDef: VatGroupDef): VatResult =
        VatResult(VatRate.NO_TAX, vatGroupDef.rcAccount)
}
