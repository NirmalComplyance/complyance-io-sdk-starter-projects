package io.complyance.sdk;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Purpose types for GETS Unify API
 */
public enum Purpose {
    MAPPING("mapping"),
    INVOICING("invoicing");

    private final String value;

    Purpose(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static Purpose fromString(String value) {
        for (Purpose purpose : Purpose.values()) {
            if (purpose.value.equals(value)) {
                return purpose;
            }
        }
        throw new IllegalArgumentException("Unknown purpose: " + value);
    }

    @Override
    public String toString() {
        return value;
    }
}