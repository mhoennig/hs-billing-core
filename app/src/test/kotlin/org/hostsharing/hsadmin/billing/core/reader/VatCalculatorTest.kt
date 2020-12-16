package org.hostsharing.hsadmin.billing.core.reader

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isEqualToIgnoringGivenProperties
import org.hostsharing.hsadmin.billing.core.domain.*
import org.hostsharing.hsadmin.billing.core.lib.Configuration
import org.hostsharing.hsadmin.billing.core.lib.DomainException
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class VatCalculatorTest {

    val config = object : Configuration {}
    val calculator = VatCalculator(config)

    val ID00_MEMBERSHIP = VatGroup("00", "Membership Fee", PlaceOfSupply.NOT_APPLICABLE)
    val ID10_HOSTING = VatGroup("10", "Hosting-Plan", PlaceOfSupply.RECEIVER)
    val ID20_WEBMASTER = VatGroup("20", "Webmaster on Demand", PlaceOfSupply.SUPPLIER)
    val ID30_BOOK = VatGroup("30", "Book", PlaceOfSupply.SUPPLIER)
    val ID40_TSHIRT = VatGroup("40", "T-Shirt", PlaceOfSupply.SUPPLIER)

    // just to have an overview of all VAT group definitions,
    // each test lists their own definitions which are checked against these global definitions
    /* ktlint-disable */// @formatter:off
    val vatCountryGroupDefsGlobals = mapOf(
        "DE" to mapOf(
            vatGroupDefAssignment(ID00_MEMBERSHIP,  VatRate.NO_TAX,         dcAccount = "420000", rcAccount = "n/a"),
            vatGroupDefAssignment(ID10_HOSTING,     VatRate("16,00"),  dcAccount = "440010", rcAccount = "n/a"),
            vatGroupDefAssignment(ID20_WEBMASTER,   VatRate("16,00"),  dcAccount = "440020", rcAccount = "n/a"),
            vatGroupDefAssignment(ID30_BOOK,        VatRate( "5,00"),  dcAccount = "430030", rcAccount = "n/a"),
            vatGroupDefAssignment(ID40_TSHIRT,      VatRate("16,00"),  dcAccount = "440040", rcAccount = "n/a"),
        ),
        "AT" to mapOf(
            vatGroupDefAssignment(ID00_MEMBERSHIP,  VatRate.NO_TAX,         dcAccount = "420000", rcAccount = "420000"),
            vatGroupDefAssignment(ID10_HOSTING,     VatRate("21,00"),  dcAccount = "433110", rcAccount = "433610"),
            vatGroupDefAssignment(ID20_WEBMASTER,   VatRate("21,00"),  dcAccount = "433120", rcAccount = "433620"),
            vatGroupDefAssignment(ID30_BOOK,        VatRate( "9,00"),  dcAccount = "433130", rcAccount = "433630"),
            vatGroupDefAssignment(ID40_TSHIRT,      VatRate("16,00"),  dcAccount = "430040", rcAccount = "433640"),
        ),
        "CH" to mapOf(
            vatGroupDefAssignment(ID00_MEMBERSHIP,  VatRate.NO_TAX,         dcAccount = "420000", rcAccount = "n/a"),
            vatGroupDefAssignment(ID10_HOSTING,     VatRate.NOT_IMPLEMENTED,dcAccount = "n/i",    rcAccount = "433810"),
            vatGroupDefAssignment(ID20_WEBMASTER,   VatRate.NOT_IMPLEMENTED,dcAccount = "n/i",    rcAccount = "433820"),
            vatGroupDefAssignment(ID30_BOOK,        VatRate.NOT_IMPLEMENTED,dcAccount = "n/i",    rcAccount = "433830"),
            vatGroupDefAssignment(ID40_TSHIRT,      VatRate.NOT_IMPLEMENTED,dcAccount = "n/i",    rcAccount = "433840"),
        )
    )
    /* ktlint-enable */ // @formatter:on

    @Nested
    inner class `VAT charge-mode 'domestic'` : VatCalculatorTestImplementation() {

        @Test
        fun `customer with VAT-country-code 'DE' for non-taxable item`() =
            vatCalculatorWillCalculateResult(
                Given(
                    vatGroupDef("DE", ID00_MEMBERSHIP, "noTax", dcAccount = "420000", rcAccount = "n/a"),
                    ID00_MEMBERSHIP, vatBase("DE", VatChargeMode.DOMESTIC)
                ),
                Expected.Result(
                    VatRate.NO_TAX, "420000"
                )
            )

        @Test
        fun `customer with VAT-country-code 'DE' for electronic service item with full tax rate`() =
            vatCalculatorWillCalculateResult(
                Given(
                    vatGroupDef("DE", ID10_HOSTING, "16,00", dcAccount = "440010", rcAccount = "n/a"),
                    ID10_HOSTING, vatBase("DE", VatChargeMode.DOMESTIC)
                ),
                Expected.Result(
                    VatRate("16,00"), "440010"
                )
            )

        @Test
        fun `customer with VAT-country-code 'DE' for physical item with reduced tax rate`() =
            vatCalculatorWillCalculateResult(
                Given(
                    vatGroupDef("DE", ID30_BOOK, "5,00", dcAccount = "430030", rcAccount = "n/a"),
                    ID30_BOOK, vatBase("DE", VatChargeMode.DOMESTIC)
                ),
                Expected.Result(
                    VatRate("5,00"), "430030"
                )
            )

        @Test
        fun `customer with VAT-country-code 'DE' for physical item with full tax rate`() =
            vatCalculatorWillCalculateResult(
                Given(
                    vatGroupDef("DE", ID40_TSHIRT, "16,00", dcAccount = "440040", rcAccount = "n/a"),
                    ID40_TSHIRT, vatBase("DE", VatChargeMode.DOMESTIC)
                ),
                Expected.Result(
                    VatRate("16,00"), "440040"
                )
            )

        // TODO: apply this formulation for all cases
        fun `customer with invalid VAT-country-code 'AT' and arbitrary item`() =
            vatCalculatorWillThrowDomainException(
                Given(
                    vatGroupDef("AT", ID10_HOSTING, "21,00", dcAccount = "433110", rcAccount = "433610"),
                    ID10_HOSTING, vatBase("AT", VatChargeMode.DOMESTIC)
                ),
                Expected.DomainException(
                    """
                    vatCountryCode 'AT' is invalid for vatChargeMode DOMESTIC
                    - in calculating VAT by vatGroupId='10', vatCountryCode='AT', vatChargeMode='DOMESTIC'
                    """.trimIndent()
                )
            )
    }

    @Nested
    inner class `VAT charge-mode 'EU-direct'` : VatCalculatorTestImplementation() {

        @Test
        fun `customer with VAT-country-code 'AT' for non-taxable item`() =
            vatCalculatorWillCalculateResult(
                Given(
                    vatCountryGroupDefsGlobals,
                    ID00_MEMBERSHIP, vatBase("AT", VatChargeMode.EU_DIRECT)
                ),
                Expected.Result(
                    VatRate.NO_TAX, "420000"
                )
            )

        @Test
        fun `customer with VAT-country-code 'AT' for electronic service item with full tax rate`() =
            vatCalculatorWillCalculateResult(
                Given(
                    vatCountryGroupDefsGlobals,
                    ID10_HOSTING, vatBase("AT", VatChargeMode.EU_DIRECT)
                ),
                Expected.Result(
                    VatRate("21,00"), "433110"
                )
            )

        @Test
        fun `customer with VAT-country-code 'AT' for manual service item with full tax rate`() =
            vatCalculatorWillCalculateResult(
                Given(
                    vatGroupDef("AT", ID20_WEBMASTER, "21,00", dcAccount = "433120", rcAccount = "433620"),
                    ID20_WEBMASTER, vatBase("AT", VatChargeMode.EU_DIRECT)
                ),
                Expected.Result(
                    VatRate("21,00"), "433120"
                )
            )

        @Test
        fun `customer with VAT-country-code 'AT' for physical item with reduced tax rate`() =
            vatCalculatorWillCalculateResult(
                Given(
                    vatGroupDef("AT", ID30_BOOK, "9,00", dcAccount = "433130", rcAccount = "433630"),
                    ID30_BOOK, vatBase("AT", VatChargeMode.EU_DIRECT)
                ),
                Expected.Result(
                    VatRate("9,00"), "433130"
                )
            )

        @Test
        fun `customer with invalid VAT-country-code 'DE' for an arbitrary item`() =
            vatCalculatorWillThrowDomainException(
                Given(
                    vatGroupDef("DE", ID10_HOSTING, "16,00", dcAccount = "440010", rcAccount = "n/a"),
                    ID10_HOSTING, vatBase("DE", VatChargeMode.EU_DIRECT)
                ),
                Expected.DomainException(
                    """
                vatCountryCode 'DE' is invalid for vatChargeMode EU_DIRECT
                - in calculating VAT by vatGroupId='10', vatCountryCode='DE', vatChargeMode='EU_DIRECT'
                    """.trimIndent()
                )
            )
    }

    @Nested
    inner class `VAT charge-mode 'EU-reverse'` : VatCalculatorTestImplementation() {

        @Test
        fun `customer in AT with Vat-charge-mode 'EU-reverse' and non-taxable item`() =
            vatCalculatorWillCalculateResult(
                Given(
                    vatGroupDef("AT", ID00_MEMBERSHIP, "noTax", dcAccount = "420000", rcAccount = "420000"),
                    ID00_MEMBERSHIP, vatBase("AT", VatChargeMode.EU_REVERSE)
                ),
                Expected.Result(
                    VatRate.NO_TAX, "420000"
                )
            )

        @Test
        fun `customer with VAT-country-code 'AT' and electronic service item with full tax rate`() =
            vatCalculatorWillCalculateResult(
                Given(
                    vatGroupDef("AT", ID10_HOSTING, "21,00", dcAccount = "433110", rcAccount = "433610"),
                    ID10_HOSTING, vatBase("AT", VatChargeMode.EU_REVERSE)
                ),
                Expected.Result(
                    VatRate.NO_TAX, "433610"
                )
            )

        @Test
        fun `customer with VAT-country-code 'AT' and item with reduced tax rate`() =
            vatCalculatorWillCalculateResult(
                Given(
                    vatCountryGroupDefsGlobals,
                    ID20_WEBMASTER, vatBase("AT", VatChargeMode.EU_REVERSE)
                ),
                Expected.Result(
                    VatRate.NO_TAX, "433620"
                )
            )

        @Test
        fun `customer with invalid VAT-country-code 'DE' and item with arbitrary tax rate`() =
            vatCalculatorWillThrowDomainException(
                Given(
                    vatGroupDef("DE", ID10_HOSTING, "16,00", dcAccount = "440010", rcAccount = "n/a"),
                    ID10_HOSTING, vatBase("DE", VatChargeMode.EU_REVERSE)
                ),
                Expected.DomainException(
                    """
                    vatCountryCode 'DE' is invalid for vatChargeMode EU_REVERSE
                    - in calculating VAT by vatGroupId='10', vatCountryCode='DE', vatChargeMode='EU_REVERSE'
                    """.trimIndent()
                )
            )
    }

    @Nested
    inner class `VAT charge-mode 'Non-EU-reverse'` : VatCalculatorTestImplementation() {

        @Test
        fun `customer with VAT-country-code 'CH' for non-taxable item`() =
            vatCalculatorWillCalculateResult(
                Given(
                    vatGroupDef("CH", ID00_MEMBERSHIP, "noTax", dcAccount = "420000", rcAccount = "n/a"),
                    ID00_MEMBERSHIP, vatBase("CH", VatChargeMode.NON_EU_REVERSE)
                ),
                Expected.Result(
                    VatRate.NO_TAX, "420000"
                )
            )

        @Test
        fun `customer with VAT-country-code 'CH' for electronic service item`() =
            vatCalculatorWillCalculateResult(
                Given(
                    vatGroupDef("CH", ID10_HOSTING, "n/i", dcAccount = "n/i", rcAccount = "433810"),
                    ID10_HOSTING, vatBase("CH", VatChargeMode.NON_EU_REVERSE)
                ),
                Expected.Result(
                    VatRate.NO_TAX, "433810"
                )
            )

        @Test
        fun `customer with VAT-country-code 'CH' for manual service item`() =
            vatCalculatorWillCalculateResult(
                Given(
                    vatGroupDef("CH", ID20_WEBMASTER, "n/i", dcAccount = "n/i", rcAccount = "433820"),
                    ID20_WEBMASTER, vatBase("CH", VatChargeMode.NON_EU_REVERSE)
                ),
                Expected.Result(
                    VatRate.NO_TAX, "433820"
                )
            )

        @Test
        fun `customer with invalid VAT-country-code 'DE' item with arbitrary tax rate`() =
            vatCalculatorWillThrowDomainException(
                Given(
                    vatGroupDef("DE", ID10_HOSTING, "16,00", dcAccount = "440010", rcAccount = "n/a"),
                    ID10_HOSTING, vatBase("DE", VatChargeMode.NON_EU_REVERSE, "DE0123456789")
                ),
                Expected.DomainException(
                    """
                    vatCountryCode 'DE' is invalid for vatChargeMode NON_EU_REVERSE
                    - in calculating VAT by vatGroupId='10', vatCountryCode='DE', vatChargeMode='NON_EU_REVERSE'
                    """.trimIndent()
                )
            )
    }

    // this method is used to move the test data into the test for readability
    private fun vatGroupDef(
        countryCode: CountryCode,
        vatGroup: VatGroup,
        vatRate: String,
        dcAccount: String,
        rcAccount: String
    ): Map<CountryCode, Map<VatGroupId, VatGroupDef>> {
        // make sure the given VAT group def is part of the global vat group definitions
        val defaultVatGroupDef = vatCountryGroupDefsGlobals[countryCode]!!.get(vatGroup.id)!!
        val givenVatGroupDefAssignment = vatGroupDefAssignment(vatGroup, VatRate(vatRate), dcAccount, rcAccount)
        assertThat(
            givenVatGroupDefAssignment.second,
            "[PRECONDITION] given definition from the test does not match corresponding global definition"
        )
            .isEqualToIgnoringGivenProperties(defaultVatGroupDef)

        // use just the definition necessary for the particular test
        return mapOf(
            countryCode to mapOf(givenVatGroupDefAssignment)
        )
    }

    private fun vatBase(vatCountryCode: String, vatChargeMode: VatChargeMode, uidVat: String? = null): VatBase =
        object : VatBase {
            override val vatCountryCode = vatCountryCode
            override val vatChargeMode = vatChargeMode
            override val uidVat = uidVat
        }

    open inner class VatCalculatorTestImplementation {

        protected fun vatCalculatorWillCalculateResult(given: Given, expected: Expected.Result) {
            // when
            val result = calculator.calculateEffectiveRate(
                given.vatCountryGroupDefs,
                given.vatGroup.id,
                given.vatBase
            )

            // then
            assertThat(result.vatRate).isEqualTo(expected.vatRate)
            assertThat(result.vatAccount).isEqualTo(expected.vatAccount)
        }

        protected fun vatCalculatorWillThrowDomainException(given: Given, expected: Expected.DomainException) {

            // when
            val actualException = assertThrows<DomainException> {
                calculator.calculateEffectiveRate(
                    given.vatCountryGroupDefs,
                    given.vatGroup.id,
                    given.vatBase
                )
            }

            // then
            assertThat(actualException.message).isEqualTo(expected.message)
        }
    }

    class Given(
        val vatCountryGroupDefs: Map<CountryCode, Map<VatGroupId, VatGroupDef>>,
        val vatGroup: VatGroup,
        val vatBase: VatBase
    )

    sealed class Expected {

        class Result(
            val vatRate: VatRate,
            val vatAccount: String
        ) : Expected()

        class DomainException(
            val message: String
        ) : Expected()
    }
}

data class VatGroup(val id: VatGroupId, val description: String, val placeOfSupply: PlaceOfSupply)

private fun vatGroupDefAssignment(
    vatGroup: VatGroup,
    vatRate: VatRate,
    dcAccount: String,
    rcAccount: String
): Pair<String, VatGroupDef> =
    vatGroup.id to object : VatGroupDef {
        override val countryCode = "n/a" // not needed in the test
        override val id = vatGroup.id
        override val description = vatGroup.description
        override val placeOfSupply = vatGroup.placeOfSupply
        override val vatRate = vatRate
        override val dcAccount = dcAccount
        override val rcAccount = rcAccount
    }
