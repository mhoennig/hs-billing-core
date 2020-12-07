package org.hostsharing.hsadmin.billing.core.reader

import org.hostsharing.hsadmin.billing.core.domain.VatRate

interface VatGroupDef {
    val id: String
    val description: String
    val electronicService: Boolean
    val rates: Map<String, VatRate>
}


