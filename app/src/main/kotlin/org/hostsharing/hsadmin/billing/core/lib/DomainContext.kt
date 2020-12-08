package org.hostsharing.hsadmin.billing.core.lib

import java.util.*

class DomainContext {
    companion object {
        val infos = Stack<String>()

        override fun toString(): String =
            infos.reversed().map { "- in $it" }.joinToString("\n")
    }
}

fun <T> withDomainContext(contextInfo: String, body: () -> T): T {
    try {
        DomainContext.infos.push(contextInfo)
        try {
            return body()
        } catch (exc: DomainException) {
            throw exc
        } catch (exc: Exception) {
            throw DomainException(
                (exc.message ?: exc.javaClass.simpleName) + "\n" + DomainContext.toString(),
                exc
            )
        }
    } finally {
        DomainContext.infos.pop()
    }
}

class DomainException(message: String, exc: Exception) : Exception(message, exc)
