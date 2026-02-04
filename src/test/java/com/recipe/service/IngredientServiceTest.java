package com.recipe.service;

import com.recipe.dto.IngredientDTO;
import com.recipe.dto.IngredientSearchMode;
import com.recipe.dto.PagedResponse;
import com.recipe.entity.Ingredient;
import com.recipe.exception.IngredientNotFoundException;
import com.recipe.mapper.IngredientMapper;
import com.recipe.repository.IngredientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IngredientServiceTest {

    @Mock
    private IngredientRepository ingredientRepository;

    @Mock
    private IngredientMapper ingredientMapper;

    @InjectMocks
    private IngredientService ingredientService;

    private Ingredient ingredient;

    @BeforeEach
    void setUp() {
        ingredient = Ingredient.builder().id(1L).name("salt").build();

        // mapper stubs
        lenient().when(ingredientMapper.toDTO(any(Ingredient.class))).thenAnswer(invocation -> {
            Ingredient i = invocation.getArgument(0);
            return new IngredientDTO(i.getId(), i.getName());
        });
        lenient().when(ingredientMapper.toEntityForCreate(any(IngredientDTO.class))).thenAnswer(invocation -> {
            IngredientDTO dto = invocation.getArgument(0);
            Ingredient i = new Ingredient();
            i.setName(dto.getName());
            return i;
        });
    }

    @Test
    void getAllIngredients_returnsList() {
        when(ingredientRepository.findAll()).thenReturn(List.of(ingredient));

        List<IngredientDTO> result = ingredientService.getAllIngredients();

        assertEquals(1, result.size());
        assertEquals("salt", result.get(0).getName());
        verify(ingredientRepository, times(1)).findAll();
        verify(ingredientMapper, times(1)).toDTO(any(Ingredient.class));
    }

    @Test
    void getIngredientById_whenFound_returnsDto() {
        when(ingredientRepository.findById(1L)).thenReturn(Optional.of(ingredient));

        IngredientDTO dto = ingredientService.getIngredientById(1L);

        assertEquals(1L, dto.getId());
        assertEquals("salt", dto.getName());
        verify(ingredientMapper, times(1)).toDTO(ingredient);
    }

    @Test
    void getIngredientById_whenMissing_throwsNotFound() {
        when(ingredientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IngredientNotFoundException.class, () -> ingredientService.getIngredientById(99L));
    }

    @Test
    void searchIngredients_contains_usesRepositoryMethod() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name").ascending());
        when(ingredientRepository.findByNameContainingIgnoreCase(eq("salt"), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(ingredient), pageable, 1));

        PagedResponse<IngredientDTO> page = ingredientService.searchIngredients("salt", IngredientSearchMode.CONTAINS, pageable);

        assertEquals(1, page.getContent().size());
        assertEquals("salt", page.getContent().get(0).getName());
        verify(ingredientRepository).findByNameContainingIgnoreCase(eq("salt"), eq(pageable));
        verify(ingredientMapper, times(1)).toDTO(any(Ingredient.class));
    }

    @Test
    void searchIngredients_startsWith_usesRepositoryMethod() {
        Pageable pageable = PageRequest.of(0, 10);
        when(ingredientRepository.findByNameStartingWithIgnoreCase(eq("sa"), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(ingredient), pageable, 1));

        PagedResponse<IngredientDTO> page = ingredientService.searchIngredients("sa", IngredientSearchMode.STARTS_WITH, pageable);

        assertEquals(1, page.getContent().size());
        verify(ingredientRepository).findByNameStartingWithIgnoreCase(eq("sa"), eq(pageable));
        verify(ingredientMapper, times(1)).toDTO(any(Ingredient.class));
    }

    @Test
    void searchIngredients_exact_returnsSingleWhenFound() {
        Pageable pageable = PageRequest.of(0, 10);
        when(ingredientRepository.findFirstByNameIgnoreCase("salt")).thenReturn(Optional.of(ingredient));

        PagedResponse<IngredientDTO> page = ingredientService.searchIngredients("salt", IngredientSearchMode.EXACT, pageable);

        assertEquals(1, page.getContent().size());
        assertEquals("salt", page.getContent().get(0).getName());
        verify(ingredientRepository).findFirstByNameIgnoreCase("salt");
        verify(ingredientMapper, times(1)).toDTO(any(Ingredient.class));
    }

    @Test
    void searchIngredients_exact_returnsEmptyWhenNotFound() {
        Pageable pageable = PageRequest.of(0, 10);
        when(ingredientRepository.findFirstByNameIgnoreCase("missing")).thenReturn(Optional.empty());

        PagedResponse<IngredientDTO> page = ingredientService.searchIngredients("missing", IngredientSearchMode.EXACT, pageable);

        assertTrue(page.getContent().isEmpty());
        verify(ingredientRepository).findFirstByNameIgnoreCase("missing");
    }

    @Test
    void createIngredient_trimsName() {
        when(ingredientRepository.save(any(Ingredient.class))).thenAnswer(invocation -> {
            Ingredient arg = invocation.getArgument(0);
            return Ingredient.builder().id(10L).name(arg.getName()).build();
        });

        IngredientDTO created = ingredientService.createIngredient(new IngredientDTO(null, "  sugar  "));

        assertEquals(10L, created.getId());
        assertEquals("sugar", created.getName());
        verify(ingredientMapper, times(1)).toEntityForCreate(any(IngredientDTO.class));
        verify(ingredientMapper, times(1)).toDTO(any(Ingredient.class));
    }

    @Test
    void updateIngredient_whenFound_updatesName() {
        when(ingredientRepository.findById(1L)).thenReturn(Optional.of(ingredient));
        when(ingredientRepository.save(any(Ingredient.class))).thenAnswer(invocation -> invocation.getArgument(0));

        IngredientDTO updated = ingredientService.updateIngredient(1L, new IngredientDTO(null, "  kosher salt "));

        assertEquals("kosher salt", updated.getName());
        verify(ingredientMapper, times(1)).toDTO(any(Ingredient.class));
    }

    @Test
    void deleteIngredient_whenFound_deletes() {
        when(ingredientRepository.findById(1L)).thenReturn(Optional.of(ingredient));

        ingredientService.deleteIngredient(1L);

        verify(ingredientRepository).delete(ingredient);
    }
}
