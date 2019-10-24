package com.bugsnag.android;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class ObjectJsonStreamer {

    private static final String FILTERED_PLACEHOLDER = "[FILTERED]";
    private static final String OBJECT_PLACEHOLDER = "[OBJECT]";

    Set<String> redactKeys;

    public ObjectJsonStreamer() {
        this.redactKeys = new HashSet<>();
        this.redactKeys.add("password");
    }

    // Write complex/nested values to a JsonStreamer
    void objectToStream(@Nullable Object obj,
                        @NonNull JsonStream writer) throws IOException {
        if (obj == null) {
            writer.nullValue();
        } else if (obj instanceof String) {
            writer.value((String) obj);
        } else if (obj instanceof Number) {
            writer.value((Number) obj);
        } else if (obj instanceof Boolean) {
            writer.value((Boolean) obj);
        } else if (obj instanceof Map) {
            // Map objects
            writer.beginObject();
            for (Object o : ((Map) obj).entrySet()) {

                @SuppressWarnings("unchecked")
                Map.Entry<Object, Object> entry = (Map.Entry<Object, Object>) o;

                Object keyObj = entry.getKey();
                if (keyObj instanceof String) {
                    String key = (String) keyObj;
                    writer.name(key);
                    if (shouldRedact(key)) {
                        writer.value(FILTERED_PLACEHOLDER);
                    } else {
                        objectToStream(entry.getValue(), writer);
                    }
                }
            }
            writer.endObject();
        } else if (obj instanceof Collection) {
            // Collection objects (Lists, Sets etc)
            writer.beginArray();
            for (Object entry : (Collection) obj) {
                objectToStream(entry, writer);
            }
            writer.endArray();
        } else if (obj.getClass().isArray()) {
            // Primitive array objects
            writer.beginArray();
            int length = Array.getLength(obj);
            for (int i = 0; i < length; i += 1) {
                objectToStream(Array.get(obj, i), writer);
            }
            writer.endArray();
        } else {
            writer.value(OBJECT_PLACEHOLDER);
        }
    }

    // Should this key be redacted
    private boolean shouldRedact(@Nullable String key) {
        if (redactKeys == null || key == null) {
            return false;
        }

        for (String filter : redactKeys) {
            if (key.contains(filter)) {
                return true;
            }
        }

        return false;
    }

}