package org.hostsharing.hsadmin.billing.core.lib

interface Configuration {

    val templatesDirectory: String
        get() {
            error("not configured")
        }
    val outputDirectory: String
        get() {
            error("not configured")
        }

    val domesticVatCountryCode: String
        get() = "DE"


    val domesticCountryCodes: Array<String>
        get() = arrayOf("DE")

    // TODO: remove with detekt 1.16.x, see https://github.com/detekt/detekt/issues/3291
    @Suppress("NamedArguments")
    val euCountryCodes: Array<String>
        get() = arrayOf("DK", "NL", "BE", "FR", "AT", "IT", "SK", "CZ", "PL", "ES")

    val paymentTermInDays: Long
        get() {
            @Suppress("MagicNumber")
            return 30
        }
}
