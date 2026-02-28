DELETE FROM cliente;

INSERT INTO cliente (id, nombre, apellido, membresia) VALUES (1, 'Ana', 'Gomez', 101);
INSERT INTO cliente (id, nombre, apellido, membresia) VALUES (2, 'Luis', 'Perez', 102);
INSERT INTO cliente (id, nombre, apellido, membresia) VALUES (3, 'Marta', 'Diaz', 103);

ALTER TABLE cliente ALTER COLUMN id RESTART WITH 4;
