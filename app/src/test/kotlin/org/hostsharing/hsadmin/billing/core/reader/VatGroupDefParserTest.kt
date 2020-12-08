package org.hostsharing.hsadmin.billing.core.reader

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.hostsharing.hsadmin.billing.core.lib.ContextException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

internal class VatGroupDefParserTest {

    private val defaultRecordWithAllValidValues: Map<String, String?> = mapOf(
        "id" to "42",
        "description" to "Tästmann GmbH",
        "electronicService" to "DE987654321",
        "DE" to "16,00",
        "AT" to "20,00"
    )

    @Test
    fun `will create VatGroupDef from record with all related fields set and valid`() {
        val givenRecord = defaultRecordWithAllValidValues

        val actual = VatGroupDefParser.parse(givenRecord)

        assertThat(actual.formatted()).isEqualTo(
            """
            id="42"
            description="Tästmann GmbH"
            electronicService="false"
            rates={DE=16,0, AT=20,0}
            """.trimIndent()
        )
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `will set electronicService in VatGroupDef depending on electronicService value in record`(electronicService: Boolean) {
        val givenRecord = defaultRecordWithAllValidValues.toMutableMap().also {
            it.put("electronicService", "$electronicService")
        }
        val actual = VatGroupDefParser.parse(givenRecord)

        assertThat(actual.electronicService).isEqualTo(electronicService)
    }

    @Test
    fun `will throw error when parsing VatGroupDef with invalid percentage`() {
        val givenRecord = defaultRecordWithAllValidValues.toMutableMap().also {
            it.put("DE", "broken")
        }

        val actual = assertThrows<ContextException> {
            VatGroupDefParser.parse(givenRecord)
        }

        assertThat(actual.message).isEqualTo(
            """
            Unparseable number: "broken"
            - in VAT rate definition 'DE'
            - in parsing VAT group definition $givenRecord
            """.trimIndent()
        )
    }

    @ParameterizedTest
    @ValueSource(strings = ["id", "description", "electronicService"])
    fun `will throw error when parsing VatGroupDef from record without mandatory field`(fieldName: String) {
        val givenRecord = defaultRecordWithAllValidValues.toMutableMap().also {
            it.put(fieldName, null)
        }

        val actualException = assertThrows<ContextException> {
            VatGroupDefParser.parse(givenRecord)
        }

        assertThat(actualException.message).isEqualTo(
            """
            VAT group definition without $fieldName
            - in parsing VAT group definition $givenRecord
            """.trimIndent()
        )
    }
}
