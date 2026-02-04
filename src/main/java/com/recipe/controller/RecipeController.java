package com.recipe.controller;

import com.recipe.dto.RecipeDTO;
import com.recipe.dto.RecipeSearchDTO;
import com.recipe.service.RecipeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * REST Controller for Recipe management
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/recipes")
@AllArgsConstructor
@Tag(name = "Recipe Management", description = "API endpoints for managing recipes")
public class RecipeController {

    private final RecipeService recipeService;

    /**
     * Get all recipes
     */
    @GetMapping
    @Operation(summary = "Get all recipes", description = "Retrieve all recipes from the system")
    public ResponseEntity<List<RecipeDTO>> getAllRecipes() {
        log.info("GET request: /api/v1/recipes");
        List<RecipeDTO> recipes = recipeService.getAllRecipes();
        return ResponseEntity.ok(recipes);
    }

    /**
     * Get recipe by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get recipe by ID", description = "Retrieve a specific recipe by its ID")
    public ResponseEntity<RecipeDTO> getRecipeById(@PathVariable Long id) {
        log.info("GET request: /api/v1/recipes/{}", id);
        RecipeDTO recipe = recipeService.getRecipeById(id);
        return ResponseEntity.ok(recipe);
    }

    /**
     * Create a new recipe
     */
    @PostMapping
    @Operation(summary = "Create a new recipe", description = "Add a new recipe to the system")
    public ResponseEntity<RecipeDTO> createRecipe(@Valid @RequestBody RecipeDTO recipeDTO) {
        log.info("POST request: /api/v1/recipes - Creating recipe: {}", recipeDTO.getName());
        RecipeDTO createdRecipe = recipeService.createRecipe(recipeDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRecipe);
    }

    /**
     * Update an existing recipe
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update an existing recipe", description = "Update details of an existing recipe")
    public ResponseEntity<RecipeDTO> updateRecipe(
            @PathVariable Long id,
            @Valid @RequestBody RecipeDTO recipeDTO) {
        log.info("PUT request: /api/v1/recipes/{} - Updating recipe", id);
        RecipeDTO updatedRecipe = recipeService.updateRecipe(id, recipeDTO);
        return ResponseEntity.ok(updatedRecipe);
    }

    /**
     * Delete a recipe
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a recipe", description = "Remove a recipe from the system")
    public ResponseEntity<Void> deleteRecipe(@PathVariable Long id) {
        log.info("DELETE request: /api/v1/recipes/{}", id);
        recipeService.deleteRecipe(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Search recipes with filters
     */
    @PostMapping("/search")
    @Operation(summary = "Search recipes", description = "Search recipes based on various filters (vegetarian, servings, ingredients, instructions)")
    public ResponseEntity<List<RecipeDTO>> searchRecipes(@RequestBody RecipeSearchDTO searchDTO) {
        log.info("POST request: /api/v1/recipes/search - Searching recipes with filters: {}", searchDTO);
        List<RecipeDTO> results = recipeService.searchRecipes(searchDTO);
        return ResponseEntity.ok(results);
    }
}
