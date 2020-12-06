package org.hostsharing.hsadmin.billing.core.lib

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test

internal class ContextTest {

    @Test
    fun `exception error message will contain context infos`() {
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

    @Test
    fun `exception class will be used for missing message`() {
        val actualException = org.junit.jupiter.api.assertThrows<ContextException> {
            withContext("processing outer") {
                withContext("processing inner") {
                    throw IllegalStateException()
                }
            }
        }

        assertThat(actualException.message).isEqualTo("""
            IllegalStateException
            - while processing inner
            - while processing outer
            """.trimIndent())
    }

    @Test
    fun `body value will be returned`() {
        val actual = withContext("processing outer") {
            withContext("processing inner") {
                "some value"
            }
        }

        assertThat(actual).isEqualTo("some value")
    }
}
