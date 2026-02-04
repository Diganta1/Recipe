package com.recipe.service;

import com.recipe.dto.IngredientDTO;
import com.recipe.dto.IngredientSearchMode;
import com.recipe.dto.PagedResponse;
import com.recipe.entity.Ingredient;
import com.recipe.exception.IngredientNotFoundException;
import com.recipe.mapper.IngredientMapper;
import com.recipe.repository.IngredientRepository;
import com.recipe.util.NameNormalizer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for Transaction management
 */
@Service
@AllArgsConstructor
@Slf4j
public class IngredientService {


    private final IngredientRepository ingredientRepository;

    private final IngredientMapper ingredientMapper;

    /**
     * Fetch all ingredients (non-paginated).
     */
    @Transactional(readOnly = true)
    public List<IngredientDTO> getAllIngredients() {
        log.info("Fetching all ingredients");
        return ingredientRepository.findAll().stream()
                .map(ingredientMapper::toDTO)
                .toList();
    }

    /**
     * Retrieve one ingredient by id.
     *
     * @throws IngredientNotFoundException when the id does not exist
     */
    @Transactional(readOnly = true)
    public IngredientDTO getIngredientById(Long id) {
        log.info("Fetching ingredient with id: {}", id);

        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new IngredientNotFoundException("Ingredient not found with id: " + id));

        return ingredientMapper.toDTO(ingredient);
    }

    /**
     * Paginated list/search endpoint.
     */
    @Transactional(readOnly = true)
    public PagedResponse<IngredientDTO> searchIngredients(String q, IngredientSearchMode mode, Pageable pageable) {
        IngredientSearchMode effectiveMode = mode == null ? IngredientSearchMode.CONTAINS : mode;
        log.info("Searching ingredients with query: {}, mode: {}, pageable: {}", q, effectiveMode, pageable);

        Page<Ingredient> page;

        if (q == null || q.trim().isEmpty()) {
            page = ingredientRepository.findAll(pageable);
        } else {
            String token = q.trim();

            page = switch (effectiveMode) {
                case STARTS_WITH -> ingredientRepository.findByNameStartingWithIgnoreCase(token, pageable);
                case EXACT -> {
                    Optional<Ingredient> found = ingredientRepository.findFirstByNameIgnoreCase(token);
                    yield found
                            .map(ingredient -> (Page<Ingredient>) new PageImpl<>(List.of(ingredient), pageable, 1))
                            .orElseGet(() -> new PageImpl<>(List.of(), pageable, 0));
                }
                case CONTAINS -> ingredientRepository.findByNameContainingIgnoreCase(token, pageable);
            };
        }

        List<IngredientDTO> content = page.getContent().stream().map(ingredientMapper::toDTO).toList();
        return PagedResponse.<IngredientDTO>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .sort(pageable.getSort().toString())
                .build();
    }

    /**
     * Create a new ingredient.
     *
     * <p>Name is trimmed and blank names become {@code null} via {@link NameNormalizer}.
     */
    @Transactional
    public IngredientDTO createIngredient(IngredientDTO dto) {
        log.info("Creating ingredient: {}", dto.getName());

        Ingredient ingredient = ingredientMapper.toEntityForCreate(dto);
        ingredient.setName(NameNormalizer.trimToNullSafe(dto.getName()));

        Ingredient saved = ingredientRepository.save(ingredient);
        return ingredientMapper.toDTO(saved);
    }

    /**
     * Update an existing ingredient.
     *
     * @throws IngredientNotFoundException when the ingredient does not exist
     */
    @Transactional
    public IngredientDTO updateIngredient(Long id, IngredientDTO dto) {
        log.info("Updating ingredient with id: {}", id);

        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new IngredientNotFoundException("Ingredient not found with id: " + id));

        ingredient.setName(NameNormalizer.trimToNullSafe(dto.getName()));

        Ingredient saved = ingredientRepository.save(ingredient);
        return ingredientMapper.toDTO(saved);
    }

    /**
     * Delete an ingredient by id.
     *
     * @throws IngredientNotFoundException when the ingredient does not exist
     */
    @Transactional
    public void deleteIngredient(Long id) {
        log.info("Deleting ingredient with id: {}", id);

        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new IngredientNotFoundException("Ingredient not found with id: " + id));

        ingredientRepository.delete(ingredient);
    }
}
