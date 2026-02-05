# Recipe Management Service

A standalone REST API application for managing recipes with advanced filtering capabilities. Built with Spring Boot, Spring Data JPA, and H2 in-memory database.

## Quick Start

### 1) Prerequisites
- Java **21+**
- Maven **3.8+**

### 2) Build
```bash
mvn clean test
```

### 3) Run
Default port is `8081`.

**Option A: Dev mode (recommended for local testing)**
- Starts without a real JWT issuer.
- Use a fixed token for protected endpoints: `Authorization: Bearer dev-token`

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

**Option B: Default mode (OAuth2 Resource Server / JWT)**
- Requires configuring your IdP (issuer or JWKS).

```bash
mvn spring-boot:run
```

### 4) URLs
- API base: `http://localhost:8081`
- Swagger UI: `http://localhost:8081/swagger-ui.html`
- Actuator: `http://localhost:8081/actuator`
- H2 console: `http://localhost:8081/h2-console`

---

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

### Observability (Actuator)
- Actuator base path: `/actuator`
- Exposed endpoints (current config):
  - `GET /actuator/health`
  - `GET /actuator/info`

Notes:
- `/actuator/info` is populated from `info.*` properties in `src/main/resources/application.properties`.

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
├── docs/
│   ├── RecipeManagementService.postman_collection.json
│   └── RecipeManagementService.postman_environment.json
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

### Application Configuration
Main config file:
- `src/main/resources/application.properties`

Key properties:
- `server.port=8081`
- H2:
  - `spring.datasource.url=jdbc:h2:mem:testdb`
  - `spring.h2.console.enabled=true`
- Actuator:
  - `management.endpoints.web.exposure.include=health,info`

### Run Profiles
This project uses profiles to make local development easier.

- `dev` profile:
  - does **not** require a real OAuth2 JWT issuer configuration
  - accepts a fixed token `dev-token`

Run with `dev`:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Default profile:
- OAuth2 Resource Server (JWT) is enabled
- you must configure an issuer or JWKS URL (see Security section)

### Database / Sample Data
- Schema: `src/main/resources/schema.sql`
- Sample data: `src/main/resources/data.sql`

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

### Dev Token (local development)
If you run with `-Dspring-boot.run.profiles=dev`, the API accepts:

- Header: `Authorization: Bearer dev-token`

This is meant only for local development.

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

## Postman

A Postman collection and environment are included under `docs/`:
- `docs/RecipeManagementService.postman_collection.json`
- `docs/RecipeManagementService.postman_environment.json`

Import steps:
1) Postman → **Import** → select the collection JSON
2) (Optional) Import the environment JSON and select it
3) Set environment variable `token`:
   - dev profile: `dev-token`
   - default profile: a real JWT access token

Variables:
- `{{baseUrl}}` (default `http://localhost:8081`)
- `{{token}}`
- `{{recipeId}}`
- `{{ingredientId}}`

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

## Troubleshooting

### /actuator/info is empty
`/actuator/info` returns `{}` unless you define `info.*` properties.
This project already provides `info.app.*` in `src/main/resources/application.properties`.

### 401 Unauthorized on /api/**
All `/api/**` endpoints are protected.
- Use `-Dspring-boot.run.profiles=dev` and send `Authorization: Bearer dev-token`, or
- Configure your OAuth2 JWT issuer/JWKS and send a real token.

### H2 Console doesn’t open
- Ensure the app is running and you’re using: `http://localhost:8081/h2-console`
- JDBC URL must match config: `jdbc:h2:mem:testdb`

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

**Last Updated**: February 5, 2026
