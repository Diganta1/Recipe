package com.recipe.dto;

/**
 * Search mode for ingredient name matching.
 *
 * <ul>
 *   <li>{@link #CONTAINS}: case-insensitive substring match</li>
 *   <li>{@link #STARTS_WITH}: case-insensitive prefix match</li>
 *   <li>{@link #EXACT}: case-insensitive full string match</li>
 * </ul>
 */
public enum IngredientSearchMode {
    CONTAINS,
    STARTS_WITH,
    EXACT
}
