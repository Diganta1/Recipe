package com.recipe.util;

/**
 * Simple helper to normalize free-text names coming from API payloads.
 */
public final class NameNormalizer {

    private NameNormalizer() {

    }

    /**
     * Normalizes a user-provided string by trimming it.
     *
     * @param value input value (may be null)
     * @return trimmed string (may be null)
     */
    public static String trimToNullSafe(String value) {
        if (value == null) {
            return null;
        }
        return value.trim();
    }

    /**
     * Convenience helper for case-insensitive matching.
     * Trims the input and lowercases it. Returns {@code null} if input is null.
     */
    public static String trimToLowerSafe(String value) {
        String trimmed = trimToNullSafe(value);
        return trimmed == null ? null : trimmed.toLowerCase();
    }
}
