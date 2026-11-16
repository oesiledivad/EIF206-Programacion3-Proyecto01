-- Catalogo opcional de demostracion; usar despues de schema.sql.
-- No crea credenciales, usuarios ni reservas. No modifica filas existentes.
USE sistema_reservas;
START TRANSACTION;

INSERT INTO categorias (id_categoria, descripcion)
SELECT 1, 'Sala para 10 personas'
WHERE NOT EXISTS (SELECT 1 FROM categorias WHERE id_categoria = 1);

INSERT INTO categorias (id_categoria, descripcion)
SELECT 2, 'Laptop Windows 11'
WHERE NOT EXISTS (SELECT 1 FROM categorias WHERE id_categoria = 2);

INSERT INTO recursos (id_recurso, id_categoria, descripcion)
SELECT 'SALA-001', id_categoria, 'Sala 1, primer piso'
FROM categorias
WHERE id_categoria = 1 AND descripcion = 'Sala para 10 personas'
  AND NOT EXISTS (SELECT 1 FROM recursos WHERE id_recurso = 'SALA-001');

INSERT INTO recursos (id_recurso, id_categoria, descripcion)
SELECT 'LAPTOP-001', id_categoria, 'Laptop de demostracion'
FROM categorias
WHERE id_categoria = 2 AND descripcion = 'Laptop Windows 11'
  AND NOT EXISTS (SELECT 1 FROM recursos WHERE id_recurso = 'LAPTOP-001');

COMMIT;
