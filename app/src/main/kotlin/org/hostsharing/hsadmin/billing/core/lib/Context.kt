package org.hostsharing.hsadmin.billing.core.lib

import java.util.*

class Context {
    companion object {
        val infos = Stack<String>()

        fun log(message: String) {
            System.out.println(message)
        }

        fun err(message: String) {
            System.err.println(message)
        }
    }
}

inline fun <T> withContext(contextInfo: String, body: () -> T): T {
    try {
        Context.infos.push(contextInfo)
        try {
            return body()
        } catch ( exc: ContextException ) {
            Context.err("- within " + contextInfo)
            throw exc
        } catch ( exc: Exception ) {
            Context.err(contextInfo + ": " + exc.message)
            throw ContextException(exc.message ?: exc.javaClass.simpleName, exc)
        }
    } finally {
        Context.infos.pop()
    }
}

class ContextException(message: String, exc: Exception) : Exception(message, exc)
