package com.recipe.mapper;

import com.recipe.dto.RecipeDTO;
import com.recipe.entity.Ingredient;
import com.recipe.entity.Recipe;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Mapper for converting between {@link Recipe} entities and {@link RecipeDTO} DTOs.
 *
 * Note: This mapper does not resolve ingredient names into {@link Ingredient} entities.
 * That resolution is handled in the service layer via {@code IngredientRepository}.
 */
@Component
@Slf4j
public class RecipeMapper {

    public RecipeDTO toDTO(Recipe recipe) {
        if (recipe == null) {
            return null;
        }

        RecipeDTO dto = new RecipeDTO();
        dto.setId(recipe.getId());
        dto.setName(recipe.getName());
        dto.setDescription(recipe.getDescription());
        dto.setIsVegetarian(recipe.getIsVegetarian());
        dto.setServings(recipe.getServings());
        dto.setIngredients(
                recipe.getIngredients() == null ? List.of() :
                        recipe.getIngredients().stream()
                                .map(Ingredient::getName)
                                .filter(Objects::nonNull)
                                .sorted(String.CASE_INSENSITIVE_ORDER)
                                .collect(Collectors.toList())
        );
        dto.setInstructions(recipe.getInstructions());
        return dto;
    }

    /**
     * Maps basic recipe fields from DTO onto a new entity.
     *
     * Ingredients are not set here because that requires repository lookups; use service logic.
     */
    public Recipe toEntityForCreate(RecipeDTO dto) {
        if (dto == null) {
            return null;
        }

        Recipe recipe = new Recipe();
        recipe.setName(dto.getName());
        recipe.setDescription(dto.getDescription());
        recipe.setIsVegetarian(dto.getIsVegetarian());
        recipe.setServings(dto.getServings());
        recipe.setInstructions(dto.getInstructions());
        return recipe;
    }

    /**
     * Applies updatable fields from DTO onto an existing entity.
     *
     * Ingredients are intentionally not handled here.
     */
    public void updateEntity(Recipe recipe, RecipeDTO dto) {
        if (recipe == null || dto == null) {
            return;
        }

        recipe.setName(dto.getName());
        recipe.setDescription(dto.getDescription());
        recipe.setIsVegetarian(dto.getIsVegetarian());
        recipe.setServings(dto.getServings());
        recipe.setInstructions(dto.getInstructions());
    }
}

