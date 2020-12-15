package org.hostsharing.hsadmin.billing.core.lib

interface Configuration {

    val templatesDirectory: String
        get() { error("not configured") }
    val outputDirectory: String
        get() { error("not configured") }

    val domesticCountryCode: String
        get() = "DE"
}
