package org.hostsharing.hsadmin.billing.core.lib

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test

internal class ContextTest {

    @Test
    fun `exception error message should contain context infos`() {
        val actualException = org.junit.jupiter.api.assertThrows<ContextException> {
            withContext("processing outer") {
                withContext("processing inner") {
                    throw RuntimeException("some error message")
                }
            }
        }

        assertThat(actualException.message).isEqualTo("""
            some error message
            - while processing inner
            - while processing outer
            """.trimIndent())
    }
}
