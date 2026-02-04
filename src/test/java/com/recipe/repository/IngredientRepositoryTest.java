package com.recipe.repository;

import com.recipe.entity.Ingredient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class IngredientRepositoryTest {

    @Autowired
    private IngredientRepository ingredientRepository;

    @Test
    void findByNameIgnoreCase_findsRegardlessOfCase() {
        String name = "Repo-Case-" + UUID.randomUUID();
        ingredientRepository.save(Ingredient.builder().name(name).build());

        var found = ingredientRepository.findByNameIgnoreCase(name.toLowerCase());

        assertTrue(found.isPresent());
        assertEquals(name, found.get().getName());
    }

    @Test
    void findByNameContainingIgnoreCase_supportsPaginationAndSorting() {
        String token = "Repo-Contains-" + UUID.randomUUID();
        String a = token + "-a";
        String b = token + "-b";

        ingredientRepository.save(Ingredient.builder().name(b).build());
        ingredientRepository.save(Ingredient.builder().name(a).build());

        var page = ingredientRepository.findByNameContainingIgnoreCase(token,
                PageRequest.of(0, 1, Sort.by("name").ascending()));

        assertEquals(1, page.getContent().size());
        assertTrue(page.getTotalElements() >= 2);
        assertEquals(a, page.getContent().get(0).getName());
    }

    @Test
    void findByNameStartingWithIgnoreCase_filtersByPrefix() {
        String prefix = "Repo-Prefix-" + UUID.randomUUID();
        String match = prefix + "-match";
        String noMatch = "Other-" + UUID.randomUUID();

        ingredientRepository.save(Ingredient.builder().name(match).build());
        ingredientRepository.save(Ingredient.builder().name(noMatch).build());

        var page = ingredientRepository.findByNameStartingWithIgnoreCase(prefix.toLowerCase(), PageRequest.of(0, 10));

        assertTrue(page.getContent().stream().anyMatch(i -> i.getName().equals(match)));
        assertTrue(page.getContent().stream().noneMatch(i -> i.getName().equals(noMatch)));
    }

    @Test
    void findFirstByNameIgnoreCase_returnsExactMatch() {
        String name = "Repo-Exact-" + UUID.randomUUID();
        ingredientRepository.save(Ingredient.builder().name(name).build());

        var found = ingredientRepository.findFirstByNameIgnoreCase(name.toUpperCase());

        assertTrue(found.isPresent());
        assertEquals(name, found.get().getName());
    }
}
