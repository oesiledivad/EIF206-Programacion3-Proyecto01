# MySQL con Docker Compose

Requiere Docker con Compose. Ejecutar desde la raíz del repositorio.

## Configuración

Copiar los ejemplos y completar las contraseñas antes de iniciar:

```powershell
Copy-Item .config/docker.env.example .config/docker.env
Copy-Item .config/database.docker.env.example .config/database.env
```

Si ya existe `.config/database.env`, conservarlo antes de sustituirlo. Usar la misma
contraseña de `reservas_app` en ambos archivos. Las configuraciones locales están
excluidas de Git.

## Inicio y conexión

```sh
docker compose --env-file .config/docker.env config --quiet
docker compose --env-file .config/docker.env up -d --wait
docker compose --env-file .config/docker.env ps
```

MySQL 8.4 publica el puerto `3307` de localhost y puede convivir con una instalación
local en `3306`. El backend se ejecuta desde Java y usa la configuración JDBC de
`.config/database.env`. Workbench puede conectarse a `127.0.0.1:3307` con el usuario
`reservas_app`.

La imagen crea la base y el usuario de aplicación. `database/schema.sql` se ejecuta
al inicializar un volumen vacío. El catálogo de demostración es opcional:

```powershell
Get-Content -Raw database/seed.sql | docker compose --env-file .config/docker.env exec -T mysql sh -c 'MYSQL_PWD="$MYSQL_PASSWORD" mysql -u "$MYSQL_USER" "$MYSQL_DATABASE"'
```

## Datos y ciclo de vida

```sh
docker compose --env-file .config/docker.env stop
docker compose --env-file .config/docker.env start
docker compose --env-file .config/docker.env down
```

El volumen `mysql_data` conserva la información aunque se elimine el contenedor.
Los scripts y las variables de inicialización no se vuelven a aplicar a un volumen
con datos. Los cambios de esquema se aplican mediante migraciones SQL.

`docker compose down -v` elimina también los datos del volumen; usarlo únicamente
cuando se quiera descartar la base completa.

[Scripts y modelo de datos](README.md) · [Documentación general](../README.md)
