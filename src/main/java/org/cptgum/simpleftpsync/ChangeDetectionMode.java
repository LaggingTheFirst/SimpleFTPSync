package org.cptgum.simpleftpsync;

public enum ChangeDetectionMode {
    METADATA,
    CHECKSUM;

    public static ChangeDetectionMode fromConfig(String value) {
        if (value == null || value.trim().isEmpty()) {
            return CHECKSUM;
        }

        for (ChangeDetectionMode mode : values()) {
            if (mode.name().equalsIgnoreCase(value)) {
                return mode;
            }
        }

        return CHECKSUM;
    }
}
