package com.recipe.repository;

import com.recipe.entity.Ingredient;
import com.recipe.entity.Recipe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class RecipeRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RecipeRepository recipeRepository;

    private Recipe testRecipe;

    @BeforeEach
    void setUp() {
        // Use unique names to avoid clashing with application seed data.sql
        Ingredient tomato = Ingredient.builder().name("repo-test-tomato").build();
        Ingredient basil = Ingredient.builder().name("repo-test-basil").build();
        Ingredient oliveOil = Ingredient.builder().name("repo-test-olive-oil").build();

        tomato = entityManager.persist(tomato);
        basil = entityManager.persist(basil);
        oliveOil = entityManager.persist(oliveOil);

        testRecipe = new Recipe();
        testRecipe.setName("Test Recipe");
        testRecipe.setDescription("Test Description");
        testRecipe.setIsVegetarian(true);
        testRecipe.setServings(4);
        testRecipe.setIngredients(new HashSet<>(Set.of(tomato, basil, oliveOil)));
        testRecipe.setInstructions("Mix ingredients and serve");
        testRecipe.setCreatedAt(LocalDateTime.now());
        testRecipe.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void testSaveRecipe() {
        Recipe savedRecipe = recipeRepository.save(testRecipe);

        assertNotNull(savedRecipe.getId());
        assertEquals("Test Recipe", savedRecipe.getName());

        Recipe retrievedRecipe = entityManager.find(Recipe.class, savedRecipe.getId());
        assertNotNull(retrievedRecipe);
        assertEquals("Test Recipe", retrievedRecipe.getName());
        assertNotNull(retrievedRecipe.getIngredients());
        assertFalse(retrievedRecipe.getIngredients().isEmpty());
    }

    @Test
    void testFindById() {
        Recipe savedRecipe = recipeRepository.save(testRecipe);

        Recipe foundRecipe = recipeRepository.findById(savedRecipe.getId()).orElse(null);

        assertNotNull(foundRecipe);
        assertEquals("Test Recipe", foundRecipe.getName());
    }

    @Test
    void testFindAll() {
        recipeRepository.save(testRecipe);

        var allRecipes = recipeRepository.findAll();

        assertFalse(allRecipes.isEmpty());
        assertTrue(allRecipes.stream().anyMatch(r -> r.getName().equals("Test Recipe")));
    }

    @Test
    void testUpdateRecipe() {
        Recipe savedRecipe = recipeRepository.save(testRecipe);
        savedRecipe.setName("Updated Recipe");
        Recipe updatedRecipe = recipeRepository.save(savedRecipe);

        Recipe retrievedRecipe = recipeRepository.findById(updatedRecipe.getId()).orElse(null);

        assertNotNull(retrievedRecipe);
        assertEquals("Updated Recipe", retrievedRecipe.getName());
    }

    @Test
    void testDeleteRecipe() {
        Recipe savedRecipe = recipeRepository.save(testRecipe);
        Long recipeId = savedRecipe.getId();

        recipeRepository.delete(savedRecipe);

        var deletedRecipe = recipeRepository.findById(recipeId);

        assertTrue(deletedRecipe.isEmpty());
    }
}
