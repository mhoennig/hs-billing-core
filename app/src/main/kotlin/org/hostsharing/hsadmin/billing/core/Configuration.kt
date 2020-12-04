package org.hostsharing.hsadmin.billing.core

interface Configuration {

    val templatesDirectory: String

    /**
     * Konten:
     *
     * - nicht-steuerbare Umsätze:                      4200xx
     *
     * - Inland / steuerbare Umsätze (19%, 16%):        4400xx
     *
     * - EU-Ausland mit Unternehmereigenschaft (RC):    4336xx ("RC..."-Text, s.u.)
     * - EU-Ausland ohne Unternehmereigenschaft:        4331xx (Buchungsschlüssel 44, EU-Steuersatz, EU-Land)
     *
     * - Drittland mit Unternehmereigenschaft (RC):     4338xx ("RC..."-Text, s.u.)
     * - Drittland ohne Unternehmereigenschaft:         machen wir nicht
     *
     *  RC... = "Steuerschuldnerschaft des Leistungsempfängers, we apply to reverse charge ..."
     *      der Text evtl. mit Bedingung ins Velocity-Template
     *
     * (xx=Artikelgruppe)
     */
}