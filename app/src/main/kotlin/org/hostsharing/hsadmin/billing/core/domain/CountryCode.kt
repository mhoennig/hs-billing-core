package org.hostsharing.hsadmin.billing.core.domain

typealias CountryCode = String

fun String.isCountryCode() =
    this.length == 2 && this == this.toUpperCase() && this[0].isLetter() && this[1].isLetter()
