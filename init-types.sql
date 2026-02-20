-- Insert pet types if they don't exist
INSERT INTO types (id, name) SELECT 1, 'cat' WHERE NOT EXISTS (SELECT 1 FROM types WHERE name = 'cat');
INSERT INTO types (id, name) SELECT 2, 'dog' WHERE NOT EXISTS (SELECT 1 FROM types WHERE name = 'dog');
INSERT INTO types (id, name) SELECT 3, 'lizard' WHERE NOT EXISTS (SELECT 1 FROM types WHERE name = 'lizard');
INSERT INTO types (id, name) SELECT 4, 'snake' WHERE NOT EXISTS (SELECT 1 FROM types WHERE name = 'snake');
INSERT INTO types (id, name) SELECT 5, 'bird' WHERE NOT EXISTS (SELECT 1 FROM types WHERE name = 'bird');
INSERT INTO types (id, name) SELECT 6, 'hamster' WHERE NOT EXISTS (SELECT 1 FROM types WHERE name = 'hamster');
