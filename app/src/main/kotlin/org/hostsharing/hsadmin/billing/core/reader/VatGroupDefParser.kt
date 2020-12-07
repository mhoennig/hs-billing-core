package org.hostsharing.hsadmin.billing.core.reader

import org.hostsharing.hsadmin.billing.core.domain.VatRate
import org.hostsharing.hsadmin.billing.core.domain.isCountryCode
import org.hostsharing.hsadmin.billing.core.lib.withContext

class VatGroupDefParser internal constructor(record: Map<String, String?>)
    : VatGroupDef, Parser("VAT group definition") {

    companion object {
        fun parse(record: Map<String, String?>): VatGroupDef =
            withContext("parsing VAT group definition ${record}") { VatGroupDefParser(record) }
    }

    override val id = record.mandatoryString("id")
    override val description = record.mandatoryString("description")
    override val electronicService = record.mandatoryBoolean("electronicService")
    override val rates = record.filter { it.key.isCountryCode() }
        .map {
            withContext("VAT rate definition '${it.key}'") {
                it.key to VatRate(it.value ?: error("mandatory VAT rate missing"))
            }
        }
        .toMap()
}
