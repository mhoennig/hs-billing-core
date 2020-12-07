package org.hostsharing.hsadmin.billing.core.reader

import org.hostsharing.hsadmin.billing.core.domain.VatChargeCode
import org.hostsharing.hsadmin.billing.core.domain.isCountryCode

open class Parser(val contextInfo: String) {

    protected fun Map<String, String?>.mandatoryString(fieldName: String): String =
        get(fieldName) ?: error("${contextInfo} without ${fieldName}")

    protected fun Map<String, String?>.optionalString(fieldName: String): String? =
        get(fieldName)

    protected fun Map<String, String?>.mandatoryInt(fieldName: String): Int =
        mandatoryString(fieldName).toInt()

    protected fun Map<String, String?>.mandatoryBoolean(fieldName: String): Boolean =
        mandatoryString(fieldName).toBoolean()

    protected fun Map<String, String?>.mandatoryCountryCode(fieldName: String): String {
        val fieldValue = mandatoryString(fieldName)
        if (!fieldValue.isCountryCode()) {
            error("${contextInfo} with ${fieldName}='${fieldValue}' not a valid country code")
        }
        return fieldValue
    }

    protected fun Map<String, String?>.mandatoryVatChargeCode(fieldName: String): VatChargeCode {
        val fieldValue = mandatoryString(fieldName)
        try {
            return VatChargeCode.ofCode(fieldValue)
        } catch (exc: Exception) {
            error("${contextInfo} with ${fieldName}='${fieldValue}' not a valid VAT charge code")
        }
    }
}
