package com.recipe.repository;

import com.recipe.entity.Ingredient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {
    Optional<Ingredient> findByNameIgnoreCase(String name);

    Page<Ingredient> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Ingredient> findByNameStartingWithIgnoreCase(String name, Pageable pageable);

    Optional<Ingredient> findFirstByNameIgnoreCase(String name);
}
