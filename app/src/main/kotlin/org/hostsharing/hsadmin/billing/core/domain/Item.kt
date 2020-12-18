package org.hostsharing.hsadmin.billing.core.domain

import java.math.BigDecimal

interface Item {
    val customerCode: String
    val netAmount: BigDecimal
    val vatGroupId: String
}
