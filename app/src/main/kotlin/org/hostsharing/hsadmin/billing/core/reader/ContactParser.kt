package org.hostsharing.hsadmin.billing.core.reader

import org.hostsharing.hsadmin.billing.core.domain.Contact
import org.hostsharing.hsadmin.billing.core.lib.withDomainContext

class ContactParser internal constructor(contextInfo: String, record: Map<String, String?>) :
    Contact, Parser(contextInfo) {

    companion object {
        fun parse(contextInfo: String, record: Map<String, String?>): Contact =
            withDomainContext("parsing $contextInfo $record") { ContactParser(contextInfo, record) }
    }

    override val company = record.optionalString("company")
    override val salutation = record.mandatoryString("salutation")
    override val title = record.optionalString("title")
    override val firstName = record.mandatoryString("firstName")
    override val lastName = record.mandatoryString("lastName")
    override val co = record.optionalString("co")
    override val street = record.mandatoryString("street")
    override val zipCode = record.mandatoryString("zipCode")
    override val city = record.mandatoryString("city")
    override val country = record.mandatoryString("country")
    override val countryCode = record.mandatoryCountryCode("countryCode")
    override val email = record.mandatoryString("email")
}
