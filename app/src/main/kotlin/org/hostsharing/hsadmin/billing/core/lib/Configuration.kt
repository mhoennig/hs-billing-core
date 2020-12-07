package org.hostsharing.hsadmin.billing.core.lib

interface Configuration {

    val templatesDirectory: String

    val accountBaseForNonTaxableRevenues: String                    // "4200" + vatGroupId

    val accountBaseForTaxableDomesticRevenues: String               // "4400" + vatGroupId

    val accountBaseForTaxableForeignEuRevenuesReverseCharge: String // "4336" + vatGroupId & RC-Text, s.u.
    val accountBaseForTaxableForeignEuRevenues: String              // "4331" + vatGroupId & Non-RC-Text, s.u.

    val accountBaseForTaxableAbroadEuRevenuesReverseCharge: String  // "4338" + vatGroupId

    /**
     * Diese Texte müssen über das Velocity-Template in die Rechnungen aufgenommen werden:
     *
     *  RC-Text:
     *  "Steuerschuldnerschaft des Leistungsempfängers, we apply to reverse charge ..."
     *      der Text evtl. mit Bedingung ins Velocity-Template
     *
     *  Non-RC-Text:
     *  "Buchungsschlüssel 44, EU-Steuersatz, EU-Land"
     */
}

