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

    val id00Membership = VatGroup("00", "Membership Fee", PlaceOfSupply.NOT_APPLICABLE)
    val id10Hosting = VatGroup("10", "Hosting-Plan", PlaceOfSupply.RECEIVER)
    val id20Webmaster = VatGroup("20", "Webmaster on Demand", PlaceOfSupply.SUPPLIER)
    val id30Book = VatGroup("30", "Book", PlaceOfSupply.SUPPLIER)
    val id40TShirt = VatGroup("40", "T-Shirt", PlaceOfSupply.SUPPLIER)
    val id99Undefined = VatGroup( "99", "Undefined", PlaceOfSupply.SUPPLIER)

    // This map of global VatGroupDefs is just to have an overview of all VAT group definitions.
    // Each single test lists their own definitions to have all relevant inputs and outputs in one place.
    // Also, the test fixture checkes each the inputs against these global VatGroupDefs.
    /* ktlint-disable */// @formatter:off
    val vatCountryGroupDefsGlobals = VatGroupDefs(
        config,
        mapOf(
            "DE" to mapOf(
                vatGroupDefAssignment(id00Membership, VatRate.NO_TAX,         dcAccount = "420000", rcAccount = "n/a"),
                vatGroupDefAssignment(id10Hosting,    VatRate("16,00") , dcAccount = "440010", rcAccount = "n/a"),
                vatGroupDefAssignment(id20Webmaster,  VatRate("16,00"),  dcAccount = "440020", rcAccount = "n/a"),
                vatGroupDefAssignment(id30Book,       VatRate( "5,00"),  dcAccount = "430030", rcAccount = "n/a"),
                vatGroupDefAssignment(id40TShirt,     VatRate("16,00"),  dcAccount = "440040", rcAccount = "n/a"),
            ),
            "AT" to mapOf(
                vatGroupDefAssignment(id00Membership, VatRate.NO_TAX,         dcAccount = "420000", rcAccount = "420000"),
                vatGroupDefAssignment(id10Hosting,    VatRate("21,00"),  dcAccount = "433110", rcAccount = "433610"),
                vatGroupDefAssignment(id20Webmaster,  VatRate("21,00"),  dcAccount = "433120", rcAccount = "433620"),
                vatGroupDefAssignment(id30Book,       VatRate( "9,00"),  dcAccount = "433130", rcAccount = "433630"),
                vatGroupDefAssignment(id40TShirt,     VatRate("16,00"),  dcAccount = "433130", rcAccount = "433640"),
            ),
            "CH" to mapOf(
                vatGroupDefAssignment(id00Membership, VatRate.NO_TAX,         dcAccount = "420000", rcAccount = "n/a"),
                vatGroupDefAssignment(id10Hosting, VatRate.NOT_IMPLEMENTED,   dcAccount = "n/i",    rcAccount = "433810"),
                vatGroupDefAssignment(id20Webmaster, VatRate.NOT_IMPLEMENTED, dcAccount = "n/i",    rcAccount = "433820"),
                vatGroupDefAssignment(id30Book, VatRate.NOT_IMPLEMENTED,      dcAccount = "n/i",    rcAccount = "433830"),
                vatGroupDefAssignment(id40TShirt, VatRate.NOT_IMPLEMENTED,    dcAccount = "n/i",    rcAccount = "433840"),
            )
        )
    )
    /* ktlint-enable */ // @formatter:on

    @Nested
    inner class `VAT charge-mode 'domestic'` : VatCalculatorTestImplementation() {

        @Test
        fun `customer with VAT-country-code 'DE' for non-taxable item`() =
            vatCalculatorWillCalculateResult(
                Given(
                    vatGroupDef("DE", id00Membership, "noTax", dcAccount = "420000", rcAccount = "n/a"),
                    id00Membership, customerVatBase("DE", VatChargeMode.DOMESTIC)
                ),
                Expected.Result(
                    VatRate.NO_TAX, "420000"
                )
            )

        @Test
        fun `customer with VAT-country-code 'DE' for electronic service item with full tax rate`() =
            vatCalculatorWillCalculateResult(
                Given(
                    vatGroupDef("DE", id10Hosting, "16,00", dcAccount = "440010", rcAccount = "n/a"),
                    id10Hosting, customerVatBase("DE", VatChargeMode.DOMESTIC)
                ),
                Expected.Result(
                    VatRate("16,00"), "440010"
                )
            )

        @Test
        fun `customer with VAT-country-code 'DE' for manual service item with full tax rate`() =
            vatCalculatorWillCalculateResult(
                Given(
                        vatGroupDef("DE", id20Webmaster, "16,00", dcAccount = "440020", rcAccount = "n/a"),
                    id20Webmaster, customerVatBase("DE", VatChargeMode.DOMESTIC)
                ),
                Expected.Result(
                    VatRate("21,00"), "440020"
                )
            )

        @Test
        fun `customer with VAT-country-code 'DE' for physical item with reduced tax rate`() =
            vatCalculatorWillCalculateResult(
                Given(
                    vatGroupDef("DE", id30Book, "5,00", dcAccount = "430030", rcAccount = "n/a"),
                    id30Book, customerVatBase("DE", VatChargeMode.DOMESTIC)
                ),
                Expected.Result(
                    VatRate("5,00"), "430030"
                )
            )

        @Test
        fun `customer with VAT-country-code 'DE' for physical item with full tax rate`() =
            vatCalculatorWillCalculateResult(
                Given(
                    vatGroupDef("DE", id40TShirt, "16,00", dcAccount = "440040", rcAccount = "n/a"),
                    id40TShirt, customerVatBase("DE", VatChargeMode.DOMESTIC)
                ),
                Expected.Result(
                    VatRate("16,00"), "440040"
                )
            )

        fun `customer with invalid VAT-country-code 'AT' and arbitrary item`() =
            vatCalculatorWillThrowDomainException(
                Given(
                    vatGroupDef("AT", id10Hosting, "21,00", dcAccount = "433110", rcAccount = "433610"),
                    id10Hosting, customerVatBase("AT", VatChargeMode.DOMESTIC)
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
                    id00Membership, customerVatBase("AT", VatChargeMode.EU_DIRECT)
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
                    id10Hosting, customerVatBase("AT", VatChargeMode.EU_DIRECT)
                ),
                Expected.Result(
                    VatRate("21,00"), "433110"
                )
            )

        @Test
        fun `customer with VAT-country-code 'AT' for manual service item with full tax rate`() =
            vatCalculatorWillCalculateResult(
                Given(
                    vatGroupDef("AT", id20Webmaster, "21,00", dcAccount = "433120", rcAccount = "433620"),
                    id20Webmaster, customerVatBase("AT", VatChargeMode.EU_DIRECT)
                ),
                Expected.Result(
                    VatRate("21,00"), "433120"
                )
            )

        @Test
        fun `customer with VAT-country-code 'AT' for physical item with reduced tax rate`() =
            vatCalculatorWillCalculateResult(
                Given(
                    vatGroupDef("AT", id30Book, "9,00", dcAccount = "433130", rcAccount = "433630"),
                    id30Book, customerVatBase("AT", VatChargeMode.EU_DIRECT)
                ),
                Expected.Result(
                    VatRate("9,00"), "433130"
                )
            )

        @Test
        fun `customer with VAT-country-code 'AT' for physical item with full tax rate`() =
            vatCalculatorWillCalculateResult(
                Given(
                    vatGroupDef("AT", id40TShirt, "16,00", dcAccount = "433130", rcAccount = "433640"),
                    id40TShirt, customerVatBase("AT", VatChargeMode.EU_DIRECT)
                ),
                Expected.Result(
                    VatRate("16,00"), "433130"
                )
            )

        @Test
        fun `customer with invalid VAT-country-code 'DE' for an arbitrary item`() =
            vatCalculatorWillThrowDomainException(
                Given(
                    vatGroupDef("DE", id10Hosting, "16,00", dcAccount = "440010", rcAccount = "n/a"),
                    id10Hosting, customerVatBase("DE", VatChargeMode.EU_DIRECT)
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
                    vatGroupDef("AT", id00Membership, "noTax", dcAccount = "420000", rcAccount = "420000"),
                    id00Membership, customerVatBase("AT", VatChargeMode.EU_REVERSE)
                ),
                Expected.Result(
                    VatRate.NO_TAX, "420000"
                )
            )

        @Test
        fun `customer with VAT-country-code 'AT' and electronic service item with full tax rate`() =
            vatCalculatorWillCalculateResult(
                Given(
                    vatGroupDef("AT", id10Hosting, "21,00", dcAccount = "433110", rcAccount = "433610"),
                    id10Hosting, customerVatBase("AT", VatChargeMode.EU_REVERSE)
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
                    id20Webmaster, customerVatBase("AT", VatChargeMode.EU_REVERSE)
                ),
                Expected.Result(
                    VatRate.NO_TAX, "433620"
                )
            )

        @Test
        fun `customer with invalid VAT-country-code 'DE' and item with arbitrary tax rate`() =
            vatCalculatorWillThrowDomainException(
                Given(
                    vatGroupDef("DE", id10Hosting, "16,00", dcAccount = "440010", rcAccount = "n/a"),
                    id10Hosting, customerVatBase("DE", VatChargeMode.EU_REVERSE)
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
                    vatGroupDef("CH", id00Membership, "noTax", dcAccount = "420000", rcAccount = "n/a"),
                    id00Membership, customerVatBase("CH", VatChargeMode.NON_EU_REVERSE)
                ),
                Expected.Result(
                    VatRate.NO_TAX, "420000"
                )
            )

        @Test
        fun `customer with VAT-country-code 'CH' for electronic service item`() =
            vatCalculatorWillCalculateResult(
                Given(
                    vatGroupDef("CH", id10Hosting, "n/i", dcAccount = "n/i", rcAccount = "433810"),
                    id10Hosting, customerVatBase("CH", VatChargeMode.NON_EU_REVERSE)
                ),
                Expected.Result(
                    VatRate.NO_TAX, "433810"
                )
            )

        @Test
        fun `customer with VAT-country-code 'CH' for manual service item`() =
            vatCalculatorWillCalculateResult(
                Given(
                    vatGroupDef("CH", id20Webmaster, "n/i", dcAccount = "n/i", rcAccount = "433820"),
                    id20Webmaster, customerVatBase("CH", VatChargeMode.NON_EU_REVERSE)
                ),
                Expected.Result(
                    VatRate.NO_TAX, "433820"
                )
            )

        @Test
        fun `customer with invalid VAT-country-code 'DE' item with arbitrary tax rate`() =
            vatCalculatorWillThrowDomainException(
                Given(
                    vatGroupDef("DE", id10Hosting, "16,00", dcAccount = "440010", rcAccount = "n/a"),
                    id10Hosting, customerVatBase("DE", VatChargeMode.NON_EU_REVERSE, "DE0123456789")
                ),
                Expected.DomainException(
                    """
                    vatCountryCode 'DE' is invalid for vatChargeMode NON_EU_REVERSE
                    - in calculating VAT by vatGroupId='10', vatCountryCode='DE', vatChargeMode='NON_EU_REVERSE'
                    """.trimIndent()
                )
            )
    }


    @Nested
    inner class `will throw exception with error message` : VatCalculatorTestImplementation() {

        @Test
        fun `if  customerVatBase-vatCountryCode is not defined`() =
            vatCalculatorWillThrowDomainException(
                Given(
                    vatGroupDef("DE", id10Hosting, "16,00", dcAccount = "440010", rcAccount = "n/a"),
                    id10Hosting, customerVatBase("XX", VatChargeMode.EU_REVERSE, "XX0123456789")
                ),
                Expected.DomainException(
                    """
                    vatCountryCode 'XX' not found in [DE]
                    - in calculating VAT by vatGroupId='10', vatCountryCode='XX', vatChargeMode='EU_REVERSE'
                    """.trimIndent()
                )
            )

        @Test
        fun `if vatGroupId is not defined`() =
            vatCalculatorWillThrowDomainException(
                Given(
                    vatGroupDef("DE", id10Hosting, "16,00", dcAccount = "440010", rcAccount = "n/a"),
                    id99Undefined, customerVatBase("DE", VatChargeMode.DOMESTIC, "DE0123456789")
                ),
                Expected.DomainException(
                    """
                    vatGroupId '99' not found in [10]
                    - in calculating VAT by vatGroupId='99', vatCountryCode='DE', vatChargeMode='DOMESTIC'
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
    ): VatGroupDefs {
        // make sure the given VAT group def is part of the global vat group definitions
        val defaultVatGroupDef = vatCountryGroupDefsGlobals[countryCode]!!.get(vatGroup.id)!!
        val givenVatGroupDefAssignment = vatGroupDefAssignment(vatGroup, VatRate(vatRate), dcAccount, rcAccount)
        assertThat(
            givenVatGroupDefAssignment.second,
            "[PRECONDITION] given definition from the test does not match corresponding global definition"
        ).isEqualToIgnoringGivenProperties(defaultVatGroupDef)

        // use just the definition necessary for the particular test
        return VatGroupDefs(
            config,
            mapOf(
                countryCode to mapOf(givenVatGroupDefAssignment)
            )
        )
    }

    private fun customerVatBase(vatCountryCode: String, vatChargeMode: VatChargeMode, uidVat: String? = null): CustomerVatBase =
        object : CustomerVatBase {
            override val vatCountryCode = vatCountryCode
            override val vatChargeMode = vatChargeMode
            override val uidVat = uidVat
        }

    open inner class VatCalculatorTestImplementation {

        protected fun vatCalculatorWillCalculateResult(given: Given, expected: Expected.Result) {
            // when
            val result = calculator.determineEffectiveRate(
                given.vatCountryGroupDefs,
                given.vatGroup.id,
                given.customerVatBase
            )

            // then
            assertThat(result.vatRate).isEqualTo(expected.vatRate)
            assertThat(result.vatAccount).isEqualTo(expected.vatAccount)
        }

        protected fun vatCalculatorWillThrowDomainException(given: Given, expected: Expected.DomainException) {

            // when
            val actualException = assertThrows<DomainException> {
                calculator.determineEffectiveRate(
                    given.vatCountryGroupDefs,
                    given.vatGroup.id,
                    given.customerVatBase
                )
            }

            // then
            assertThat(actualException.message).isEqualTo(expected.message)
        }
    }

    class Given(
        val vatCountryGroupDefs: VatGroupDefs,
        val vatGroup: VatGroup,
        val customerVatBase: CustomerVatBase
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
    vatGroup.id to VatGroupDef(
        countryCode = "n/a", // not needed in the test
        id = vatGroup.id,
        description = vatGroup.description,
        placeOfSupply = vatGroup.placeOfSupply,
        vatRate = vatRate,
        dcAccount = dcAccount,
        rcAccount = rcAccount
    )
