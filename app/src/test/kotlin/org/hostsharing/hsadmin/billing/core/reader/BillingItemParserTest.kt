package org.hostsharing.hsadmin.billing.core.reader

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.hostsharing.hsadmin.billing.core.lib.DomainException
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

internal class BillingItemParserTest {

    private val recordWithAllFieldValues: Map<String, String?> = mapOf(
        "customerCode" to "xyz",
        "vatGroupId" to "10",
        "netAmount" to "16.50" // TODO: should use "," as decimal separator
    )

    @Test
    fun `will create billing item from record with all related fields set and valid`() {
        val givenRecord = recordWithAllFieldValues

        val actual = BillingItemParser.parse(givenRecord)

        assertThat(actual.formatted()).isEqualTo(
            """
            customerCode="xyz"
            netAmount="16.50"
            vatGroupId="10"
            """.trimIndent()
        )
    }

    @ParameterizedTest
    @ValueSource(strings = ["customerCode", "vatGroupId", "netAmount"])
    fun `will throw error when parsing contact from record without mandatory field`(fieldName: String) {
        val givenRecord = recordWithAllFieldValues.toMutableMap().apply {
            put(fieldName, null)
        }

        val actualException = org.junit.jupiter.api.assertThrows<DomainException> {
            BillingItemParser.parse(givenRecord)
        }

        assertThat(actualException.message).isEqualTo(
            """
            billing item without $fieldName
            - in parsing billing item $givenRecord
            """.trimIndent()
        )
    }
}
