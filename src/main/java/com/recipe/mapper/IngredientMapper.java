package com.recipe.mapper;

import com.recipe.dto.IngredientDTO;
import com.recipe.entity.Ingredient;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between {@link Ingredient} entities and {@link IngredientDTO} DTOs.
 */
@Component
public class IngredientMapper {

    public IngredientDTO toDTO(Ingredient ingredient) {
        if (ingredient == null) {
            return null;
        }
        IngredientDTO dto = new IngredientDTO();
        dto.setId(ingredient.getId());
        dto.setName(ingredient.getName());
        return dto;
    }

    /**
     * Creates a new Ingredient entity for create operations.
     *
     * Note: name normalization/validation is handled by the service layer.
     */
    public Ingredient toEntityForCreate(IngredientDTO dto) {
        if (dto == null) {
            return null;
        }

        Ingredient ingredient = new Ingredient();
        ingredient.setName(dto.getName());
        return ingredient;
    }

    public void updateEntity(Ingredient ingredient, IngredientDTO dto) {
        if (ingredient == null || dto == null) {
            return;
        }

        ingredient.setName(dto.getName());
    }
}

