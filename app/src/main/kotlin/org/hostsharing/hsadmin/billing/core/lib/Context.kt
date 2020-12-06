package org.hostsharing.hsadmin.billing.core.lib

import java.util.*

class Context {
    companion object {
        val infos = Stack<String>()

        override fun toString(): String =
            infos.reversed().map{ "- while ${it}"}.joinToString("\n")
    }
}

fun <T> withContext(contextInfo: String, body: () -> T): T {
    try {
        Context.infos.push(contextInfo)
        try {
            return body()
        } catch ( exc: ContextException ) {
            throw exc
        } catch ( exc: Exception ) {
            throw ContextException(
                (exc.message ?: exc.javaClass.simpleName) + "\n" + Context.toString(),
                exc)
        }
    } finally {
        Context.infos.pop()
    }
}

class ContextException(message: String, exc: Exception) : Exception(message, exc)
