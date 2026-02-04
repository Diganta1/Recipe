# Recipe Management Service

A standalone REST API application for managing recipes with advanced filtering capabilities. Built with Spring Boot, Spring Data JPA, and H2 in-memory database.

## Overview

This application allows users to:
- **Add** new recipes to the system
- **Update** existing recipes
- **Remove** recipes from the system
- **Fetch** individual or all recipes
- **Filter** recipes based on multiple criteria:
  - Vegetarian status
  - Number of servings
  - Specific ingredients (include or exclude)
  - Text search within instructions
- **Manage Ingredients** as first-class entities

## Features

### Core Functionality
- RESTful API endpoints for CRUD operations
- Advanced recipe filtering with multiple criteria
- Ingredient management (many-to-many with recipes)
- Input validation and error handling
- Comprehensive logging
- H2 in-memory database with sample data

### API Documentation
- OpenAPI/Swagger UI documentation at `/swagger-ui.html`
- Interactive API testing and exploration
- Auto-generated documentation based on annotations

### Production-Ready
- Unit tests with Mockito
- Integration tests with MockMvc
- Repository tests with DataJpaTest
- Global exception handler for error management
- Proper HTTP status codes and error responses

## Project Structure

```
recipe-management-service/
├── src/
│   ├── main/
│   │   ├── java/com/recipe/
│   │   │   ├── RecipeManagementApplication.java    (Entry point)
│   │   │   ├── controller/
│   │   │   │   ├── RecipeController.java            (Recipe REST endpoints)
│   │   │   │   └── IngredientController.java        (Ingredient REST endpoints)
│   │   │   ├── service/
│   │   │   │   ├── RecipeService.java               (Recipe business logic)
│   │   │   │   └── IngredientService.java           (Ingredient business logic)
│   │   │   ├── repository/
│   │   │   │   ├── RecipeRepository.java            (Data access)
│   │   │   │   └── IngredientRepository.java        (Data access)
│   │   │   ├── entity/
│   │   │   │   ├── Recipe.java                      (JPA entity)
│   │   │   │   └── Ingredient.java                  (JPA entity)
│   │   │   ├── dto/
│   │   │   │   ├── RecipeDTO.java                   (Transfer object)
│   │   │   │   ├── RecipeSearchDTO.java             (Search filters)
│   │   │   │   ├── IngredientDTO.java               (Ingredient transfer object)
│   │   │   │   ├── IngredientSearchMode.java        (CONTAINS/STARTS_WITH/EXACT)
│   │   │   │   └── PagedResponse.java               (Paged list responses)
│   │   │   └── exception/
│   │   │       ├── RecipeNotFoundException.java
│   │   │       ├── IngredientNotFoundException.java
│   │   │       ├── ErrorResponse.java
│   │   │       └── GlobalExceptionHandler.java
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── schema.sql
│   │       └── data.sql
│   └── test/
│       └── java/com/recipe/
│           ├── service/
│           │   ├── RecipeServiceTest.java
│           │   └── IngredientServiceTest.java
│           ├── controller/
│           │   ├── RecipeControllerIntegrationTest.java
│           │   └── IngredientControllerIntegrationTest.java
│           └── repository/
│               ├── RecipeRepositoryTest.java
│               └── IngredientRepositoryTest.java
├── pom.xml
└── README.md
```

## Technical Stack

- **Java**: 21
- **Framework**: Spring Boot 3.3.x
- **Database**: H2 (in-memory)
- **ORM**: Spring Data JPA
- **API Documentation**: OpenAPI 3.0 / Springdoc
- **Testing**: JUnit 5, Mockito, MockMvc
- **Build Tool**: Maven
- **Logging**: SLF4J with Logback

## Installation & Setup

### Prerequisites
- Java 21 or higher
- Maven 3.8.0 or higher

### Steps

1. **Build the project**
   ```bash
   mvn clean install
   ```

2. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

   By default (as configured in `src/main/resources/application.properties`) the application starts on:
   - `http://localhost:8081`

3. **Access Swagger UI**
   - `http://localhost:8081/swagger-ui.html`

4. **Access H2 Console**
   - `http://localhost:8081/h2-console`
   - JDBC URL: `jdbc:h2:mem:testdb`
   - Username: `sa`
   - Password: (leave empty)

## Data Model

### Recipes ↔ Ingredients
Ingredients are stored in a separate `ingredients` table.
A recipe can have many ingredients and an ingredient can belong to many recipes.
This is modeled via a join table: `recipe_ingredients`.

## API Endpoints

### Recipes

#### Get All Recipes
```http
GET /api/v1/recipes
```

#### Get Recipe by ID
```http
GET /api/v1/recipes/{id}
```

#### Create Recipe
```http
POST /api/v1/recipes
Content-Type: application/json

{
  "name": "Vegetable Stir Fry",
  "description": "A quick and healthy vegetable stir fry",
  "isVegetarian": true,
  "servings": 4,
  "ingredients": ["broccoli", "carrots", "bell peppers", "garlic", "soy sauce", "oil"],
  "instructions": "Heat oil in wok. Add garlic and ginger. Stir fry vegetables. Add soy sauce. Serve over rice."
}
```

#### Update Recipe
```http
PUT /api/v1/recipes/{id}
Content-Type: application/json

{
  "name": "Updated Recipe Name",
  "description": "Updated description",
  "isVegetarian": true,
  "servings": 4,
  "ingredients": ["updated", "ingredient", "list"],
  "instructions": "updated instructions"
}
```

#### Delete Recipe
```http
DELETE /api/v1/recipes/{id}
```

#### Search Recipes
```http
POST /api/v1/recipes/search
Content-Type: application/json

{
  "isVegetarian": true,
  "servings": 4,
  "includeIngredients": "potatoes,onion",
  "excludeIngredients": "salmon",
  "instructionsKeyword": "oven"
}
```

> Note: `includeIngredients` and `excludeIngredients` are comma-separated strings.

### Ingredients

#### List Ingredients (pagination + sorting + search)
```http
GET /api/v1/ingredients?q=gar&mode=STARTS_WITH&page=0&size=20&sort=name,asc
```

Query params:
- `q` (optional): term to search for
- `mode` (optional): `CONTAINS` (default), `STARTS_WITH`, `EXACT`
- `page` (default `0`): 0-based page index
- `size` (default `20`, max `200`): page size
- `sort` (default `name,asc`): `id|name,(asc|desc)`

Response is a `PagedResponse<IngredientDTO>`:
- `content`: list of ingredients
- `page`, `size`, `totalElements`, `totalPages`, `first`, `last`, `sort`

#### Get Ingredient by ID
```http
GET /api/v1/ingredients/{id}
```

#### Create Ingredient
```http
POST /api/v1/ingredients
Content-Type: application/json

{
  "name": "smoked paprika"
}
```

#### Update Ingredient
```http
PUT /api/v1/ingredients/{id}
Content-Type: application/json

{
  "name": "paprika"
}
```

#### Delete Ingredient
```http
DELETE /api/v1/ingredients/{id}
```

## Security (OAuth2 / JWT)

This API is secured as an **OAuth2 Resource Server** using **JWT bearer tokens**.

### Public vs Protected Endpoints

**Public (no token required):**
- Swagger UI: `/swagger-ui.html` (may redirect to `/swagger-ui/index.html`)
- OpenAPI docs: `/v3/api-docs/**`
- Actuator: `/actuator/**`
- H2 console (dev): `/h2-console/**`

**Protected (token required):**
- All business APIs under `/api/**`
  - `/api/v1/recipes/**`
  - `/api/v1/ingredients/**`

If you call a protected endpoint without a token you will get **401 Unauthorized**.

### Configure your Identity Provider

You must configure **one** of the following properties (recommended via environment variables or profile-specific config):

- Option A (recommended):
  - `spring.security.oauth2.resourceserver.jwt.issuer-uri=<your-issuer>`

- Option B:
  - `spring.security.oauth2.resourceserver.jwt.jwk-set-uri=<your-jwks-url>`

Example (Keycloak):
- `issuer-uri=http://localhost:8080/realms/recipe`

### Call an API with a Bearer token

1) Obtain an access token (JWT) from your Identity Provider.

2) Call the API by sending the `Authorization` header:

```bash
curl -H "Authorization: Bearer <ACCESS_TOKEN>" \
  http://localhost:8081/api/v1/recipes
```

### Swagger UI

Open Swagger UI and click **Authorize**, then paste:

- `Bearer <ACCESS_TOKEN>`

After authorizing, try the protected endpoints directly in Swagger.

## Testing

### Run All Tests
```bash
mvn test
```

### Run Tests with Coverage
```bash
mvn clean test jacoco:report
```

Coverage report:
- `target/site/jacoco/index.html`

## Sample Data

Data is loaded from `src/main/resources/data.sql` during application startup.
The SQL is designed to be idempotent (safe to re-run) and respects the many-to-many relationship.

## Developer Notes

### Mapping (Entity ↔ DTO)
To keep controllers/services small and testable, mapping is handled by dedicated mapper components:
- `com.recipe.mapper.RecipeMapper`
- `com.recipe.mapper.IngredientMapper`

Guidelines:
- Prefer calling mappers from the service layer for entity ↔ DTO conversion.
- Keep repository lookups (e.g., resolving ingredient names into `Ingredient` entities) in the service layer.

### Normalization Helpers
User-provided text values (ingredient names, filter tokens, instruction keywords) are normalized using:
- `com.recipe.util.NameNormalizer`

Helpers:
- `trimToNullSafe(value)`: trims input (null-safe)
- `trimToLowerSafe(value)`: trims + lowercases for case-insensitive matching

---

**Last Updated**: February 2, 2026
