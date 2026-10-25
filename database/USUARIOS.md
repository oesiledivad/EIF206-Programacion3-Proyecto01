# Usuarios y credenciales

`UsuarioDatos` almacena los datos generales en `usuarios` y el teléfono del
funcionario en `funcionarios`. Crear o editar un perfil utiliza una conexión
propia y una transacción para ambas tablas.

Las contraseñas se almacenan con PBKDF2-HMAC-SHA256, 600 000 iteraciones, sal
aleatoria de 16 bytes y clave derivada de 32 bytes. El formato de `clave_hash` es:

```text
pbkdf2-sha256$iteraciones$sal_base64$hash_base64
```

El hash se consulta únicamente para autenticar o cambiar la contraseña. Las
consultas de perfiles no incluyen credenciales. El campo de contraseña del modelo
compartido se excluye de la serialización Java.

## Administrador

Con la base configurada e inicializada, ejecutar la clase
`una.proyecto.bootstrap.CrearAdministrador` del backend, usando la raíz del
repositorio como directorio de trabajo. Con una consola interactiva solicita la
contraseña; desde IntelliJ, definir `APP_ADMIN_PASSWORD` en el entorno de esa
ejecución. Acepta la clave inicial `admin` o una contraseña de al menos seis caracteres.
El script `iniciar.ps1` utiliza `admin` / `admin` en instalaciones nuevas.

Desde la raíz también se puede ejecutar con Maven Wrapper:

```sh
./mvnw -pl backend exec:java "-Dexec.mainClass=una.proyecto.bootstrap.CrearAdministrador"
```

En Windows, usar `.\mvnw.cmd`. La [guía de arranque](../README.md#compilación-y-ejecución)
incluye el ingreso de la contraseña desde PowerShell.

La utilidad crea el usuario `admin`. Si existe, conserva su contraseña y estado.
Las contraseñas se configuran localmente y no se incluyen en los scripts SQL.

## Funcionarios

Al crear un funcionario mediante `FuncionarioLogic`, su clave inicial es su ID,
como establece el enunciado. `UsuarioDatos` guarda el hash de esa clave.
Actualizar nombre y teléfono conserva las credenciales almacenadas.

La baja es lógica: establece `usuarios.activo = FALSE` y conserva el perfil y sus
relaciones. Un usuario inactivo no aparece en las consultas activas, no puede
autenticarse ni cambiar la contraseña. Editar su perfil no lo reactiva.

El cambio de contraseña verifica la actual y actualiza solo si el hash sigue
siendo el que se verificó. Así, un cambio simultáneo o una baja impiden que una
operación obsoleta sobrescriba las credenciales.

## Pruebas

Las pruebas de integración requieren una base exclusiva para pruebas con
`schema.sql`. Se puede usar otra instancia Compose con otro nombre de proyecto
y otro puerto. No usar datos personales en esa base.

```sh
./mvnw verify "-Dit.test=UsuariosIntegracionIT,FuncionariosIntegracionIT,ServerIntegracionIT,LoginIntegracionIT" -Dfailsafe.failIfNoSpecifiedTests=false
```

La conexión acepta `DB_URL`, `DB_USER` y `DB_PASSWORD` desde el entorno o desde
`.config/database.env`. Las pruebas eliminan únicamente sus identificadores de
usuario reservados. La conversión de archivos XML existentes es una operación
independiente de la creación del esquema.

[MySQL con Docker](DOCKER.md) · [Esquema y catálogo](README.md)

[Contrato de autenticación por sockets](../docs/protocolo-autenticacion.md)
