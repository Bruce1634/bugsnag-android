package com.bugsnag.android

import java.io.IOException
import java.lang.reflect.Array

internal class ObjectJsonStreamer {

    companion object {
        private const val REDACTED_PLACEHOLDER = "[REDACTED]"
        private const val OBJECT_PLACEHOLDER = "[OBJECT]"
    }

    val redactKeys = mutableSetOf("password")

    // Write complex/nested values to a JsonStreamer
    @Throws(IOException::class)
    fun objectToStream(obj: Any?, writer: JsonStream) {
        when {
            obj == null -> writer.nullValue()
            obj is String -> writer.value(obj)
            obj is Number -> writer.value(obj)
            obj is Boolean -> writer.value(obj)
            obj is JsonStream.Streamable -> obj.toStream(writer)
            obj is Map<*, *> -> mapToStream(writer, obj)
            obj is Collection<*> -> collectionToStream(writer, obj)
            obj.javaClass.isArray -> arrayToStream(writer, obj)
            else -> writer.value(OBJECT_PLACEHOLDER)
        }
    }

    private fun mapToStream(writer: JsonStream, obj: Map<*, *>) {
        writer.beginObject()
        obj.entries.forEach {
            val keyObj = it.key
            if (keyObj is String) {
                writer.name(keyObj)
                if (shouldRedact(keyObj)) {
                    writer.value(REDACTED_PLACEHOLDER)
                } else {
                    objectToStream(it.value, writer)
                }
            }
        }
        writer.endObject()
    }

    private fun collectionToStream(writer: JsonStream, obj: Collection<*>) {
        writer.beginArray()
        obj.forEach { objectToStream(it, writer) }
        writer.endArray()
    }

    private fun arrayToStream(writer: JsonStream, obj: Any?) {
        // Primitive array objects
        writer.beginArray()
        val length = Array.getLength(obj)
        var i = 0
        while (i < length) {
            objectToStream(Array.get(obj, i), writer)
            i += 1
        }
        writer.endArray()
    }

    // Should this key be redacted
    private fun shouldRedact(key: String) = redactKeys.any { key.contains(it) }

}