package org.hostsharing.hsadmin.billing.core.domain

import org.hostsharing.hsadmin.billing.core.lib.Configuration
import org.hostsharing.hsadmin.billing.core.reader.VatResult

enum class VatChargeMode(
    val code: String,
    val needsVatRate: Boolean
) {

    DOMESTIC("domestic", true) {
        override fun calculateVat(config: Configuration, vatGroupDef: VatGroupDef, vatCountryCode: String): VatResult {
            validateDomesticCountryCode(config, vatCountryCode)
            return determineDirectChargeVat(vatGroupDef)
        }
    },
    EU_DIRECT("EU-direct", true) {
        override fun calculateVat(config: Configuration, vatGroupDef: VatGroupDef, vatCountryCode: String): VatResult {
            validateNonDomesticCountryCode(config, vatCountryCode)
            return determineDirectChargeVat(vatGroupDef)
        }
    },
    EU_REVERSE("EU-reverse", false) {
        override fun calculateVat(config: Configuration, vatGroupDef: VatGroupDef, vatCountryCode: String): VatResult {
            validateNonDomesticCountryCode(config, vatCountryCode)
            return if (vatGroupDef.vatRate.noTax)
                determineDirectChargeVat(vatGroupDef)
            else
                determineReverseChargeVat(config, vatGroupDef)
        }
    },
    NON_EU_REVERSE("NonEU-reverse", false) {
        override fun calculateVat(config: Configuration, vatGroupDef: VatGroupDef, vatCountryCode: String): VatResult {
            validateNonDomesticCountryCode(config, vatCountryCode)
            return if (vatGroupDef.vatRate.noTax)
                determineDirectChargeVat(vatGroupDef)
            else
                determineReverseChargeVat(config, vatGroupDef)
        }
    };

    companion object {
        fun ofCode(code: String): VatChargeMode =
            values().firstOrNull { it.code == code }
                ?: error("unknown vatChargeMode '$code'")
    }

    abstract fun calculateVat(config: Configuration, vatGroupDef: VatGroupDef, vatCountryCode: String): VatResult

    protected fun validateDomesticCountryCode(config: Configuration, vatCountryCode: String) {
        if (vatCountryCode != config.domesticCountryCode) {
            error("vatCountryCode '$vatCountryCode' is invalid for vatChargeMode $this")
        }
    }

    protected fun validateNonDomesticCountryCode(config: Configuration, vatCountryCode: String) {
        if (vatCountryCode == config.domesticCountryCode) {
            error("vatCountryCode '$vatCountryCode' is invalid for vatChargeMode $this")
        }
    }

    protected fun determineDirectChargeVat(vatGroupDef: VatGroupDef): VatResult {
        if (vatGroupDef.vatRate.unknown) {
            error("VAT rate cannot be determined from ${vatGroupDef.format(0)}")
        }
        return VatResult(vatGroupDef.vatRate, vatGroupDef.dcAccount)
    }

    protected fun determineReverseChargeVat(config: Configuration, vatGroupDef: VatGroupDef): VatResult {
        return VatResult(VatRate.NO_TAX, vatGroupDef.rcAccount)
    }
}
