package com.recipe.controller;

import com.recipe.dto.IngredientDTO;
import com.recipe.dto.IngredientSearchMode;
import com.recipe.dto.PagedResponse;
import com.recipe.service.IngredientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Ingredient management
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/ingredients")
@AllArgsConstructor
@Tag(name = "Ingredient Management", description = "API endpoints for managing ingredients")
public class IngredientController {

    private final IngredientService ingredientService;

    @GetMapping
    @Operation(
            summary = "List ingredients",
            description = "Retrieve ingredients with optional search + pagination + sorting. " +
                    "Search modes: CONTAINS (default), STARTS_WITH, EXACT."
    )
    public ResponseEntity<PagedResponse<IngredientDTO>> listIngredients(
            @RequestParam(value = "q", required = false)
            @Parameter(description = "Search term for ingredient name")
            String q,

            @RequestParam(value = "mode", required = false, defaultValue = "CONTAINS")
            @Parameter(description = "Search mode: CONTAINS | STARTS_WITH | EXACT")
            IngredientSearchMode mode,

            @RequestParam(value = "page", required = false, defaultValue = "0")
            @Parameter(description = "0-based page index")
            int page,

            @RequestParam(value = "size", required = false, defaultValue = "20")
            @Parameter(description = "Page size")
            int size,

            @RequestParam(value = "sort", required = false, defaultValue = "name,asc")
            @Parameter(description = "Sort in the format 'field,asc|desc'. Supported fields: id,name")
            String sort
    ) {
        Sort springSort = parseSort(sort);
        Pageable pageable = PageRequest.of(Math.max(page, 0), clampSize(size), springSort);

        log.info("GET request: /api/v1/ingredients?q={}, mode={}, page={}, size={}, sort={}", q, mode, page, size, sort);
        return ResponseEntity.ok(ingredientService.searchIngredients(q, mode, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get ingredient by ID", description = "Retrieve a specific ingredient by its ID")
    public ResponseEntity<IngredientDTO> getIngredientById(@PathVariable Long id) {
        log.info("GET request: /api/v1/ingredients/{}", id);
        return ResponseEntity.ok(ingredientService.getIngredientById(id));
    }

    @PostMapping
    @Operation(summary = "Create ingredient", description = "Create a new ingredient")
    public ResponseEntity<IngredientDTO> createIngredient(@Valid @RequestBody IngredientDTO dto) {
        log.info("POST request: /api/v1/ingredients - Creating ingredient: {}", dto.getName());
        IngredientDTO created = ingredientService.createIngredient(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update ingredient", description = "Update an existing ingredient")
    public ResponseEntity<IngredientDTO> updateIngredient(
            @PathVariable Long id,
            @Valid @RequestBody IngredientDTO dto
    ) {
        log.info("PUT request: /api/v1/ingredients/{} - Updating ingredient", id);
        return ResponseEntity.ok(ingredientService.updateIngredient(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete ingredient", description = "Delete an ingredient")
    public ResponseEntity<Void> deleteIngredient(@PathVariable Long id) {
        log.info("DELETE request: /api/v1/ingredients/{}", id);
        ingredientService.deleteIngredient(id);
        return ResponseEntity.noContent().build();
    }

    private int clampSize(int size) {
        // prevent unbounded queries
        if (size <= 0) {
            return 20;
        }
        return Math.min(size, 200);
    }

    private Sort parseSort(String sort) {
        if (sort == null || sort.trim().isEmpty()) {
            return Sort.by(Sort.Direction.ASC, "name");
        }

        String[] parts = sort.split(",");
        String property = parts[0].trim();

        if (!property.equals("id") && !property.equals("name")) {
            property = "name";
        }

        Sort.Direction direction = Sort.Direction.ASC;
        if (parts.length > 1) {
            String dir = parts[1].trim().toLowerCase();
            if (dir.equals("desc")) {
                direction = Sort.Direction.DESC;
            }
        }

        return Sort.by(direction, property);
    }
}
