package org.hostsharing.hsadmin.billing.core.reader

import org.hostsharing.hsadmin.billing.core.domain.VatGroupDef
import org.hostsharing.hsadmin.billing.core.lib.withDomainContext

class VatGroupDefParser internal constructor(record: Map<String, String?>) :
    VatGroupDef, Parser("VAT group definition") {

    companion object {
        fun parse(record: Map<String, String?>): VatGroupDef =
            withDomainContext("parsing VAT group definition $record") { VatGroupDefParser(record) }
    }

    override val id = record.mandatoryString("id")
    override val description = record.mandatoryString("description")
    override val placeOfSupply = record.mandatoryPlaceOfSupply("placeOfSupply")
    override val vatRate = record.mandatorVatRate("vatRate")
    override val dcAccount = record.mandatoryString("dcAccount")
    override val rcAccount = record.mandatoryString("rcAccount")
}
