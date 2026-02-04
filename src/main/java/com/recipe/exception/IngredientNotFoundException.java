package com.recipe.exception;

/**
 * Thrown when an ingredient cannot be found for a given identifier.
 */
public class IngredientNotFoundException extends RuntimeException {

    public IngredientNotFoundException(String message) {
        super(message);
    }
}

