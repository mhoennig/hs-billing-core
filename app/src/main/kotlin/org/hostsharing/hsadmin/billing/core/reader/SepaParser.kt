package org.hostsharing.hsadmin.billing.core.reader

import org.hostsharing.hsadmin.billing.core.domain.Sepa
import org.hostsharing.hsadmin.billing.core.lib.withContext

class SepaParser internal constructor(record: Map<String, String?>)
    : Sepa, Parser("SEPA data") {

    companion object {
        fun parse(record: Map<String, String?>): Sepa =
            withContext("parsing SEPA data ${record}") { SepaParser(record) }
    }

    override val directDebiting = record.mandatoryBoolean("directDebiting")
    override val bankCustomer = record.optionalString("bankCustomer")
    override val bankIBAN = record.optionalString("bankIBAN")
    override val bankBIC = record.optionalString("bankBIC")
    override val mandatRef = record.optionalString("mandatRef")
}
