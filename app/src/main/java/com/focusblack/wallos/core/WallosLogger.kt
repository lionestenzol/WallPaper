package com.focusblack.wallos.core

import android.util.Log

object WallosLogger {
    fun info(tag: String, event: String, attributes: Map<String, Any?> = emptyMap()) {
        Log.i(tag, buildMessage(event, attributes))
    }

    fun warn(tag: String, event: String, attributes: Map<String, Any?> = emptyMap()) {
        Log.w(tag, buildMessage(event, attributes))
    }

    fun error(
        tag: String,
        event: String,
        attributes: Map<String, Any?> = emptyMap(),
        throwable: Throwable? = null
    ) {
        Log.e(tag, buildMessage(event, attributes), throwable)
    }

    private fun buildMessage(event: String, attributes: Map<String, Any?>): String {
        if (attributes.isEmpty()) {
            return "event=${sanitize(event)}"
        }

        val builder = StringBuilder("event=${sanitize(event)}")
        attributes.forEach { (key, value) ->
            builder.append(' ')
            builder.append(sanitize(key))
            builder.append('=')
            builder.append(formatValue(value))
        }
        return builder.toString()
    }

    private fun formatValue(value: Any?): String {
        val raw = value?.toString() ?: "null"
        val sanitized = sanitize(raw)
        return if (sanitized.contains(' ')) "\"$sanitized\"" else sanitized
    }

    private fun sanitize(value: String): String = value.replace("\n", " ").trim()
}
