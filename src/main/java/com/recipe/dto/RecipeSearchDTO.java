package com.recipe.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

/**
 * DTO for recipe search/filter parameters
 */
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecipeSearchDTO {
    private Boolean isVegetarian;
    private Integer servings;
    private String includeIngredients;
    private String excludeIngredients;
    private String instructionsKeyword;
}
