package org.hostsharing.hsadmin.billing.core

import assertk.Assert
import assertk.assertThat
import assertk.assertions.isEqualTo
import org.hostsharing.hsadmin.billing.core.lib.Configuration
import org.hostsharing.hsadmin.billing.core.lib.DomainException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.File
import java.time.LocalDate

class BillingIntegrationTest {

    @Test
    fun `will generate accounting-records-csv`() {

        val customersCsvFile = givenInputDir withFile "customers.csv" containing """
            |customerNumber;customerCode;company;salutation;title;firstName;lastName;fullName;co;street;zipCode;city;country;countryCode;email;uidVat;directDebiting;bankCustomer;bankIBAN;bankBIC;mandatRef;vatChargeCode
            |"12345";"hsh00-xyz";"Testmann GmbH";"Herr";"";"Tästi";"Testmann";"Tästi Testmann";"";"Teststraße 42";"20144";"Hamburg";"Germany";"DE";"taesti@taestmann.de";"DE987654321";"true";"Testmann GmbH";"DE81201900030012345678";"GENODEF1HH2";"HS-10003-20140801";"domestic"
            |"""

        val vatGroupsCsvFile = givenInputDir withFile "article-groups.csv" containing """
            |id;    description;            electronicService;    DE;         AT;
            |"00";  "Mitgliedsbeitrag";     "true";             "noTax";    "noTax";    
            |"01";  "Rabatttarif";          "true";             "16,00";    "21,00";
            |"02";  "Domain-Laufzeit";      "true";             "16,00";    "21,00";
            |"03";  "Package";              "true";             "16,00";    "21,00";
            |"04";  "Traffic";              "true";             "16,00";    "21,00";
            |"05";  "CPU";                  "true";             "16,00";    "21,00";
            |"06";  "WoD";                  "false";            "16,00";    "21,00";
            |"07";  "SLA";                  "true";             "16,00";    "21,00";
            |"08";  "BBB";                  "true";             "16,00";    "21,00";
            |"""

        val billingItemsCsvFile = givenInputDir withFile "billing-items.csv" containing """
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

        val actualAccountRecords = Billing(
            configuration,
            periodEndDate = LocalDate.parse("2020-11-30"),
            billingDate = LocalDate.parse("2020-12-03"),
            startInvoiceNumber = 2000,
            vatGroupsCSV = vatGroupsCsvFile,
            customersCSV = customersCsvFile,
            billingItemsCSVs = arrayOf(billingItemsCsvFile)
        ).generateAccountingRecordsCsv().readText()

        assertThat(actualAccountRecords) matchesInExactOrder """
            |customerNumber;documentNumber;documentDate;referenceDate;referencePeriod;dueDate;directDebiting;vatRate;netAmount;grossAmount;vatAmount;vatAccount
            |"12345";"2020-2000-12345";"03.12.2020";"30.11.2020";"11/2020";"02.01.2021";"true";"0,00";"10,00";"10,00";"0,00";"420000"
            |"12345";"2020-2000-12345";"03.12.2020";"30.11.2020";"11/2020";"02.01.2021";"true";"16,00";"10,00";"11,60";"1,60";"440001"
            |"12345";"2020-2000-12345";"03.12.2020";"30.11.2020";"11/2020";"02.01.2021";"true";"16,00";"4,50";"5,22";"0,72";"440002"
            |"12345";"2020-2000-12345";"03.12.2020";"30.11.2020";"11/2020";"02.01.2021";"true";"16,00";"24,00";"27,84";"3,84";"440003"
            |"12345";"2020-2000-12345";"03.12.2020";"30.11.2020";"11/2020";"02.01.2021";"true";"16,00";"5,00";"5,80";"0,80";"440004"
            |"12345";"2020-2000-12345";"03.12.2020";"30.11.2020";"11/2020";"02.01.2021";"true";"16,00";"25,00";"29,00";"4,00";"440006"
            |"12345";"2020-2000-12345";"03.12.2020";"30.11.2020";"11/2020";"02.01.2021";"true";"16,00";"10,00";"11,60";"1,60";"440007"
            |"12345";"2020-2000-12345";"03.12.2020";"30.11.2020";"11/2020";"02.01.2021";"true";"16,00";"15,00";"17,40";"2,40";"440005"
            |"12345";"2020-2000-12345";"03.12.2020";"30.11.2020";"11/2020";"02.01.2021";"true";"16,00";"15,00";"17,40";"2,40";"440008"
            |"""
    }

    @Test
    fun `will consider multiple billing-item-files`() {

        val customersCsvFile = givenInputDir withFile "customers.csv" containing """
            |customerNumber;customerCode;company;salutation;title;firstName;lastName;fullName;co;street;zipCode;city;country;countryCode;email;uidVat;directDebiting;bankCustomer;bankIBAN;bankBIC;mandatRef;vatChargeCode
            |"12345";"hsh00-xyz";"Testmann GmbH";"Herr";"";"Tästi";"Testmann";"Tästi Testmann";"";"Teststraße 42";"20144";"Hamburg";"Germany";"DE";"taesti@taestmann.de";"DE987654321";"true";"Testmann GmbH";"DE81201900030012345678";"GENODEF1HH2";"HS-10003-20140801";"domestic"
            |"""

        val vatGroupsCsvFile = givenInputDir withFile "article-groups.csv" containing """
            |id;    description;               electronicService;    DE;         AT;
            |"00";  "Mitgliedsbeitrag";     "true";             "noTax";    "noTax";    
            |"02";  "Domain-Laufzeit";      "true";             "16,00";    "21,00";
            |"03";  "Package";              "true";             "16,00";    "21,00";
            |"""

        val customerBillingItemsCsvFile = givenInputDir withFile "customer-billing-items.csv" containing """
            |customerCode;   product?;      project; count; vatGroupId; articleId; fromTimestamp;          untilTimestamp;        description;                  netAmount
            |"hsh00-xyz";    "";            ;          "1";       "00";      "0"; "2020-11-14";           "2020-11-14";          "Mitgliedsbeitrag";             "10.00"
            |"""

        val domainItemsCsvFile = givenInputDir withFile "domain-billing-items.csv" containing """
            |customerCode;   product?;      project; count; vatGroupId; articleId; fromTimestamp;          untilTimestamp;        description;                  netAmount
            |"hsh00-xyz";    "testmann.xy"; "myxyz";   "1";       "02";    "210"; "2020-11-01";           "2020-11-14";          "Laufzeit bis 01.10.21";         "4.50"
            |"""

        val packageBillingItemsCsvFile = givenInputDir withFile "package-billing-items.csv" containing """
            |customerCode;   product?;      project; count; vatGroupId; articleId; fromTimestamp;          untilTimestamp;        description;                  netAmount
            |"hsh00-xyz";    "xyz01";       "myxyz";   "1";       "03";   "2000"; "2020-11-14";           "2020-12-13";          "Web-Paket";                    "20.00"
            |"""

        val actualAccountRecords = Billing(
            configuration,
            periodEndDate = LocalDate.parse("2020-11-30"),
            billingDate = LocalDate.parse("2020-12-03"),
            startInvoiceNumber = 2000,
            vatGroupsCSV = vatGroupsCsvFile,
            customersCSV = customersCsvFile,
            billingItemsCSVs = arrayOf(customerBillingItemsCsvFile, domainItemsCsvFile, packageBillingItemsCsvFile)
        ).generateAccountingRecordsCsv().readText()

        assertThat(actualAccountRecords) matchesInExactOrder """
            |customerNumber;documentNumber;documentDate;referenceDate;referencePeriod;dueDate;directDebiting;vatRate;netAmount;grossAmount;vatAmount;vatAccount
            |"12345";"2020-2000-12345";"03.12.2020";"30.11.2020";"11/2020";"02.01.2021";"true";"0,00";"10,00";"10,00";"0,00";"420000"
            |"12345";"2020-2000-12345";"03.12.2020";"30.11.2020";"11/2020";"02.01.2021";"true";"16,00";"4,50";"5,22";"0,72";"440002"
            |"12345";"2020-2000-12345";"03.12.2020";"30.11.2020";"11/2020";"02.01.2021";"true";"16,00";"20,00";"23,20";"3,20";"440003"
            |"""
    }

    @Test
    fun `will choose VAT-accounts based on vatChargeCode`() {

        val customersCsvFile = givenInputDir withFile "customers.csv" containing """
            |customerNumber;customerCode;company;salutation;title;firstName;lastName;fullName;co;street;zipCode;city;country;countryCode;email;uidVat;directDebiting;bankCustomer;bankIBAN;bankBIC;mandatRef;vatChargeCode
            |"10001";"hsh00-dee";"Testmann GmbH";"Herr";"";"Tästi";"Testmann";"Tästi Testmann";"";"Teststraße 42";"20144";"Hamburg";"Germany";    "DE";"taesti@taestmann.de";"DE987654321";"true";"Tästmann GmbH"; "DE81201900030012345678";"GENODEF1HH2";"HS-10001-20140801";"domestic"
            |"10001";"hsh00-dep";               ;"Herr";"";"Tästi";"Testmann";"Tästi Testmann";"";"Teststraße 42";"20144";"Hamburg";"Germany";    "DE";"taesti@taestmann.de";             ;"true";"Tästi Tästmann";"DE81201900030012345678";"GENODEF1HH2";"HS-10001-20140801";"domestic"
            |"10002";"hsh00-ate";"Testmann GmbH";"Herr";"";"Tästi";"Testmann";"Tästi Testmann";"";"Teststraße 42";"10010";"Wien";   "Austria";    "AT";"taesti@taestmann.de";"AT123456789";"true";"Testmann GmbH"; "DE81201900030012345678";"GENODEF1HH2";"HS-10002-20140801";"EU-RC"
            |"10002";"hsh00-atp";               ;"Herr";"";"Tästi";"Testmann";"Tästi Testmann";"";"Teststraße 42";"10010";"Wien";   "Austria";    "AT";"taesti@taestmann.de";             ;"true";"Testmann GmbH"; "DE81201900030012345678";"GENODEF1HH2";"HS-10002-20140801";"EU"
            |"10003";"hsh00-che";"Testmann GmbH";"Herr";"";"Tästi";"Testmann";"Tästi Testmann";"";"Teststraße 42";"20020";"Zürich"; "Switzerland";"AT";"taesti@taestmann.de";"CH123456789";"true";"Testmann GmbH"; "DE81201900030012345678";"GENODEF1HH2";"HS-10002-20140801";"NonEU-RC"
            |"""

        val vatGroupsCsvFile = givenInputDir withFile "article-groups.csv" containing """
            |id;    description;            electronicService;    DE;         AT;
            |"00";  "Mitgliedsbeitrag";     "true";               "noTax";    "noTax";    
            |"03";  "Package";              "true";               "16,00";    "21,00";
            |"""

        val billingItemsCsvFile = givenInputDir withFile "customer-billing-items.csv" containing """
            |customerCode;   product?;      project; count; vatGroupId; articleId; fromTimestamp;          untilTimestamp;        description;                  netAmount
            |"hsh00-dee";    "";            ;          "1";       "00";      "0"; "2020-11-14";           "2020-11-14";          "Mitgliedsbeitrag";            "10.00"
            |"hsh00-dee";    "";            ;          "1";       "03";     "10"; "2020-11-14";           "2020-11-14";          "Web-Paket";                   "20.00"
            |"hsh00-dep";    "";            ;          "1";       "00";      "0"; "2020-11-14";           "2020-11-14";          "Mitgliedsbeitrag";            "10.00"
            |"hsh00-dep";    "";            ;          "1";       "03";     "10"; "2020-11-14";           "2020-11-14";          "Web-Paket";                   "20.00"
            |"hsh00-ate";    "";            ;          "1";       "00";      "0"; "2020-11-14";           "2020-11-14";          "Mitgliedsbeitrag";            "10.00"
            |"hsh00-ate";    "";            ;          "1";       "03";     "10"; "2020-11-14";           "2020-11-14";          "Web-Paket";                   "20.00"
            |"hsh00-atp";    "";            ;          "1";       "00";      "0"; "2020-11-14";           "2020-11-14";          "Mitgliedsbeitrag";            "10.00"
            |"hsh00-atp";    "";            ;          "1";       "03";     "10"; "2020-11-14";           "2020-11-14";          "Web-Paket";                   "20.00"
            |"hsh00-che";    "";            ;          "1";       "00";      "0"; "2020-11-14";           "2020-11-14";          "Mitgliedsbeitrag";            "10.00"
            |"hsh00-che";    "";            ;          "1";       "03";     "10"; "2020-11-14";           "2020-11-14";          "Web-Paket";                   "20.00"
            |"""

        val actualAccountingRecordsCsv = Billing(
            configuration,
            periodEndDate = LocalDate.parse("2020-11-30"),
            billingDate = LocalDate.parse("2020-12-03"),
            startInvoiceNumber = 2000,
            vatGroupsCSV = vatGroupsCsvFile,
            customersCSV = customersCsvFile,
            billingItemsCSVs = arrayOf(billingItemsCsvFile)
        ).generateAccountingRecordsCsv().readText()

        assertThat(actualAccountingRecordsCsv) matchesInExactOrder """
            |customerNumber;documentNumber;documentDate;referenceDate;referencePeriod;dueDate;directDebiting;vatRate;netAmount;grossAmount;vatAmount;vatAccount
            |"10001";"2020-2000-10001";"03.12.2020";"30.11.2020";"11/2020";"02.01.2021";"true";"0,00";"10,00";"10,00";"0,00";"420000"
            |"10001";"2020-2000-10001";"03.12.2020";"30.11.2020";"11/2020";"02.01.2021";"true";"16,00";"20,00";"23,20";"3,20";"440003"
            |"10001";"2020-2001-10001";"03.12.2020";"30.11.2020";"11/2020";"02.01.2021";"true";"0,00";"10,00";"10,00";"0,00";"420000"
            |"10001";"2020-2001-10001";"03.12.2020";"30.11.2020";"11/2020";"02.01.2021";"true";"16,00";"20,00";"23,20";"3,20";"440003"
            |"10002";"2020-2002-10002";"03.12.2020";"30.11.2020";"11/2020";"02.01.2021";"true";"0,00";"10,00";"10,00";"0,00";"420000"
            |"10002";"2020-2002-10002";"03.12.2020";"30.11.2020";"11/2020";"02.01.2021";"true";"21,00";"20,00";"24,20";"4,20";"433603"
            |"10002";"2020-2003-10002";"03.12.2020";"30.11.2020";"11/2020";"02.01.2021";"true";"0,00";"10,00";"10,00";"0,00";"420000"
            |"10002";"2020-2003-10002";"03.12.2020";"30.11.2020";"11/2020";"02.01.2021";"true";"21,00";"20,00";"24,20";"4,20";"433103"
            |"10003";"2020-2004-10003";"03.12.2020";"30.11.2020";"11/2020";"02.01.2021";"true";"0,00";"10,00";"10,00";"0,00";"420000"
            |"10003";"2020-2004-10003";"03.12.2020";"30.11.2020";"11/2020";"02.01.2021";"true";"21,00";"20,00";"24,20";"4,20";"433803"
            |"""
    }

    @Test
    fun `will report missing field and invalid record on invalid CSV`() {

        val customersCsvFile = givenInputDir withFile "customers.csv" containing """
            |customerNumber;customerCode;salutation
            |"10001";"hsh00-dee";"Herr"
            |"""

        val vatGroupsCsvFile = givenInputDir withFile "article-groups.csv" containing """
            |id;    description;            electronicService;    DE;         AT;
            |"00";  "Mitgliedsbeitrag";     "true";               "noTax";    "noTax";    
            |"03";  "Package";              "true";               "16,00";    "20,00";
            |"""

        val actualException = assertThrows<DomainException> {
            Billing(
                configuration,
                periodEndDate = LocalDate.parse("2020-11-30"),
                billingDate = LocalDate.parse("2020-12-03"),
                startInvoiceNumber = 2000,
                vatGroupsCSV = vatGroupsCsvFile,
                customersCSV = customersCsvFile,
                billingItemsCSVs = emptyArray()
            ).generateAccountingRecordsCsv().readText()
        }

        assertThat(actualException.message).isEqualTo(
            """
            billing contact without firstName
            - in parsing billing contact {customerNumber=10001, customerCode=hsh00-dee, salutation=Herr}
            - in parsing customer {customerNumber=10001, customerCode=hsh00-dee, salutation=Herr}
            - in reading customers: customers.csv
            """.trimIndent()
        )
    }

    // --- fixture ----------------------------------------------------------

    val givenInputDir = createTempDir(prefix = "hs-billing-test-input")

    object configuration : Configuration {
        override val templatesDirectory = "/src/main/resources/templates"
        override val outputDirectory = createTempDir(prefix = "hs-billing-test-output").also {
            it.mkdirs()
        }.toString()
        override val accountBaseForNonTaxableRevenues = "4200"
        override val accountBaseForTaxableDomesticRevenues = "4400"
        override val accountBaseForTaxableForeignEuRevenuesReverseCharge = "4336"
        override val accountBaseForTaxableForeignEuRevenues = "4331"
        override val accountBaseForTaxableAbroadEuRevenuesReverseCharge = "4338"
    }

    private infix fun File.withFile(name: String): File =
        File(givenInputDir, name)

    val semicolonWithSpaces = Regex("; *")

    private infix fun File.containing(content: String): File {
        this.writeText(content.replaceIndentByMargin(marginPrefix = "|").replace(semicolonWithSpaces, ";"))
        return this
    }

    private infix fun Assert<String>.matchesInExactOrder(textBlock: String) = this.isEqualTo(textBlock.replaceIndentByMargin(marginPrefix = "|"))
}
