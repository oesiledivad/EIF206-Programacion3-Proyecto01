# Ejecución manual con MySQL en Docker

Este recorrido permite iniciar MySQL, backend y frontend por separado. Para el
arranque automático, consulta el [README](../README.md#compilación-y-ejecución).
Los comandos usan PowerShell desde la raíz del repositorio. En Linux/macOS,
utiliza `./mvnw` para los comandos Maven.

Los pasos 2 y 3 utilizan MySQL en Docker. Para utilizar una instalación propia
de MySQL, sigue la [compilación y ejecución sin Docker](ejecucion-sin-docker.md).

## 1. 📥 Obtener el proyecto y comprobar Java

```powershell
git clone --branch develop https://github.com/oesiledivad/EIF206-Programacion3-Proyecto01.git
cd EIF206-Programacion3-Proyecto01
.\mvnw.cmd -version
docker compose version
```

Si ya tienes el repositorio, utiliza tu copia de trabajo. Maven debe indicar
**Java 17 o superior**, preferiblemente **JDK 21**. Si muestra Java 8, configura
`JAVA_HOME` con la ruta de tu JDK y agrega su carpeta `bin` al `PATH`. Comprueba
la versión en cada terminal usada para ejecutar el proyecto.

## 2. ⚙️ Configurar MySQL

Crear los archivos locales solo si no existen. Su estructura se explica en la
[configuración del README](../README.md#configuración):

```powershell
if (-not (Test-Path .config/docker.env)) {
    Copy-Item .config/docker.env.example .config/docker.env
}
if (-not (Test-Path .config/database.env)) {
    Copy-Item .config/database.docker.env.example .config/database.env
}
```

Editar ambos archivos con los siguientes datos:

| Archivo | Configuración |
| --- | --- |
| `.config/docker.env` | `MYSQL_PORT=3307`, contraseña de `reservas_app` en `MYSQL_PASSWORD` y contraseña de MySQL root en `MYSQL_ROOT_PASSWORD` |
| `.config/database.env` | `DB_URL=jdbc:mysql://127.0.0.1:3307/sistema_reservas`, `DB_USER=reservas_app` y la misma contraseña de aplicación en `DB_PASSWORD` |

Las contraseñas de ejemplo deben sustituirse por valores locales. Estos archivos
están excluidos de Git. El usuario MySQL `reservas_app` es la cuenta de conexión
del backend; el usuario `admin` del sistema se crea en el paso 5.

## 3. 🐳 Iniciar MySQL

Con Docker Desktop abierto:

```powershell
docker compose --env-file .config/docker.env config --quiet
docker compose --env-file .config/docker.env up -d --wait
docker compose --env-file .config/docker.env ps
```

El servicio debe aparecer como **healthy**. La base `sistema_reservas` y sus seis
tablas se crean automáticamente al inicializar un volumen vacío. MySQL publica
`127.0.0.1:3307`, por lo que puede convivir con otra instalación local en `3306`.

Para cargar las dos categorías y los dos recursos de demostración:

```powershell
Get-Content -Raw database/seed.sql | docker compose --env-file .config/docker.env exec -T mysql sh -c 'MYSQL_PWD="$MYSQL_PASSWORD" mysql -u "$MYSQL_USER" "$MYSQL_DATABASE"'
```

El catálogo es opcional; el login también funciona con un catálogo vacío.

## 4. 🔨 Compilar los tres módulos

```powershell
.\mvnw.cmd install -DskipTests
```

Este comando compila e instala `common`, `backend` y `frontend` en el repositorio
Maven local. Ejecutarlo también después de cambiar clases compartidas.

## 5. 🔐 Crear el administrador

Ejecutar una vez para la base de datos elegida. La contraseña solicitada será la
que usarás en la pantalla de login; puede ser `admin` o una clave personalizada de al menos seis caracteres.

```powershell
$claveAdmin = Read-Host "Contraseña para admin" -AsSecureString
$env:APP_ADMIN_PASSWORD = [System.Net.NetworkCredential]::new("", $claveAdmin).Password
try {
    .\mvnw.cmd -pl backend exec:java "-Dexec.mainClass=una.proyecto.bootstrap.CrearAdministrador"
} finally {
    Remove-Item Env:APP_ADMIN_PASSWORD -ErrorAction SilentlyContinue
    $claveAdmin.Dispose()
}
```

El resultado es **Administrador creado.** Si `admin` ya existe, la utilidad
conserva su contraseña y su estado. Usar la contraseña definida cuando se creó;
las credenciales se almacenan como hashes en MySQL.

## 6. 🖥️ Ejecutar el backend — terminal 1

```powershell
.\mvnw.cmd -pl backend exec:java
```

Mantener esa terminal abierta. Debe mostrar:

```text
Servidor escuchando en el puerto 1234
```

## 7. 📅 Ejecutar el frontend — terminal 2

Abrir otra terminal en la misma raíz del repositorio, con el JDK configurado:

```powershell
.\mvnw.cmd -pl frontend javafx:run
```

Ingresar con **ID `admin`** y la contraseña del paso 5. Debe abrirse el panel
principal con el nombre **Administrador** y los contadores leídos desde el backend.
Si se cargó el catálogo, el panel mostrará **2 recursos**.

Las operaciones de datos utilizan el proxy de sockets. El frontend no requiere
configuración MySQL ni incluye el backend entre sus dependencias de ejecución.
Las operaciones de red de login, carga del panel y estadísticas se realizan en
segundo plano para mantener la interfaz disponible.

Para conectar ese frontend a un backend en otra computadora, definir antes de
arrancarlo (también puedes usar el [script de clientes](../README.md#-conectar-dos-computadoras-en-la-misma-red)):

```powershell
$env:SERVER_HOST = "IP_DEL_EQUIPO_BACKEND"
$env:SERVER_PORT = "1234"
```

## 8. ⏹️ Detener los procesos

Cierra JavaFX, detén el backend con `Ctrl+C` y apaga MySQL:

```powershell
docker compose --env-file .config/docker.env down
```

El volumen conserva los datos. Los scripts de inicialización y las contraseñas
de creación de MySQL solo se aplican a un volumen vacío.

La [sección de requisitos](../README.md#requisitos) incluye la instalación de
Java, Maven y Docker.

[Volver al README](../README.md) · [Pruebas funcionales](pruebas-funcionales.md)
