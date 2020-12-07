package org.hostsharing.hsadmin.billing.core.reader

import org.hostsharing.hsadmin.billing.core.writer.InWriterToTestArchUnitRules

interface InReaderToTestArchUnitRules {
    val badCycle: InWriterToTestArchUnitRules
}
