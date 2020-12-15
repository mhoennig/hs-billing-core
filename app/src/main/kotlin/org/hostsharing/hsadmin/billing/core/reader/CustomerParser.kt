package org.hostsharing.hsadmin.billing.core.reader

import org.hostsharing.hsadmin.billing.core.domain.Customer
import org.hostsharing.hsadmin.billing.core.lib.withDomainContext

class CustomerParser internal constructor(record: Map<String, String?>) :
    Customer, Parser("customer-row") {

    companion object {
        fun parse(record: Map<String, String?>): Customer =
            withDomainContext("parsing customer $record") { CustomerParser(record) }
    }

    override val number = record.mandatoryInt("customerNumber")
    override val code = record.mandatoryString("customerCode")
    override val billingContact = ContactParser.parse("billing contact", record)
    override val sepa = SepaParser.parse(record)
    override val vatBase = VatBaseParser.parse(record)
}
