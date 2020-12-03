package org.hostsharing.hsadmin.billing.core

import assertk.Assert
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.fail
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.io.File
import java.time.LocalDate

class BillingIntegrationTest {

    @Test
    fun `will generate invoices-csv`() {

        val customersCsvFile = givenInputFile named "customers.csv" containing """
            customerNumber;customerCode;salutation;company;title;firstName;lastName;fullName;co;address;zipcode;city;country;uidVat;directDebiting;bankCustomer;bankIBAN;bankBIC;mandatRef
            "12345";"hsh00-xyz";"Herr";"Testmann GmbH";"";"Tästi";"Testmann";"Tästi Testmann";"";"Teststraße 42";"20144";"Hamburg";"Germany";"DE987654321";"true";"Testmann GmbH";"DE81201900030012345678";"GENODEF1HH2";"HS-10003-20140801";"DE-UIDVAT"
            """

        val articleGroupsCsvFile = givenInputFile named "article-groups.csv" containing """
            group-id;   bezeichnung;            DE-*;       AT-UID
            "M";        "Mitgliedsbeitrag";      "0.00"
            "R";        "Rabatttarif";          "16.00"
            "D";        "Domain-Laufzeit";      "16.00"
            "T";        "Traffic";              "16.00"
            "C";        "CPU";                  "16.00"
            "W";        "WoD";                  "16.00"
            """

        val billingItemsCsvFile = givenInputFile named "billing-items.csv" containing """
            customerCode;   product?;      project; count;   ???;    fromTimestamp;          untilTimestamp;        description;                  netAmount; groupId
            "hsh00-xyz";    "";            ;          "1";    "110"; "2020-11-14";           "2020-11-14";          "Domain-Rabatt";                "10.00";    "R"
            "hsh00-xyz";    "testmann.xy"; "myxyz";   "1";    "210"; "2020-11-01";           "2020-11-14";          "Laufzeit bis 01.10.21";         "4.50";    "D"
            "hsh00-xyz";    "xyz01";       "myxyz";   "1";   "3000"; "2020-11-14";           "2020-11-14";          "250 GB Datentransfervolumen";   "5.00";    "T"
            "hsh00-xyz";    "";            "myxyz";  "12";   "0500"; "2020-11-14";           ;                      "15 Min. WoD-Normal: ...";      "25.00";    "W"
            "hsh00-xyz";    "xyz01";       "myxyz";   "1";   "3100"; "2020-11-14";           "2020-11-14";          "HS Basic Support";             "10.00";    "S"
            "hsh00-xyz";    "xyz01";       "myxyz";   "4";   "3000"; "2020-11-01";           "2020-11-14";          "Prozessor-Thread";             "15.00";    "C"
            "hsh00-xyz";    "bbbmeet";     "myxyz";   "1";   "4711"; "2020-11-01T10:25:00";  "2020-11-14T11:35:00"; "BBB Meet Konferenz";           "15.00";    "C"
            """

        val actualInvoicesCsvFile = givenOutputFile named "invoices.csv"

        val actualInvoicesCsv = Billing(
                periodEndDate = LocalDate.parse("2020-11-30"),
                billingDate = LocalDate.parse("2020-12-02"),
                startInvoiceNumber = 2000,
                articleGroupsCSV = articleGroupsCsvFile,
                customersCSV = customersCsvFile,
                billingItemsCSVs = arrayOf(billingItemsCsvFile)
        ).generateInvoicesCsv(actualInvoicesCsvFile).readText()

        assertThat(actualInvoicesCsv) matches """
            |customerNumber;documentNumber;documentDate;referenceDate;referencePeriod;dueDate;directDebiting;vatRate;vatGross"
            |"12345";"2020-2000-12345";"02.12.2020";"30.11.2020";"11/2020";"20.12.2020";"true";"16";"206,66"
            """
    }

    @Test
    @Disabled
    fun `repeated runs will generate identical invoices-csv`() {
        fail("todo")
    }

    // --- fixture ----------------------------------------------------------

    val givenInputDir = createTempDir(prefix = "hs-billing-test-input")
    val givenInputFile = givenInputDir
    val givenOutputDir = createTempDir(prefix = "hs-billing-test-output")
    val givenOutputFile = givenOutputDir

    private infix fun File.named(name: String): File =
            File(givenInputFile, name)

    private infix fun File.containing(content: String): File {
        this.writeText(content.trimIndent())
        return this
    }

    private infix fun Assert<String>.matches(textBlock: String) = this.isEqualTo(textBlock.replaceIndentByMargin(marginPrefix = "|"))
}
