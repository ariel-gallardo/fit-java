DELETE FROM usuario;
DELETE FROM cliente;
DELETE FROM membresia;

INSERT INTO membresia (id, tipo, precio) VALUES (1, 'SILVER', 100);
INSERT INTO membresia (id, tipo, precio) VALUES (2, 'GOLD', 200);
INSERT INTO membresia (id, tipo, precio) VALUES (3, 'BRONZE', 300);

INSERT INTO cliente (id, nombre, apellido, membresia_id, membresia_expira_en) VALUES (1, 'Ana', 'Gomez', 1, '2030-01-01T00:00:00');
INSERT INTO cliente (id, nombre, apellido, membresia_id, membresia_expira_en) VALUES (2, 'Luis', 'Perez', 2, '2030-06-01T00:00:00');
INSERT INTO cliente (id, nombre, apellido, membresia_id, membresia_expira_en) VALUES (3, 'Marta', 'Diaz', NULL, NULL);

INSERT INTO usuario (id, username, email, password, rol, cliente_id) VALUES (1, 'admin', 'admin@zonafit.com', '{noop}admin123', 'ADMIN', 1);
INSERT INTO usuario (id, username, email, password, rol, cliente_id) VALUES (2, 'cliente', 'cliente@zonafit.com', '{noop}cliente123', 'CLIENTE', 2);

ALTER TABLE membresia ALTER COLUMN id RESTART WITH 4;
ALTER TABLE cliente ALTER COLUMN id RESTART WITH 4;
ALTER TABLE usuario ALTER COLUMN id RESTART WITH 3;
