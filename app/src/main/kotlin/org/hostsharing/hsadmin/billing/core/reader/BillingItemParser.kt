package org.hostsharing.hsadmin.billing.core.reader

import org.hostsharing.hsadmin.billing.core.domain.BillingItem
import org.hostsharing.hsadmin.billing.core.lib.withDomainContext

class BillingItemParser internal constructor(val record: Map<String, String?>) :
    Parser("billing item") {

    companion object {
        fun parse(record: Map<String, String?>): BillingItem =
            BillingItemParser(record).parse()
    }

    private fun parse(): BillingItem =
        withDomainContext("parsing billing item $record") {
            BillingItem(
                customerCode = record.mandatoryString("customerCode"),
                vatGroupId = record.mandatoryString("vatGroupId"),
                netAmount = record.mandatoryBigDecimal("netAmount")
            )
        }
}
