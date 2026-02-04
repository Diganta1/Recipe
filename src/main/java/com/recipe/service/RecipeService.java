package com.recipe.service;

import com.recipe.dto.RecipeDTO;
import com.recipe.dto.RecipeSearchDTO;
import com.recipe.entity.Ingredient;
import com.recipe.entity.Recipe;
import com.recipe.exception.RecipeNotFoundException;
import com.recipe.mapper.RecipeMapper;
import com.recipe.repository.IngredientRepository;
import com.recipe.repository.RecipeRepository;
import com.recipe.util.NameNormalizer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for Recipe management
 */
@Service
@AllArgsConstructor
@Slf4j
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final IngredientRepository ingredientRepository;
    private final RecipeMapper recipeMapper;

    /**
     * Get all recipes
     */
    @Transactional(readOnly = true)
    public List<RecipeDTO> getAllRecipes() {
        log.info("Fetching all recipes");
        return recipeRepository.findAll()
                .stream()
                .map(recipeMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get recipe by ID
     */
    @Transactional(readOnly = true)
    public RecipeDTO getRecipeById(Long id) {
        log.info("Fetching recipe with id: {}", id);
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RecipeNotFoundException("Recipe not found with id: " + id));
        return recipeMapper.toDTO(recipe);
    }

    /**
     * Create a new recipe
     */
    @Transactional
    public RecipeDTO createRecipe(RecipeDTO recipeDTO) {
        log.info("Creating new recipe: {}", recipeDTO.getName());

        Recipe recipe = recipeMapper.toEntityForCreate(recipeDTO);
        recipe.setIngredients(resolveIngredients(recipeDTO.getIngredients()));

        Recipe savedRecipe = recipeRepository.save(recipe);
        return recipeMapper.toDTO(savedRecipe);
    }

    /**
     * Update an existing recipe
     */
    @Transactional
    public RecipeDTO updateRecipe(Long id, RecipeDTO recipeDTO) {
        log.info("Updating recipe with id: {}", id);
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RecipeNotFoundException("Recipe not found with id: " + id));

        recipeMapper.updateEntity(recipe, recipeDTO);

        // Replace ingredients
        recipe.getIngredients().clear();
        recipe.getIngredients().addAll(resolveIngredients(recipeDTO.getIngredients()));

        Recipe updatedRecipe = recipeRepository.save(recipe);
        return recipeMapper.toDTO(updatedRecipe);
    }

    /**
     * Delete a recipe
     */
    public void deleteRecipe(Long id) {
        log.info("Deleting recipe with id: {}", id);
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RecipeNotFoundException("Recipe not found with id: " + id));
        recipeRepository.delete(recipe);
    }

    /**
     * Search recipes based on filters
     */
    @Transactional(readOnly = true)
    public List<RecipeDTO> searchRecipes(RecipeSearchDTO searchDTO) {
        log.info("Searching recipes with filters: {}", searchDTO);
        List<Recipe> allRecipes = recipeRepository.findAll();

        return allRecipes.stream()
                .filter(recipe -> filterByVegetarian(recipe, searchDTO.getIsVegetarian()))
                .filter(recipe -> filterByServings(recipe, searchDTO.getServings()))
                .filter(recipe -> filterByIncludeIngredients(recipe, searchDTO.getIncludeIngredients()))
                .filter(recipe -> filterByExcludeIngredients(recipe, searchDTO.getExcludeIngredients()))
                .filter(recipe -> filterByInstructions(recipe, searchDTO.getInstructionsKeyword()))
                .map(recipeMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Filter by vegetarian status
     */
    private boolean filterByVegetarian(Recipe recipe, Boolean isVegetarian) {
        if (isVegetarian == null) {
            return true;
        }
        return Boolean.TRUE.equals(recipe.getIsVegetarian()) == isVegetarian;
    }

    /**
     * Filter by servings
     */
    private boolean filterByServings(Recipe recipe, Integer servings) {
        return servings == null || (recipe.getServings() != null && recipe.getServings().equals(servings));
    }

    private Set<String> recipeIngredientNamesLower(Recipe recipe) {
        if (recipe.getIngredients() == null || recipe.getIngredients().isEmpty()) {
            return Collections.emptySet();
        }
        return recipe.getIngredients().stream()
                .map(Ingredient::getName)
                .map(NameNormalizer::trimToLowerSafe)
                .filter(Objects::nonNull)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    /**
     * Filter by included ingredients
     */
    private boolean filterByIncludeIngredients(Recipe recipe, String includeIngredients) {
        String normalizedInclude = NameNormalizer.trimToNullSafe(includeIngredients);
        if (normalizedInclude == null || normalizedInclude.isEmpty()) {
            return true;
        }

        Set<String> recipeIngredients = recipeIngredientNamesLower(recipe);
        if (recipeIngredients.isEmpty()) {
            return false;
        }

        String[] ingredients = normalizedInclude.split(",");
        for (String ingredient : ingredients) {
            String tokenLower = NameNormalizer.trimToLowerSafe(ingredient);
            if (tokenLower != null && !tokenLower.isEmpty() && !recipeIngredients.contains(tokenLower)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Filter by excluded ingredients
     */
    private boolean filterByExcludeIngredients(Recipe recipe, String excludeIngredients) {
        String normalizedExclude = NameNormalizer.trimToNullSafe(excludeIngredients);
        if (normalizedExclude == null || normalizedExclude.isEmpty()) {
            return true;
        }

        Set<String> recipeIngredients = recipeIngredientNamesLower(recipe);
        if (recipeIngredients.isEmpty()) {
            return true;
        }

        String[] ingredients = normalizedExclude.split(",");
        for (String ingredient : ingredients) {
            String tokenLower = NameNormalizer.trimToLowerSafe(ingredient);
            if (tokenLower != null && !tokenLower.isEmpty() && recipeIngredients.contains(tokenLower)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Filter by instructions keyword
     */
    private boolean filterByInstructions(Recipe recipe, String instructionsKeyword) {
        String tokenLower = NameNormalizer.trimToLowerSafe(instructionsKeyword);
        if (tokenLower == null || tokenLower.isEmpty()) {
            return true;
        }
        if (recipe.getInstructions() == null) {
            return false;
        }
        return recipe.getInstructions().toLowerCase().contains(tokenLower);
    }

    private Set<Ingredient> resolveIngredients(List<String> ingredientNames) {
        if (ingredientNames == null) {
            return new HashSet<>();
        }

        Set<String> normalized = ingredientNames.stream()
                .map(NameNormalizer::trimToNullSafe)
                .filter(Objects::nonNull)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Set<Ingredient> ingredients = new HashSet<>();
        for (String name : normalized) {
            Ingredient ingredient = ingredientRepository.findByNameIgnoreCase(name)
                    .orElseGet(() -> ingredientRepository.save(Ingredient.builder().name(name).build()));
            ingredients.add(ingredient);
        }
        return ingredients;
    }
}
