package org.hostsharing.hsadmin.billing.core.lib

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test

internal class DomainContextTest {

    @Test
    fun `exception error message will contain context infos`() {
        val actualException = org.junit.jupiter.api.assertThrows<DomainException> {
            withDomainContext("processing outer") {
                withDomainContext("processing inner") {
                    throw RuntimeException("some error message")
                }
            }
        }

        assertThat(actualException.message).isEqualTo(
            """
            some error message
            - in processing inner
            - in processing outer
            """.trimIndent()
        )
    }

    @Test
    fun `exception class will be used for missing message`() {
        val actualException = org.junit.jupiter.api.assertThrows<DomainException> {
            withDomainContext("processing outer") {
                withDomainContext("processing inner") {
                    throw IllegalStateException()
                }
            }
        }

        assertThat(actualException.message).isEqualTo(
            """
            IllegalStateException
            - in processing inner
            - in processing outer
            """.trimIndent()
        )
    }

    @Test
    fun `body value will be returned`() {
        val actual = withDomainContext("processing outer") {
            withDomainContext("processing inner") {
                "some value"
            }
        }

        assertThat(actual).isEqualTo("some value")
    }
}
