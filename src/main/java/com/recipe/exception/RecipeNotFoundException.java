package com.recipe.exception;

/**
 * Thrown when a recipe cannot be found for a given identifier.
 */
public class RecipeNotFoundException extends RuntimeException {

    public RecipeNotFoundException(String message) {
        super(message);
    }
}

