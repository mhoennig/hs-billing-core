package org.hostsharing.hsadmin.billing.core.reader

import org.hostsharing.hsadmin.billing.core.domain.CustomerVatBase
import org.hostsharing.hsadmin.billing.core.domain.VatGroupDef
import org.hostsharing.hsadmin.billing.core.lib.withDomainContext

class CustomerVatBaseParser internal constructor(val record: Map<String, String?>) :
    Parser("CustomerVatBase") {

    companion object {
        fun parse(record: Map<String, String?>): CustomerVatBase =
            CustomerVatBaseParser(record).parse()
    }

    fun parse(): CustomerVatBase =
        withDomainContext("parsing CustomerVatBase data $record") {
            CustomerVatBase(
                vatCountryCode = record.mandatoryCountryCode("vatCountryCode"),
                vatChargeMode = record.mandatoryVatChargeMode("vatChargeMode"),
                uidVat = record.mandatoryString("uidVat")
            )
        }
}
