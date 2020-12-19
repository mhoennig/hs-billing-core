package org.hostsharing.hsadmin.billing.core.reader

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.hostsharing.hsadmin.billing.core.domain.PlaceOfSupply
import org.hostsharing.hsadmin.billing.core.lib.DomainException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource

internal class VatGroupDefParserTest {

    private val defaultRecordWithAllValidValues: Map<String, String?> = mapOf(
        "countryCode" to "DE",
        "id" to "10",
        "description" to "Test-VAT-Group",
        "placeOfSupply" to "receiver",
        "vatRate" to "16,00",
        "dcAccount" to "440000",
        "rcAccount" to "n/a"
    )

    @Test
    fun `will create VatGroupDef from record with all related fields set and valid`() {
        val givenRecord = defaultRecordWithAllValidValues

        val actual = VatGroupDefParser.parse(givenRecord)

        assertThat(actual.formatted()).isEqualTo(
            """
            countryCode="DE"
            id="10"
            description="Test-VAT-Group"
            placeOfSupply=RECEIVER
            vatRate="0.1600"
            dcAccount="440000"
            rcAccount="n/a"
            """.trimIndent()
        )
    }

    @ParameterizedTest
    @EnumSource(PlaceOfSupply::class)
    fun `will set placeOfSupply in VatGroupDef depending on placeOfSupply value in record`(placeOfSupply: PlaceOfSupply) {
        val givenRecord = defaultRecordWithAllValidValues.toMutableMap().apply {
            put("placeOfSupply", placeOfSupply.code)
        }
        val actual = VatGroupDefParser.parse(givenRecord)

        assertThat(actual.placeOfSupply).isEqualTo(placeOfSupply)
    }

    @Test
    fun `will throw error when parsing VatGroupDef with invalid percentage`() {
        val givenRecord = defaultRecordWithAllValidValues.toMutableMap().apply {
            put("vatRate", "broken")
        }

        val actual = assertThrows<DomainException> {
            VatGroupDefParser.parse(givenRecord)
        }

        assertThat(actual.message).isEqualTo(
            """
            VAT group definition: vatRate='broken' not a valid VatRate
            - in parsing VAT group definition $givenRecord
            """.trimIndent()
        )
    }

    @ParameterizedTest
    @ValueSource(strings = ["id", "description", "placeOfSupply", "vatRate", "dcAccount", "rcAccount"])
    fun `will throw error when parsing VatGroupDef from record without mandatory field`(fieldName: String) {
        val givenRecord = defaultRecordWithAllValidValues.toMutableMap().apply {
            put(fieldName, null)
        }

        val actualException = assertThrows<DomainException> {
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
