package org.hostsharing.hsadmin.billing.core.domain

interface Formattable {

    fun format(indent: Int = 0): String

    fun formatted(indent: Int = 0): String =
        format(indent).replaceIndentByMargin(marginPrefix = "|", newIndent = " ".repeat(indent))
}
