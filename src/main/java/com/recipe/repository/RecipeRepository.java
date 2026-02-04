package com.recipe.repository;

import com.recipe.entity.Recipe;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Recipe entity
 */
@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    @Override
    @EntityGraph(attributePaths = {"ingredients"})
    List<Recipe> findAll();

    @Override
    @EntityGraph(attributePaths = {"ingredients"})
    Optional<Recipe> findById(Long id);
}
