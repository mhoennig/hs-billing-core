package org.hostsharing.hsadmin.billing.core.lib

import org.hostsharing.hsadmin.billing.core.InRootToTestArchUnitRules
import org.hostsharing.hsadmin.billing.core.domain.InDomainToTestArchUnitRules
import org.hostsharing.hsadmin.billing.core.reader.InReaderToTestArchUnitRules
import org.hostsharing.hsadmin.billing.core.writer.InWriterToTestArchUnitRules

interface InLibToTestArchUnitRules {
    val badDependencyToRoot: InRootToTestArchUnitRules
    val goodDependencyToLib: InLibTooToTestArchUnitRules
    val badDependencyToDomain: InDomainToTestArchUnitRules
    val badDependencyToReader: InReaderToTestArchUnitRules
    val badDependencyToWriter: InWriterToTestArchUnitRules
}

interface InLibTooToTestArchUnitRules {
}
