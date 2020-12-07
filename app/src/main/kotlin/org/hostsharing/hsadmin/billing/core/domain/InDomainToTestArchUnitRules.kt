package org.hostsharing.hsadmin.billing.core.domain

import org.hostsharing.hsadmin.billing.core.lib.InLibToTestArchUnitRules
import org.hostsharing.hsadmin.billing.core.writer.InWriterToTestArchUnitRules

interface InDomainToTestArchUnitRules {
    val badDependency: InWriterToTestArchUnitRules
    val goodDependency: InLibToTestArchUnitRules
}
