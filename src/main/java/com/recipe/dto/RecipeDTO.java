package com.recipe.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * DTO for Recipe creation and updates
 */
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecipeDTO {
    private Long id;

    @NotBlank(message = "Recipe name cannot be blank")
    private String name;

    private String description;

    @NotNull(message = "Vegetarian status must be specified")
    private Boolean isVegetarian;

    @NotNull(message = "Number of servings must be specified")
    @Positive(message = "Servings must be positive")
    private Integer servings;

    @NotNull(message = "Ingredients must be provided")
    private List<@NotBlank(message = "Ingredient name cannot be blank") String> ingredients;

    @NotBlank(message = "Instructions cannot be blank")
    private String instructions;
}
