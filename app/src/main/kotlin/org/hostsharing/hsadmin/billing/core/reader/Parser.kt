package org.hostsharing.hsadmin.billing.core.reader

import org.hostsharing.hsadmin.billing.core.domain.PlaceOfSupply
import org.hostsharing.hsadmin.billing.core.domain.VatChargeMode
import org.hostsharing.hsadmin.billing.core.domain.VatRate
import org.hostsharing.hsadmin.billing.core.domain.isCountryCode
import java.math.BigDecimal

open class Parser(val contextInfo: String) {

    protected fun Map<String, String?>.mandatoryString(fieldName: String): String =
        get(fieldName) ?: error("$contextInfo without $fieldName")

    protected fun Map<String, String?>.optionalString(fieldName: String): String? =
        get(fieldName)

    protected fun Map<String, String?>.mandatoryInt(fieldName: String): Int =
        mandatoryString(fieldName).toInt()

    protected fun Map<String, String?>.mandatoryBigDecimal(fieldName: String): BigDecimal =
        mandatoryString(fieldName).toBigDecimal()

    protected fun Map<String, String?>.mandatoryBoolean(fieldName: String): Boolean =
        mandatoryString(fieldName).toBoolean()

    protected fun Map<String, String?>.mandatoryCountryCode(fieldName: String): String {
        val fieldValue = mandatoryString(fieldName)
        if (!fieldValue.isCountryCode()) {
            error("$contextInfo with $fieldName='$fieldValue' not a valid country code")
        }
        return fieldValue
    }

    protected fun Map<String, String?>.mandatoryVatChargeMode(fieldName: String): VatChargeMode {
        val fieldValue = mandatoryString(fieldName)
        try {
            return VatChargeMode.ofCode(fieldValue)
        } catch (exc: Exception) {
            error("$contextInfo with $fieldName='$fieldValue' not a valid VAT charge code")
        }
    }

    protected fun Map<String, String?>.mandatoryPlaceOfSupply(fieldName: String): PlaceOfSupply {
        val fieldValue = mandatoryString(fieldName)
        try {
            return PlaceOfSupply.ofCode(fieldValue)
        } catch (exc: Exception) {
            error("$contextInfo with $fieldName='$fieldValue' not a valid PlaceOfSupply")
        }
    }

    protected fun Map<String, String?>.mandatorVatRate(fieldName: String): VatRate {
        val fieldValue = mandatoryString(fieldName)
        try {
            return VatRate(fieldValue)
        } catch (exc: Exception) {
            error("$contextInfo with $fieldName='$fieldValue' not a valid VatRate")
        }
    }
}
