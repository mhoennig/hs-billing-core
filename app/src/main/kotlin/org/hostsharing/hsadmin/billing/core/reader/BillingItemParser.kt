package org.hostsharing.hsadmin.billing.core.reader

import org.hostsharing.hsadmin.billing.core.domain.BillingItem
import org.hostsharing.hsadmin.billing.core.lib.withContext

class BillingItemParser internal constructor(record: Map<String, String?>)
    : BillingItem, Parser("billing item") {

    companion object {
        fun parse(record: Map<String, String?>): BillingItem =
            withContext("parsing billing item ${record}") { BillingItemParser(record) }
    }

    override val customerCode = record.mandatoryString("customerCode")
    override val vatGroupId = record.mandatoryString("vatGroupId")
    override val netAmount = record.madatoryBigDecimal("netAmount")
}
