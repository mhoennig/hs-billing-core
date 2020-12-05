package org.hostsharing.hsadmin.billing.core.reader

import java.math.BigDecimal

class VatRate(value: String) {
    val percentage: BigDecimal = BigDecimal(if (value=="noTax") "1.00" else value)
}

interface VatGroup {

    val id: String
    val description: String
    val electronicService: Boolean
    val rates: Map<String, VatRate>
}
