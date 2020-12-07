package org.hostsharing.hsadmin.billing.core

import org.hostsharing.hsadmin.billing.core.domain.InDomainToTestArchUnitRules
import org.hostsharing.hsadmin.billing.core.lib.InLibToTestArchUnitRules
import org.hostsharing.hsadmin.billing.core.reader.InReaderToTestArchUnitRules
import org.hostsharing.hsadmin.billing.core.writer.InWriterToTestArchUnitRules

interface InRootToTestArchUnitRules {
    val goodDependencyToLib: InLibToTestArchUnitRules
    val goodDependencyToDomain: InDomainToTestArchUnitRules
    val goodDependencyToReader: InReaderToTestArchUnitRules
    val goodDependencyToWriter: InWriterToTestArchUnitRules
}
