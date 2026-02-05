package com.recipe.service;

import com.recipe.dto.RecipeDTO;
import com.recipe.dto.RecipeSearchDTO;
import com.recipe.entity.Ingredient;
import com.recipe.entity.Recipe;
import com.recipe.exception.RecipeNotFoundException;
import com.recipe.mapper.RecipeMapper;
import com.recipe.repository.IngredientRepository;
import com.recipe.repository.RecipeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RecipeServiceTest {

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private IngredientRepository ingredientRepository;

    @Mock
    private RecipeMapper recipeMapper;

    @InjectMocks
    private RecipeService recipeService;

    private Recipe testRecipe;
    private RecipeDTO testRecipeDTO;

    @BeforeEach
    void setUp() {
        testRecipe = new Recipe();
        testRecipe.setId(1L);
        testRecipe.setName("Test Recipe");
        testRecipe.setDescription("Test Description");
        testRecipe.setIsVegetarian(true);
        testRecipe.setServings(4);
        testRecipe.setIngredients(new HashSet<>(Set.of(
                Ingredient.builder().id(10L).name("tomato").build(),
                Ingredient.builder().id(11L).name("basil").build(),
                Ingredient.builder().id(12L).name("olive oil").build()
        )));
        testRecipe.setInstructions("Mix ingredients and serve");
        testRecipe.setCreatedAt(LocalDateTime.now());
        testRecipe.setUpdatedAt(LocalDateTime.now());

        testRecipeDTO = new RecipeDTO();
        testRecipeDTO.setId(1L);
        testRecipeDTO.setName("Test Recipe");
        testRecipeDTO.setDescription("Test Description");
        testRecipeDTO.setIsVegetarian(true);
        testRecipeDTO.setServings(4);
        testRecipeDTO.setIngredients(List.of("tomato", "basil", "olive oil"));
        testRecipeDTO.setInstructions("Mix ingredients and serve");

        lenient().when(recipeMapper.toDTO(any(Recipe.class))).thenAnswer(invocation -> {
            Recipe r = invocation.getArgument(0);
            RecipeDTO dto = new RecipeDTO();
            dto.setId(r.getId());
            dto.setName(r.getName());
            dto.setDescription(r.getDescription());
            dto.setIsVegetarian(r.getIsVegetarian());
            dto.setServings(r.getServings());
            dto.setIngredients(r.getIngredients() == null ? List.of() : r.getIngredients().stream()
                    .map(Ingredient::getName)
                    .filter(Objects::nonNull)
                    .toList());
            dto.setInstructions(r.getInstructions());
            return dto;
        });
        lenient().when(recipeMapper.toEntityForCreate(any(RecipeDTO.class))).thenAnswer(invocation -> {
            RecipeDTO dto = invocation.getArgument(0);
            Recipe r = new Recipe();
            r.setName(dto.getName());
            r.setDescription(dto.getDescription());
            r.setIsVegetarian(dto.getIsVegetarian());
            r.setServings(dto.getServings());
            r.setInstructions(dto.getInstructions());
            r.setIngredients(new HashSet<>());
            return r;
        });
        lenient().doAnswer(invocation -> {
            Recipe target = invocation.getArgument(0);
            RecipeDTO dto = invocation.getArgument(1);
            target.setName(dto.getName());
            target.setDescription(dto.getDescription());
            target.setIsVegetarian(dto.getIsVegetarian());
            target.setServings(dto.getServings());
            target.setInstructions(dto.getInstructions());
            return null;
        }).when(recipeMapper).updateEntity(any(Recipe.class), any(RecipeDTO.class));

        // Default ingredient resolution: create Ingredient when not found
        lenient().when(ingredientRepository.findByNameIgnoreCase(any(String.class))).thenReturn(Optional.empty());
        lenient().when(ingredientRepository.save(any(Ingredient.class))).thenAnswer(invocation -> {
            Ingredient arg = invocation.getArgument(0);
            return Ingredient.builder().id(new Random().nextLong()).name(arg.getName()).build();
        });
    }

    @Test
    void testGetAllRecipes() {
        when(recipeRepository.findAll()).thenReturn(Arrays.asList(testRecipe));

        List<RecipeDTO> recipes = recipeService.getAllRecipes();

        assertNotNull(recipes);
        assertEquals(1, recipes.size());
        assertEquals("Test Recipe", recipes.get(0).getName());
        verify(recipeRepository, times(1)).findAll();
        verify(recipeMapper, times(1)).toDTO(any(Recipe.class));
    }

    @Test
    void testGetRecipeById_Success() {
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(testRecipe));

        RecipeDTO recipe = recipeService.getRecipeById(1L);

        assertNotNull(recipe);
        assertEquals("Test Recipe", recipe.getName());
        verify(recipeRepository, times(1)).findById(1L);
        verify(recipeMapper, times(1)).toDTO(testRecipe);
    }

    @Test
    void testGetRecipeById_NotFound() {
        when(recipeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RecipeNotFoundException.class, () -> recipeService.getRecipeById(999L));
        verify(recipeRepository, times(1)).findById(999L);
    }

    @Test
    void testCreateRecipe() {
        when(recipeRepository.save(any(Recipe.class))).thenReturn(testRecipe);

        RecipeDTO createdRecipe = recipeService.createRecipe(testRecipeDTO);

        assertNotNull(createdRecipe);
        assertEquals("Test Recipe", createdRecipe.getName());
        verify(recipeMapper, times(1)).toEntityForCreate(testRecipeDTO);
        verify(recipeRepository, times(1)).save(any(Recipe.class));
        verify(recipeMapper, times(1)).toDTO(testRecipe);
    }

    @Test
    void testUpdateRecipe_Success() {
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(testRecipe));
        when(recipeRepository.save(any(Recipe.class))).thenReturn(testRecipe);

        RecipeDTO updatedRecipe = recipeService.updateRecipe(1L, testRecipeDTO);

        assertNotNull(updatedRecipe);
        assertEquals("Test Recipe", updatedRecipe.getName());
        verify(recipeRepository, times(1)).findById(1L);
        verify(recipeMapper, times(1)).updateEntity(testRecipe, testRecipeDTO);
        verify(recipeRepository, times(1)).save(any(Recipe.class));
        verify(recipeMapper, times(1)).toDTO(testRecipe);
    }

    @Test
    void testDeleteRecipe_Success() {
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(testRecipe));

        recipeService.deleteRecipe(1L);

        verify(recipeRepository, times(1)).findById(1L);
        verify(recipeRepository, times(1)).delete(testRecipe);
    }

    @Test
    void testSearchRecipes_FilterByVegetarian() {
        Recipe nonVegetarianRecipe = new Recipe();
        nonVegetarianRecipe.setId(2L);
        nonVegetarianRecipe.setName("Meat Recipe");
        nonVegetarianRecipe.setIsVegetarian(false);
        nonVegetarianRecipe.setServings(4);
        nonVegetarianRecipe.setIngredients(new HashSet<>(Set.of(Ingredient.builder().name("beef").build(), Ingredient.builder().name("onion").build())));
        nonVegetarianRecipe.setInstructions("Cook beef");

        when(recipeRepository.findAll()).thenReturn(Arrays.asList(testRecipe, nonVegetarianRecipe));

        RecipeSearchDTO searchDTO = new RecipeSearchDTO();
        searchDTO.setIsVegetarian(true);

        List<RecipeDTO> results = recipeService.searchRecipes(searchDTO);

        assertEquals(1, results.size());
        assertTrue(results.get(0).getIsVegetarian());
        verify(recipeRepository, times(1)).findAll();
        verify(recipeMapper, times(1)).toDTO(any(Recipe.class));
    }

    @Test
    void testSearchRecipes_FilterByIngredients() {
        Recipe otherRecipe = new Recipe();
        otherRecipe.setId(2L);
        otherRecipe.setName("Other Recipe");
        otherRecipe.setIsVegetarian(true);
        otherRecipe.setServings(2);
        otherRecipe.setIngredients(new HashSet<>(Set.of(Ingredient.builder().name("salt").build(), Ingredient.builder().name("pepper").build())));
        otherRecipe.setInstructions("Mix and serve");

        when(recipeRepository.findAll()).thenReturn(Arrays.asList(testRecipe, otherRecipe));

        RecipeSearchDTO searchDTO = new RecipeSearchDTO();
        searchDTO.setIncludeIngredients("tomato");

        List<RecipeDTO> results = recipeService.searchRecipes(searchDTO);

        assertEquals(1, results.size());
        assertTrue(results.get(0).getIngredients().stream().anyMatch(i -> i.equalsIgnoreCase("tomato")));
        verify(recipeRepository, times(1)).findAll();
        verify(recipeMapper, times(1)).toDTO(any(Recipe.class));
    }

    @Test
    void testSearchRecipes_FilterByInstructions() {
        Recipe otherRecipe = new Recipe();
        otherRecipe.setId(2L);
        otherRecipe.setName("Oven Recipe");
        otherRecipe.setIsVegetarian(true);
        otherRecipe.setServings(4);
        otherRecipe.setIngredients(new HashSet<>(Set.of(Ingredient.builder().name("vegetables").build())));
        otherRecipe.setInstructions("Place in oven at 350F");

        when(recipeRepository.findAll()).thenReturn(Arrays.asList(testRecipe, otherRecipe));

        RecipeSearchDTO searchDTO = new RecipeSearchDTO();
        searchDTO.setInstructionsKeyword("oven");

        List<RecipeDTO> results = recipeService.searchRecipes(searchDTO);

        assertEquals(1, results.size());
        assertTrue(results.get(0).getInstructions().toLowerCase().contains("oven"));
        verify(recipeRepository, times(1)).findAll();
        verify(recipeMapper, times(1)).toDTO(any(Recipe.class));
    }

    @Test
    void testSearchRecipes_EmptyIngredients_DoesNotThrow_AndIsFilteredOutWhenIncludeIngredientsProvided() {
        Recipe emptyIngredientsRecipe = new Recipe();
        emptyIngredientsRecipe.setId(2L);
        emptyIngredientsRecipe.setName("Empty Ingredients");
        emptyIngredientsRecipe.setIsVegetarian(true);
        emptyIngredientsRecipe.setServings(2);
        emptyIngredientsRecipe.setIngredients(Collections.emptySet());
        emptyIngredientsRecipe.setInstructions("Some instructions");

        when(recipeRepository.findAll()).thenReturn(Arrays.asList(testRecipe, emptyIngredientsRecipe));

        RecipeSearchDTO searchDTO = new RecipeSearchDTO();
        searchDTO.setIncludeIngredients("tomato");

        List<RecipeDTO> results = recipeService.searchRecipes(searchDTO);

        assertEquals(1, results.size());
        assertEquals("Test Recipe", results.get(0).getName());
        verify(recipeRepository, times(1)).findAll();
        verify(recipeMapper, times(1)).toDTO(any(Recipe.class));
    }

    @Test
    void testSearchRecipes_NullInstructions_DoesNotThrow_AndIsFilteredOutWhenKeywordProvided() {
        Recipe nullInstructionsRecipe = new Recipe();
        nullInstructionsRecipe.setId(2L);
        nullInstructionsRecipe.setName("Null Instructions");
        nullInstructionsRecipe.setIsVegetarian(true);
        nullInstructionsRecipe.setServings(2);
        nullInstructionsRecipe.setIngredients(new HashSet<>(Set.of(Ingredient.builder().name("tomato").build())));
        nullInstructionsRecipe.setInstructions(null);

        when(recipeRepository.findAll()).thenReturn(Arrays.asList(testRecipe, nullInstructionsRecipe));

        RecipeSearchDTO searchDTO = new RecipeSearchDTO();
        searchDTO.setInstructionsKeyword("mix");

        List<RecipeDTO> results = recipeService.searchRecipes(searchDTO);

        assertEquals(1, results.size());
        assertEquals("Test Recipe", results.get(0).getName());
        verify(recipeRepository, times(1)).findAll();
        verify(recipeMapper, times(1)).toDTO(any(Recipe.class));
    }
}
