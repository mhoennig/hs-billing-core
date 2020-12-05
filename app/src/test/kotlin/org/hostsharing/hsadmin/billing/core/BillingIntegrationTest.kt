package org.hostsharing.hsadmin.billing.core

import assertk.Assert
import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test
import java.io.File
import java.time.LocalDate

class BillingIntegrationTest {

    @Test
    fun `will generate bookings-csv`() {

        val customersCsvFile = givenInputFile named "customers.csv" containing """
            |customerNumber;customerCode;salutation;company;title;firstName;lastName;fullName;co;address;zipcode;city;country;countryCode;uidVat;directDebiting;bankCustomer;bankIBAN;bankBIC;mandatRef
            |"12345";"hsh00-xyz";"Herr";"Testmann GmbH";"";"Tästi";"Testmann";"Tästi Testmann";"";"Teststraße 42";"20144";"Hamburg";"Germany";"DE";"DE987654321";"true";"Testmann GmbH";"DE81201900030012345678";"GENODEF1HH2";"HS-10003-20140801";"CH";"RC"
            |"""

        val vatGroupsCsvFile = givenInputFile named "article-groups.csv" containing """
            |id;    description;            electronicService;    DE;         AT;
            |"00";  "Mitgliedsbeitrag";     "true";             "noTax";    "noTax";    
            |"01";  "Rabatttarif";          "true";             "16.00";    "20.00";
            |"02";  "Domain-Laufzeit";      "true";             "16.00";    "20.00";
            |"03";  "Package";              "true";             "16.00";    "20.00";
            |"04";  "Traffic";              "true";             "16.00";    "20.00";
            |"05";  "CPU";                  "true";             "16.00";    "20.00";
            |"06";  "WoD";                  "false";            "16.00";    "20.00";
            |"07";  "SLA";                  "true";             "16.00";    "20.00";
            |"08";  "BBB";                  "true";             "16.00";    "20.00";
            |"""

        val billingItemsCsvFile = givenInputFile named "billing-items.csv" containing """
            |customerCode;   product?;      project; count; vatGroupId; articleId; fromTimestamp;          untilTimestamp;        description;                  netAmount
            |"hsh00-xyz";    "";            ;          "1";       "00";      "0"; "2020-11-14";           "2020-11-14";          "Mitgliedsbeitrag";             "10.00"
            |"hsh00-xyz";    "";            ;          "1";       "01";    "110"; "2020-11-14";           "2020-11-14";          "Domain-Rabatt";                "10.00"
            |"hsh00-xyz";    "testmann.xy"; "myxyz";   "1";       "02";    "210"; "2020-11-01";           "2020-11-14";          "Laufzeit bis 01.10.21";         "4.50"
            |"hsh00-xyz";    "xyz01";       "myxyz";   "1";       "03";   "2000"; "2020-11-14";           "2020-12-13";          "Web-Paket";                    "24.00"
            |"hsh00-xyz";    "xyz01";       "myxyz";   "1";       "04";   "3000"; "2020-11-14";           "2020-11-14";          "250 GB Datentransfervolumen";   "5.00"
            |"hsh00-xyz";    "xyz01";       "myxyz";  "12";       "06";   "0500"; "2020-11-14";           ;                      "15 Min. WoD-Normal: ...";      "25.00"
            |"hsh00-xyz";    "xyz01";       "myxyz";   "1";       "07";   "3100"; "2020-11-14";           "2020-11-14";          "HS Basic Support";             "10.00"
            |"hsh00-xyz";    "xyz01";       "myxyz";   "4";       "05";   "3000"; "2020-11-01";           "2020-11-14";          "Prozessor-Thread";             "15.00"
            |"hsh00-xyz";    "bbbmeet";     "myxyz";   "1";       "08";   "4711"; "2020-11-01T10:25:00";  "2020-11-14T11:35:00"; "BBB Meet Konferenz";           "15.00"
            |"""

        val actualBookingsCsvFile = givenOutputFile named "bookings.csv"

        val actualBookingsCsv = Billing(
            configuration,
            periodEndDate = LocalDate.parse("2020-11-30"),
            billingDate = LocalDate.parse("2020-12-03"),
            startInvoiceNumber = 2000,
            vatGroupsCSV = vatGroupsCsvFile,
            customersCSV = customersCsvFile,
            billingItemsCSVs = arrayOf(billingItemsCsvFile)
        ).generateBookingsCsv(actualBookingsCsvFile).readText()

        assertThat(actualBookingsCsv) matchesInExactOrder """
            |customerNumber;documentNumber;documentDate;referenceDate;referencePeriod;dueDate;directDebiting;vatRate;netAmount;grossAmount;vatAmount;vatAccount
            |"12345";"2020-2000-12345";"03.12.2020";"30.11.2020";"11/2020";"02.01.2021";"true";"0,00";"10,00";"10,00";"0,00";"420000"
            |"12345";"2020-2000-12345";"03.12.2020";"30.11.2020";"11/2020";"02.01.2021";"true";"0,16";"10,00";"11,60";"1,60";"440001"
            |"12345";"2020-2000-12345";"03.12.2020";"30.11.2020";"11/2020";"02.01.2021";"true";"0,16";"4,50";"5,22";"0,72";"440002"
            |"12345";"2020-2000-12345";"03.12.2020";"30.11.2020";"11/2020";"02.01.2021";"true";"0,16";"24,00";"27,84";"3,84";"440003"
            |"12345";"2020-2000-12345";"03.12.2020";"30.11.2020";"11/2020";"02.01.2021";"true";"0,16";"5,00";"5,80";"0,80";"440004"
            |"12345";"2020-2000-12345";"03.12.2020";"30.11.2020";"11/2020";"02.01.2021";"true";"0,16";"25,00";"29,00";"4,00";"440006"
            |"12345";"2020-2000-12345";"03.12.2020";"30.11.2020";"11/2020";"02.01.2021";"true";"0,16";"10,00";"11,60";"1,60";"440007"
            |"12345";"2020-2000-12345";"03.12.2020";"30.11.2020";"11/2020";"02.01.2021";"true";"0,16";"15,00";"17,40";"2,40";"440005"
            |"12345";"2020-2000-12345";"03.12.2020";"30.11.2020";"11/2020";"02.01.2021";"true";"0,16";"15,00";"17,40";"2,40";"440008"
            |"""
    }

    @Test
    fun `will consider multiple billing-item-files`() {

        val customersCsvFile = givenInputFile named "customers.csv" containing """
            |customerNumber;customerCode;salutation;company;title;firstName;lastName;fullName;co;address;zipcode;city;country;countryCode;uidVat;directDebiting;bankCustomer;bankIBAN;bankBIC;mandatRef
            |"12345";"hsh00-xyz";"Herr";"Testmann GmbH";"";"Tästi";"Testmann";"Tästi Testmann";"";"Teststraße 42";"20144";"Hamburg";"Germany";"DE";"DE987654321";"true";"Testmann GmbH";"DE81201900030012345678";"GENODEF1HH2";"HS-10003-20140801";"CH";"RC"
            |"""

        val vatGroupsCsvFile = givenInputFile named "article-groups.csv" containing """
            |id;    description;               electronicService;    DE;         AT;
            |"00";  "Mitgliedsbeitrag";     "true";             "noTax";    "noTax";    
            |"02";  "Domain-Laufzeit";      "true";             "16.00";    "20.00";
            |"03";  "Package";              "true";             "16.00";    "20.00";
            |"""

        val customerBillingItemsCsvFile = givenInputFile named "customer-billing-items.csv" containing """
            |customerCode;   product?;      project; count; vatGroupId; articleId; fromTimestamp;          untilTimestamp;        description;                  netAmount
            |"hsh00-xyz";    "";            ;          "1";       "00";      "0"; "2020-11-14";           "2020-11-14";          "Mitgliedsbeitrag";             "10.00"
            |"""

        val domainItemsCsvFile = givenInputFile named "domain-billing-items.csv" containing """
            |customerCode;   product?;      project; count; vatGroupId; articleId; fromTimestamp;          untilTimestamp;        description;                  netAmount
            |"hsh00-xyz";    "testmann.xy"; "myxyz";   "1";       "02";    "210"; "2020-11-01";           "2020-11-14";          "Laufzeit bis 01.10.21";         "4.50"
            |"""

        val packageBillingItemsCsvFile = givenInputFile named "package-billing-items.csv" containing """
            |customerCode;   product?;      project; count; vatGroupId; articleId; fromTimestamp;          untilTimestamp;        description;                  netAmount
            |"hsh00-xyz";    "xyz01";       "myxyz";   "1";       "03";   "2000"; "2020-11-14";           "2020-12-13";          "Web-Paket";                    "20.00"
            |"""

        val actualBookingsCsvFile = givenOutputFile named "bookings.csv"

        val actualBookingsCsv = Billing(
            configuration,
            periodEndDate = LocalDate.parse("2020-11-30"),
            billingDate = LocalDate.parse("2020-12-03"),
            startInvoiceNumber = 2000,
            vatGroupsCSV = vatGroupsCsvFile,
            customersCSV = customersCsvFile,
            billingItemsCSVs = arrayOf(customerBillingItemsCsvFile, domainItemsCsvFile, packageBillingItemsCsvFile)
        ).generateBookingsCsv(actualBookingsCsvFile).readText()

        assertThat(actualBookingsCsv) matchesInExactOrder """
            |customerNumber;documentNumber;documentDate;referenceDate;referencePeriod;dueDate;directDebiting;vatRate;netAmount;grossAmount;vatAmount;vatAccount
            |"12345";"2020-2000-12345";"03.12.2020";"30.11.2020";"11/2020";"02.01.2021";"true";"0,00";"10,00";"10,00";"0,00";"420000"
            |"12345";"2020-2000-12345";"03.12.2020";"30.11.2020";"11/2020";"02.01.2021";"true";"0,16";"4,50";"5,22";"0,72";"440002"
            |"12345";"2020-2000-12345";"03.12.2020";"30.11.2020";"11/2020";"02.01.2021";"true";"0,16";"20,00";"23,20";"3,20";"440003"
            |"""
    }

    // --- fixture ----------------------------------------------------------

    object configuration : Configuration {
        override val templatesDirectory: String
            // get() = "resources/templates/bookings.csv"
            // get() = "/home/mi/Projekte/Hostsharing/hs-billing-core/app/src/main/resources/templates"
            get() = "/src/main/resources/templates"

    }

    val givenInputDir = createTempDir(prefix = "hs-billing-test-input")
    val givenInputFile = givenInputDir
    val givenOutputDir = createTempDir(prefix = "hs-billing-test-output")
    val givenOutputFile = givenOutputDir

    private infix fun File.named(name: String): File =
        File(givenInputFile, name)

    val semicolonWithSpaces = Regex("; *")

    private infix fun File.containing(content: String): File {
        this.writeText(content.replaceIndentByMargin(marginPrefix = "|").replace(semicolonWithSpaces, ";"))
        return this
    }

    private infix fun Assert<String>.matchesInExactOrder(textBlock: String) = this.isEqualTo(textBlock.replaceIndentByMargin(marginPrefix = "|"))
}

