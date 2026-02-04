package com.recipe.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recipe.dto.RecipeDTO;
import com.recipe.dto.RecipeSearchDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class RecipeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private RecipeDTO testRecipeDTO;

    @BeforeEach
    void setUp() {
        testRecipeDTO = new RecipeDTO();
        testRecipeDTO.setName("Integration Test Recipe " + java.util.UUID.randomUUID());
        testRecipeDTO.setDescription("Test Description");
        testRecipeDTO.setIsVegetarian(true);
        testRecipeDTO.setServings(4);
        testRecipeDTO.setIngredients(List.of("test-tomato-" + java.util.UUID.randomUUID(), "test-basil-" + java.util.UUID.randomUUID(), "test-olive-oil-" + java.util.UUID.randomUUID()));
        testRecipeDTO.setInstructions("Mix ingredients and serve");
    }

    @Test
    void testCreateRecipe() throws Exception {
        mockMvc.perform(post("/api/v1/recipes")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRecipeDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is(testRecipeDTO.getName())))
                .andExpect(jsonPath("$.isVegetarian", is(true)))
                .andExpect(jsonPath("$.servings", is(4)))
                .andExpect(jsonPath("$.ingredients", hasSize(3)));
    }

    @Test
    void testGetAllRecipes() throws Exception {
        // Create a recipe first
        mockMvc.perform(post("/api/v1/recipes")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRecipeDTO)));

        mockMvc.perform(get("/api/v1/recipes").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].name", notNullValue()));
    }

    @Test
    void testGetRecipeById() throws Exception {
        // Create a recipe first
        var response = mockMvc.perform(post("/api/v1/recipes")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRecipeDTO)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseBody = response.getResponse().getContentAsString();
        RecipeDTO createdRecipe = objectMapper.readValue(responseBody, RecipeDTO.class);

        mockMvc.perform(get("/api/v1/recipes/" + createdRecipe.getId()).with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(testRecipeDTO.getName())));
    }

    @Test
    void testUpdateRecipe() throws Exception {
        // Create a recipe first
        var response = mockMvc.perform(post("/api/v1/recipes")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRecipeDTO)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseBody = response.getResponse().getContentAsString();
        RecipeDTO createdRecipe = objectMapper.readValue(responseBody, RecipeDTO.class);

        testRecipeDTO.setName("Updated Recipe");
        testRecipeDTO.setIngredients(List.of("tomato", "salt"));

        mockMvc.perform(put("/api/v1/recipes/" + createdRecipe.getId())
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRecipeDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Recipe")))
                .andExpect(jsonPath("$.ingredients", hasSize(2)));
    }

    @Test
    void testDeleteRecipe() throws Exception {
        // Create a recipe first
        var response = mockMvc.perform(post("/api/v1/recipes")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRecipeDTO)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseBody = response.getResponse().getContentAsString();
        RecipeDTO createdRecipe = objectMapper.readValue(responseBody, RecipeDTO.class);

        mockMvc.perform(delete("/api/v1/recipes/" + createdRecipe.getId()).with(jwt()))
                .andExpect(status().isNoContent());
    }

    @Test
    void testSearchRecipes() throws Exception {
        // Create a vegetarian recipe
        mockMvc.perform(post("/api/v1/recipes")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRecipeDTO)));

        RecipeSearchDTO searchDTO = new RecipeSearchDTO();
        searchDTO.setIsVegetarian(true);

        mockMvc.perform(post("/api/v1/recipes/search")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(searchDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].isVegetarian", is(true)));
    }

    @Test
    void testGetRecipeNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/recipes/999999").with(jwt()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Recipe Not Found")));
    }

    @Test
    void testCreateRecipeValidationError() throws Exception {
        RecipeDTO invalidRecipe = new RecipeDTO();
        invalidRecipe.setName(""); // Empty name
        invalidRecipe.setServings(-1); // Invalid servings

        mockMvc.perform(post("/api/v1/recipes")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRecipe)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Validation Error")));
    }
}
