package org.hostsharing.hsadmin.billing.core.reader

import org.hostsharing.hsadmin.billing.core.domain.VatGroupDef
import org.hostsharing.hsadmin.billing.core.lib.withDomainContext

class VatGroupDefParser internal constructor(val record: Map<String, String?>) :
    Parser("VAT group definition") {

    companion object {
        fun parse(record: Map<String, String?>): VatGroupDef =
            VatGroupDefParser(record).parse()
    }

    private fun parse(): VatGroupDef =
        withDomainContext("parsing VAT group definition $record") {
            VatGroupDef(
                countryCode = record.mandatoryCountryCode("countryCode"),
                id = record.mandatoryString("id"),
                description = record.mandatoryString("description"),
                placeOfSupply = record.mandatoryPlaceOfSupply("placeOfSupply"),
                vatRate = record.mandatorVatRate("vatRate"),
                dcAccount = record.mandatoryString("dcAccount"),
                rcAccount = record.mandatoryString("rcAccount"),
            )
        }
}

