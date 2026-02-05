package com.recipe.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recipe.dto.IngredientDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class IngredientControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createAndGetIngredient_roundTrip() throws Exception {
        IngredientDTO dto = new IngredientDTO(null, "it-salt-" + UUID.randomUUID());

        var createResponse = mockMvc.perform(post("/api/v1/ingredients")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is(dto.getName())))
                .andReturn();

        IngredientDTO created = objectMapper.readValue(createResponse.getResponse().getContentAsString(), IngredientDTO.class);

        mockMvc.perform(get("/api/v1/ingredients/" + created.getId()).with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(dto.getName())));
    }

    @Test
    void listIngredients_supportsPagingAndSorting() throws Exception {
        String a = "it-a-" + UUID.randomUUID();
        String b = "it-b-" + UUID.randomUUID();

        mockMvc.perform(post("/api/v1/ingredients")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new IngredientDTO(null, b))));
        mockMvc.perform(post("/api/v1/ingredients")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new IngredientDTO(null, a))));

        boolean foundA = false;
        boolean foundB = false;

        int page = 0;
        int maxPagesToScan = 10;

        while (page < maxPagesToScan && !(foundA && foundB)) {
            var response = mockMvc.perform(get("/api/v1/ingredients")
                            .with(jwt())
                            .param("page", String.valueOf(page))
                            .param("size", "20")
                            .param("sort", "name,asc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", notNullValue()))
                    .andReturn();

            JsonNode root = objectMapper.readTree(response.getResponse().getContentAsString());
            JsonNode content = root.get("content");
            if (content != null && content.isArray()) {
                for (JsonNode item : content) {
                    String name = item.get("name").asText();
                    if (a.equals(name)) foundA = true;
                    if (b.equals(name)) foundB = true;
                }
            }

            boolean last = root.path("last").asBoolean(false);
            if (last) {
                break;
            }
            page++;
        }

        assertTrue(foundA && foundB, "Expected to find both created ingredients in paged results");
    }

    @Test
    void listIngredients_supportsSearchContainsStartsWithAndExact() throws Exception {
        String prefix = "it-find-" + UUID.randomUUID();
        String exact = prefix + "-exact";
        String other = prefix + "-other";

        mockMvc.perform(post("/api/v1/ingredients")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new IngredientDTO(null, exact))));
        mockMvc.perform(post("/api/v1/ingredients")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new IngredientDTO(null, other))));

        // CONTAINS
        mockMvc.perform(get("/api/v1/ingredients")
                        .with(jwt())
                        .param("q", prefix)
                        .param("mode", "CONTAINS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].name", hasItems(exact, other)));

        // STARTS_WITH (still both)
        mockMvc.perform(get("/api/v1/ingredients")
                        .with(jwt())
                        .param("q", prefix)
                        .param("mode", "STARTS_WITH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].name", hasItems(exact, other)));

        // EXACT (only one)
        mockMvc.perform(get("/api/v1/ingredients")
                        .with(jwt())
                        .param("q", exact)
                        .param("mode", "EXACT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", is(exact)));
    }

    @Test
    void getIngredientNotFound_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/ingredients/999999").with(jwt()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Ingredient Not Found")));
    }

    @Test
    void createIngredientValidationError_returns400() throws Exception {
        IngredientDTO invalid = new IngredientDTO(null, "");

        mockMvc.perform(post("/api/v1/ingredients")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Validation Error")));
    }
}
