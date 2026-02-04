MERGE INTO recipes (name, description, is_vegetarian, servings, instructions, created_at, updated_at)
KEY(name)
VALUES
('Vegetable Stir Fry', 'A quick and healthy vegetable stir fry', true, 4, 'Heat oil in wok. Add garlic and ginger. Stir fry vegetables. Add soy sauce. Serve over rice.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Grilled Salmon', 'Delicious grilled salmon with lemon', false, 2, 'Season salmon. Grill for 12-15 minutes. Serve with lemon wedges.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Pasta Carbonara', 'Classic Italian pasta dish', false, 4, 'Cook pasta. Fry bacon. Mix eggs with cheese. Combine pasta and bacon. Toss with egg mixture. Season with pepper.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Vegetable Curry', 'Spiced vegetable curry', true, 4, 'Cook onions. Add curry powder. Add vegetables and chickpeas. Simmer with coconut milk. Serve with rice.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Caesar Salad', 'Classic caesar salad', true, 2, 'Chop lettuce. Add croutons and parmesan. Drizzle with caesar dressing. Toss and serve.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Beef Steak', 'Pan seared beef steak', false, 2, 'Heat butter in pan. Sear steak on both sides. Add garlic and rosemary. Cook to desired temperature.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Vegetable Oven Bake', 'Roasted vegetables from the oven', true, 6, 'Chop vegetables. Toss with oil and herbs. Roast in oven at 400F for 30 minutes.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Chicken Fried Rice', 'Quick stir fry rice with chicken', false, 4, 'Cook rice. Heat oil and add garlic. Add chicken. Add rice, eggs, and vegetables. Season with soy sauce.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

MERGE INTO ingredients (name) KEY(name) VALUES ('broccoli');
MERGE INTO ingredients (name) KEY(name) VALUES ('carrots');
MERGE INTO ingredients (name) KEY(name) VALUES ('bell peppers');
MERGE INTO ingredients (name) KEY(name) VALUES ('garlic');
MERGE INTO ingredients (name) KEY(name) VALUES ('soy sauce');
MERGE INTO ingredients (name) KEY(name) VALUES ('oil');
MERGE INTO ingredients (name) KEY(name) VALUES ('salmon');
MERGE INTO ingredients (name) KEY(name) VALUES ('lemon');
MERGE INTO ingredients (name) KEY(name) VALUES ('olive oil');
MERGE INTO ingredients (name) KEY(name) VALUES ('salt');
MERGE INTO ingredients (name) KEY(name) VALUES ('pepper');
MERGE INTO ingredients (name) KEY(name) VALUES ('dill');
MERGE INTO ingredients (name) KEY(name) VALUES ('pasta');
MERGE INTO ingredients (name) KEY(name) VALUES ('eggs');
MERGE INTO ingredients (name) KEY(name) VALUES ('bacon');
MERGE INTO ingredients (name) KEY(name) VALUES ('parmesan cheese');
MERGE INTO ingredients (name) KEY(name) VALUES ('black pepper');
MERGE INTO ingredients (name) KEY(name) VALUES ('potatoes');
MERGE INTO ingredients (name) KEY(name) VALUES ('chickpeas');
MERGE INTO ingredients (name) KEY(name) VALUES ('onions');
MERGE INTO ingredients (name) KEY(name) VALUES ('tomatoes');
MERGE INTO ingredients (name) KEY(name) VALUES ('curry powder');
MERGE INTO ingredients (name) KEY(name) VALUES ('coconut milk');
MERGE INTO ingredients (name) KEY(name) VALUES ('romaine lettuce');
MERGE INTO ingredients (name) KEY(name) VALUES ('croutons');
MERGE INTO ingredients (name) KEY(name) VALUES ('caesar dressing');
MERGE INTO ingredients (name) KEY(name) VALUES ('beef steak');
MERGE INTO ingredients (name) KEY(name) VALUES ('butter');
MERGE INTO ingredients (name) KEY(name) VALUES ('rosemary');
MERGE INTO ingredients (name) KEY(name) VALUES ('zucchini');
MERGE INTO ingredients (name) KEY(name) VALUES ('herbs');
MERGE INTO ingredients (name) KEY(name) VALUES ('chicken');
MERGE INTO ingredients (name) KEY(name) VALUES ('rice');
MERGE INTO ingredients (name) KEY(name) VALUES ('peas');

INSERT INTO recipe_ingredients (recipe_id, ingredient_id)
SELECT r.id, i.id
FROM recipes r
JOIN ingredients i ON i.name IN ('broccoli','carrots','bell peppers','garlic','soy sauce','oil')
WHERE r.name = 'Vegetable Stir Fry'
  AND NOT EXISTS (
    SELECT 1 FROM recipe_ingredients ri
    WHERE ri.recipe_id = r.id AND ri.ingredient_id = i.id
  );

INSERT INTO recipe_ingredients (recipe_id, ingredient_id)
SELECT r.id, i.id
FROM recipes r
JOIN ingredients i ON i.name IN ('salmon','lemon','olive oil','salt','pepper','dill')
WHERE r.name = 'Grilled Salmon'
  AND NOT EXISTS (
    SELECT 1 FROM recipe_ingredients ri
    WHERE ri.recipe_id = r.id AND ri.ingredient_id = i.id
  );

INSERT INTO recipe_ingredients (recipe_id, ingredient_id)
SELECT r.id, i.id
FROM recipes r
JOIN ingredients i ON i.name IN ('pasta','eggs','bacon','parmesan cheese','black pepper')
WHERE r.name = 'Pasta Carbonara'
  AND NOT EXISTS (
    SELECT 1 FROM recipe_ingredients ri
    WHERE ri.recipe_id = r.id AND ri.ingredient_id = i.id
  );

INSERT INTO recipe_ingredients (recipe_id, ingredient_id)
SELECT r.id, i.id
FROM recipes r
JOIN ingredients i ON i.name IN ('potatoes','chickpeas','onions','tomatoes','curry powder','coconut milk')
WHERE r.name = 'Vegetable Curry'
  AND NOT EXISTS (
    SELECT 1 FROM recipe_ingredients ri
    WHERE ri.recipe_id = r.id AND ri.ingredient_id = i.id
  );

INSERT INTO recipe_ingredients (recipe_id, ingredient_id)
SELECT r.id, i.id
FROM recipes r
JOIN ingredients i ON i.name IN ('romaine lettuce','parmesan cheese','croutons','caesar dressing')
WHERE r.name = 'Caesar Salad'
  AND NOT EXISTS (
    SELECT 1 FROM recipe_ingredients ri
    WHERE ri.recipe_id = r.id AND ri.ingredient_id = i.id
  );

INSERT INTO recipe_ingredients (recipe_id, ingredient_id)
SELECT r.id, i.id
FROM recipes r
JOIN ingredients i ON i.name IN ('beef steak','butter','garlic','rosemary','salt','pepper')
WHERE r.name = 'Beef Steak'
  AND NOT EXISTS (
    SELECT 1 FROM recipe_ingredients ri
    WHERE ri.recipe_id = r.id AND ri.ingredient_id = i.id
  );

INSERT INTO recipe_ingredients (recipe_id, ingredient_id)
SELECT r.id, i.id
FROM recipes r
JOIN ingredients i ON i.name IN ('potatoes','bell peppers','zucchini','tomatoes','olive oil','herbs')
WHERE r.name = 'Vegetable Oven Bake'
  AND NOT EXISTS (
    SELECT 1 FROM recipe_ingredients ri
    WHERE ri.recipe_id = r.id AND ri.ingredient_id = i.id
  );

INSERT INTO recipe_ingredients (recipe_id, ingredient_id)
SELECT r.id, i.id
FROM recipes r
JOIN ingredients i ON i.name IN ('chicken','rice','eggs','peas','carrots','soy sauce','garlic')
WHERE r.name = 'Chicken Fried Rice'
  AND NOT EXISTS (
    SELECT 1 FROM recipe_ingredients ri
    WHERE ri.recipe_id = r.id AND ri.ingredient_id = i.id
  );
