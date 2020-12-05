package org.hostsharing.hsadmin.billing.core.reader

import java.math.BigDecimal

class VatRate(value: String) {
    val noTax: Boolean = value == "noTax"
    val percentage: BigDecimal =
        if (noTax) BigDecimal.ZERO else BigDecimal(value) / BigDecimal(100)
}

interface VatGroupDef {
    val id: String
    val description: String
    val electronicService: Boolean
    val rates: Map<String, VatRate>
}
