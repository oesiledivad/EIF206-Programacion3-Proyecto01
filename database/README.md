# Base de datos del proyecto 02

Esquema relacional del Sistema de Reserva de Recursos para MySQL 8.4.
Incluye el modelo DBML, la creación de tablas y un catálogo de demostración.

## Archivos

- `modelo.dbml`: fuente editable del MER; copiar en dbdiagram.io y exportar el diagrama.
- `schema.sql`: crea `sistema_reservas`, sus seis tablas, PK, FK, indices y CHECK de horario.
- `seed.sql`: catalogo opcional de demostracion (dos categorias y dos recursos).

El seed contiene únicamente categorías y recursos de demostración. Las cuentas
de aplicación se representan mediante las tablas `usuarios` y `funcionarios`.

## Instalacion manual

Desde la raiz del repositorio, abrir el cliente MySQL en PowerShell:

```powershell
& "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe" --default-character-set=utf8mb4 -h 127.0.0.1 -P 3306 -u root -p
```

Dentro del cliente, ejecutar en este orden:

```sql
SOURCE database/schema.sql;
SOURCE database/seed.sql;
USE sistema_reservas;
SHOW TABLES;
SELECT * FROM categorias;
SELECT * FROM recursos;
```

`seed.sql` es opcional. Si se usa Workbench, abrir y ejecutar primero `schema.sql`
y luego `seed.sql`; `SOURCE` corresponde al cliente de linea de comandos.
Se espera encontrar seis tablas y, con el seed en una base nueva, dos categorias
y dos recursos. Usuarios, funcionarios y reservas quedan vacios.

Los scripts no eliminan ni reemplazan datos. Se pueden repetir: el esquema usa
`IF NOT EXISTS` y el seed evita insertar identificadores existentes. El seed solo
vincula los recursos a las categorias de demostracion esperadas.
`IF NOT EXISTS` no migra una tabla creada con una estructura diferente; los cambios
posteriores de estructura requeriran scripts de migracion.

## Modelo de datos y reglas de negocio

- Usuario/funcionario: nombre en `usuarios`; perfil en `funcionarios` con PK/FK compartida.
- Categorias y reservas: identificadores enteros autogenerados. Los prefijos
  `CAT-` y `RES-` corresponden al formato de presentación.
- Recursos: numero de activo como texto. Reservas-recursos: PK compuesta.
- Estados de reserva: `ACTIVA`, `CANCELADA`; roles: `ADMIN`, `FUNCIONARIO`.
- Borrados: baja logica con `activo`; las FK restringen el borrado fisico asociado.
- Autenticacion: verificar hashes e impedir acceso a usuarios inactivos.
- Coherencia rol/perfil, disponibilidad sin traslapes, un recurso por categoria
  y al menos una asignacion por reserva: validar en el backend.
- Crear reserva y asignaciones atomicamente con bloqueos para evitar dobles reservas.
- Cancelar conserva el historial; no considerar canceladas al comprobar disponibilidad.
- Definir en estadisticas si se incluyen canceladas. No cambiar la categoria de
  un recurso con historial; gestionar reservas futuras antes de bajas logicas.

El script usa `root` solo para la instalacion manual. La aplicacion debe tener
un usuario MySQL propio, configurado exclusivamente en el backend. No subir
credenciales al repositorio.

## Entorno y usuarios

- [MySQL con Docker Compose](DOCKER.md)
- [Usuarios, contraseñas y pruebas](USUARIOS.md)
