package com.recipe.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * DTO for Ingredient API contracts.
 */
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IngredientDTO {

    private Long id;

    @NotBlank(message = "Ingredient name cannot be blank")
    private String name;
}

