package org.hostsharing.hsadmin.billing.core.domain

import org.hostsharing.hsadmin.billing.core.lib.Configuration
import kotlin.reflect.KProperty1

enum class VatChargeCode(val code: String, val accountBaseGetter: KProperty1<Configuration, String>) {

    DOMESTIC("domestic", Configuration::accountBaseForTaxableDomesticRevenues),
    EU("EU", Configuration::accountBaseForTaxableForeignEuRevenues),
    EU_RC("EU-RC", Configuration::accountBaseForTaxableForeignEuRevenuesReverseCharge),
    NON_EU_RC("NonER-RC", Configuration::accountBaseForTaxableAbroadEuRevenuesReverseCharge);

    companion object {
        fun ofCode(code: String): VatChargeCode =
            values().firstOrNull { it.code == code }
                ?: error("unknown vatChargeCode '${code}'")

    }

    fun accountBase(config: Configuration): String = accountBaseGetter.get(config)
}
