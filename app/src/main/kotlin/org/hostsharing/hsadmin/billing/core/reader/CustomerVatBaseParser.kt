package org.hostsharing.hsadmin.billing.core.reader

import org.hostsharing.hsadmin.billing.core.domain.CustomerVatBase
import org.hostsharing.hsadmin.billing.core.lib.withDomainContext

class CustomerVatBaseParser internal constructor(record: Map<String, String?>) :
    CustomerVatBase, Parser("VatBase") {

    companion object {
        fun parse(record: Map<String, String?>): CustomerVatBase =
            withDomainContext("parsing VatBase data $record") { CustomerVatBaseParser(record) }
    }

    override val vatCountryCode = record.mandatoryCountryCode("vatCountryCode")
    override val vatChargeMode = record.mandatoryVatChargeMode("vatChargeMode")
    override val uidVat = record.mandatoryString("uidVat")

    init {
        if (uidVat.isNotBlank() && !uidVat.startsWith(vatCountryCode))
            error("UID-VAT $uidVat does not match vatCountryCode $vatCountryCode")
    }
}
