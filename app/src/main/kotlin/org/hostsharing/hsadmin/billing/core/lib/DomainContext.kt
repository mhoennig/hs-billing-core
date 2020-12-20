package org.hostsharing.hsadmin.billing.core.lib

import java.util.*

object DomainContext {
    val infos = Stack<String>()

    override fun toString(): String =
        infos.reversed().map { "- in $it" }.joinToString("\n")
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

fun validationError(message: String, cause: Throwable): Nothing =
    throw IllegalStateException(message, cause)

fun validationError(message: String): Nothing =
    throw IllegalStateException(message)
