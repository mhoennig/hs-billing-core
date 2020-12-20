package org.hostsharing.hsadmin.billing.core.domain

import org.hostsharing.hsadmin.billing.core.lib.Configuration
import org.hostsharing.hsadmin.billing.core.lib.validationError

data class CustomerVatBase(
    val vatCountryCode: CountryCode,
    val vatChargeMode: VatChargeMode,
    val uidVat: String? = null
) : Formattable {

    init {
        if (!uidVat.isNullOrEmpty() && !uidVat.startsWith(vatCountryCode)) {
            validationError("UID-VAT $uidVat does not match vatCountryCode $vatCountryCode")
        }
    }

    override fun format(indent: Int): String = """
        |vatCountryCode=${vatCountryCode.quoted}
        |vatChargeMode=${vatChargeMode.quoted}
        |uidVat=${uidVat.quoted}
        """
}

enum class VatChargeMode(
    val code: String
) {

    DOMESTIC("domestic") {
        override fun validate(config: Configuration, vatCountryCode: CountryCode) =
            validateDomesticCountryCode(config, vatCountryCode)
    },
    EU_DIRECT("EU-direct") {
        override fun validate(config: Configuration, vatCountryCode: CountryCode) =
            validateNonDomesticEuCountryCode(config, vatCountryCode)
    },
    EU_REVERSE("EU-reverse") {
        override fun validate(config: Configuration, vatCountryCode: CountryCode) =
            validateNonDomesticEuCountryCode(config, vatCountryCode)
    },
    NON_EU_REVERSE("NonEU-reverse") {
        override fun validate(config: Configuration, vatCountryCode: CountryCode) =
            validateNonEuCountryCode(config, vatCountryCode)
    };

    companion object {
        fun ofCode(code: String): VatChargeMode =
            values().firstOrNull { it.code == code }
                ?: error("unknown vatChargeMode '$code'")
    }

    abstract fun validate(config: Configuration, vatCountryCode: CountryCode)

    protected fun validateDomesticCountryCode(config: Configuration, vatCountryCode: CountryCode) {
        if (!config.domesticCountryCodes.contains(vatCountryCode)) {
            error(countryCodeValidationError(vatCountryCode))
        }
    }

    protected fun validateNonDomesticEuCountryCode(config: Configuration, vatCountryCode: CountryCode) {
        if (!config.euCountryCodes.contains(vatCountryCode)) {
            error(countryCodeValidationError(vatCountryCode))
        }
    }

    protected fun validateNonEuCountryCode(config: Configuration, vatCountryCode: CountryCode) {
        if (config.domesticCountryCodes.contains(vatCountryCode) ||
            config.euCountryCodes.contains(vatCountryCode)
        ) {
            error(countryCodeValidationError(vatCountryCode))
        }
    }

    private fun countryCodeValidationError(vatCountryCode: CountryCode): String =
        "vatCountryCode '$vatCountryCode' is invalid for vatChargeMode $this"
}
