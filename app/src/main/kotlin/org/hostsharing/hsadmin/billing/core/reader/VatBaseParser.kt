package org.hostsharing.hsadmin.billing.core.reader

import org.hostsharing.hsadmin.billing.core.domain.VatBase
import org.hostsharing.hsadmin.billing.core.lib.withDomainContext

class VatBaseParser internal constructor(record: Map<String, String?>) :
    VatBase, Parser("VatBase") {

    companion object {
        fun parse(record: Map<String, String?>): VatBase =
            withDomainContext("parsing VatBase data $record") { VatBaseParser(record) }
    }

    override val vatCountryCode = record.mandatoryCountryCode("vatCountryCode")
    override val vatChargeMode = record.mandatoryVatChargeMode("vatChargeMode")
    override val uidVat = record.mandatoryString("uidVat")

    init {
        if (uidVat.isNotBlank() && !uidVat.startsWith(vatCountryCode))
            error("UID-VAT $uidVat does not match vatCountryCode $vatCountryCode")
    }
}
