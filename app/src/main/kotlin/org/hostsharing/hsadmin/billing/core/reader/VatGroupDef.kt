package org.hostsharing.hsadmin.billing.core.reader

import org.hostsharing.hsadmin.billing.core.domain.Formattable
import org.hostsharing.hsadmin.billing.core.domain.VatRate

interface VatGroupDef: Formattable {
    val id: String
    val description: String
    val electronicService: Boolean
    val rates: Map<String, VatRate>

    override fun format(indent: Int): String = """
        |id=${id.quoted}
        |description=${description.quoted}
        |electronicService=${electronicService.quoted}
        |rates=${rates}
        """
}


