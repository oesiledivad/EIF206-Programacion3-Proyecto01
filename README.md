<div align="center">

# 📅 Sistema de Reserva de Recursos

### EIF206 — Programación III · Proyectos 01 y 02

Sistema de escritorio para la **gestión integral de reservas de recursos institucionales**, desarrollado con Java y JavaFX. Incluye autenticación y roles, gestión de reservas y recursos, persistencia MySQL, generación de reportes PDF, pruebas automatizadas e integración con **Inteligencia Artificial (LLM)** mediante la API de Groq.

<p>
  <img src="https://img.shields.io/badge/Java-17%2B-orange?logo=openjdk&logoColor=white" alt="Java 17+">
  <img src="https://img.shields.io/badge/JavaFX-21.0.6-2f73c9?logo=java&logoColor=white" alt="JavaFX 21">
  <img src="https://img.shields.io/badge/Maven-3.9.16-C71A36?logo=apachemaven&logoColor=white" alt="Maven">
  <img src="https://img.shields.io/badge/MySQL-8.4-4479A1?logo=mysql&logoColor=white" alt="MySQL 8.4">
  <img src="https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white" alt="Docker Compose">
  <img src="https://img.shields.io/badge/JUnit-5-25A162?logo=junit5&logoColor=white" alt="JUnit 5">
  <img src="https://img.shields.io/badge/Mockito-5.x-78C257" alt="Mockito">
  <img src="https://img.shields.io/badge/AI-Groq-00A67E" alt="Groq">
</p>

<p>
  <img src="https://img.shields.io/github/last-commit/oesiledivad/EIF206-Programacion3-Proyecto01?logo=github" alt="Último commit">
  <img src="https://img.shields.io/github/commit-activity/m/oesiledivad/EIF206-Programacion3-Proyecto01?logo=github" alt="Actividad del repositorio">
  <img src="https://img.shields.io/github/repo-size/oesiledivad/EIF206-Programacion3-Proyecto01?logo=github" alt="Tamaño del repositorio">
</p>

<p>
  🎓 <strong>Proyecto académico — Universidad Nacional de Costa Rica</strong>
</p>

</div>

---

## Módulos del repositorio

| Módulo | Documentación |
| --- | --- |
| Frontend | [Interfaz JavaFX, configuración y ejecución](frontend/README.md) |
| Backend | [Servicios, datos y pruebas](backend/README.md) |
| Common | [Modelos y serialización compartidos](common/README.md) |
| Base de datos | [MER y scripts MySQL](database/README.md) |

Consulta la [guía de arquitectura](docs/organizacion-proyecto-02.md) para conocer
las dependencias entre módulos y los comandos del proyecto.

---

## Características Principales

### 🔐 Autenticación y Gestión de Usuarios
- **Autenticación segura:** Inicio de sesión mediante credenciales de acceso.
- **Gestión de roles:** Soporte estructurado para perfiles de **Administrador** y **Funcionario**.
- **Gestión de sesiones:** Control dinámico de la sesión activa y restricciones según los privilegios del usuario.
- **Cambio de contraseña:** Opción segura para la actualización de credenciales.

### 📅 Gestión de Reservas
- **Creación de reservas:** Registro asistido de nuevos apartados de recursos institucionales.
- **Consulta de reservas:** Visualización en tiempo real y filtrado de reservas vigentes o pasadas.
- **Cancelación:** Opción exclusiva para que los funcionarios cancelen reservas futuras.
- **Validación de disponibilidad:** Control automático de conflictos e interbloqueos de recursos antes de confirmar.
- **Asignación inteligente:** Selección automatizada del primer recurso disponible que cumpla con los requisitos.

### 🤖 Asistente de Inteligencia Artificial (LLM)
- **Procesamiento de lenguaje natural:** Capacidad de describir actividades usando lenguaje cotidiano y descriptivo.
- **Generación automática:** Interpretación de solicitudes para rellenar de forma inteligente los formularios de reserva.
- **Extracción de recursos:** Identificación precisa de categorías y componentes solicitados en texto libre.
- **Integración con Groq:** Enlace directo con modelos LLM de alto rendimiento a través de la API de Groq.

### 🏢 Mantenimiento del Sistema (CRUD)
Operaciones completas de alta, consulta, modificación y baja para:
- **Funcionarios:** Información de usuario, nombre completo y número de teléfono.
- **Categorías de recursos:** Identificadores y descripciones generales.
- **Recursos físicos:** Control de números de activo, asociación por llave foránea (FK) y descripciones detalladas.

### 🗓️ Calendarización y Programación
- **Matriz de recursos:** Selección por fecha y categoría, visualizando filas de horarios y columnas de recursos físicos.
- **Programación semanal:** Vista matricial por horas y días de la semana con el detalle de los responsables y actividades.

### 📊 Estadísticas y Reportes PDF
- **Estadísticas dinámicas:** Rangos de fechas para recursos (categorías, cantidades) y actividades con gráficos de barras interactivos.
- **Reportes profesionales:** Generación automatizada de reportes en PDF integrados en todas las vistas principales del sistema.

### 💾 Persistencia y Calidad
- **Persistencia MySQL:** Repositorios JDBC y esquema relacional para usuarios, funcionarios, categorías, recursos y reservas.
- **Pruebas Automatizadas:** Suite completa de pruebas unitarias (JUnit 5) e integración (Mockito).

---

## Acceso inicial

Crear el administrador con la [utilidad del backend](database/USUARIOS.md).
Los funcionarios reciben su identificación como contraseña inicial y pueden
cambiarla desde la aplicación. Las credenciales se almacenan como hashes en MySQL.

La base también puede ejecutarse con [Docker Compose](database/DOCKER.md).

---

## Arquitectura del Sistema

El proyecto organiza sus capas en módulos Maven. La interfaz pertenece a `frontend`, los servicios, la lógica y el acceso a datos a `backend`, y los modelos compartidos a `common`. El ingreso, el cambio de contraseña y el panel inicial se comunican con el servidor mediante sockets. El backend utiliza MySQL. El diseño relacional y los scripts MySQL se encuentran en `database/`.

```text
┌─────────────────────────────────────────────┐
│           INTERFAZ DE USUARIO               │
│          JavaFX · FXML · CSS                │
│            Views + Controllers               │
└──────────────────────┬──────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────┐
│                 SERVICIOS                   │
│      Autenticación · Reservas · Sesiones    │
│       Usuarios · Recursos · Reportes        │
└──────────────────────┬──────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────┐
│              LÓGICA DE NEGOCIO              │
│     Validaciones · Reglas · Disponibilidad  │
│              Integración con IA             │
└──────────────────────┬──────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────┐
│                   DATOS                     │
│         MySQL · JDBC · Repositorios         │
└──────────────────────┬──────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────┐
│              MODELOS / DTO                  │
│       Entidades y objetos de transferencia  │
└─────────────────────────────────────────────┘

```

### Organización por Capas

| Capa                    | Responsabilidad                                                                                               |
|-------------------------|---------------------------------------------------------------------------------------------------------------|
| **Interfaz de Usuario** | Vistas JavaFX, archivos FXML, hojas de estilo CSS y controladores de eventos.                                 |
| **Servicios**           | Orquestación de operaciones de la aplicación y conexión lógica con controladores.                             |
| **Lógica**              | Reglas de negocio, validaciones de disponibilidad y submódulo de IA (`aiGenerator`).                          |
| **Datos**               | Operaciones CRUD mediante JDBC sobre las tablas MySQL.                                       |
| **Modelos**             | Clases de dominio principales (`Administrador`, `Categoria`, `Funcionario`, `Recurso`, `Reserva`, `Usuario`). |

### Tecnologías e Integraciones Clave

* 🤖 **Groq API:** Procesamiento e inferencia mediante modelos LLM avanzados.
* 💾 **MySQL y JDBC:** Almacenamiento relacional y acceso a datos.
* 📄 **Apache PDFBox + EasyTable:** Renderizado y exportación de reportes institucionales en PDF.
* 🔐 **Dotenv Java:** Manejo seguro de configuraciones y variables de entorno.

---

## Configuración del Archivo `.env`

Para habilitar la funcionalidad del asistente de Inteligencia Artificial, debes crear un archivo llamado **`.env`** en la raíz absoluta del proyecto (junto al archivo `pom.xml`).

El archivo debe tener la siguiente estructura exacta:

```env
GROQ_API_KEY=tu_api_key_de_groq_aqui
GROQ_URL=https://api.groq.com/openai/v1/chat/completions
GROQ_MODEL=nombre_del_modelo_qwen
```

### Descripción de Variables

* **`GROQ_API_KEY`**: Clave de autenticación personal obtenida en la plataforma de Groq.
* **`GROQ_URL`**: Endpoint oficial de la API de Groq consumido por la aplicación.
* **`GROQ_MODEL`**: Identificador del modelo de lenguaje utilizado por el sistema.

> **Nota Importante:** Se recomienda emplear modelos de la familia **Qwen** disponibles en Groq por su excelente desempeño en la extracción estructurada de datos. Como los catálogos cambian con el tiempo, verifica el modelo activo en tu cuenta de Groq antes de ejecutar. El archivo `.env` está protegido por defecto y **nunca debe subirse al control de versiones**.

---


## Configuración del Archivo `.config/docker.env`

Docker Compose utiliza este archivo para crear MySQL. Si ejecutas `iniciar.ps1`,
se genera automáticamente con contraseñas locales. Para configurarlo manualmente,
copia `.config/docker.env.example` como `.config/docker.env` y completa:

```env
MYSQL_PORT=3307
MYSQL_PASSWORD=tu_clave_de_reservas_app
MYSQL_ROOT_PASSWORD=tu_clave_de_root
```

### Descripción de Variables

* **`MYSQL_PORT`**: Puerto de MySQL en tu computadora; el contenedor utiliza internamente el puerto `3306`.
* **`MYSQL_PASSWORD`**: Contraseña del usuario MySQL `reservas_app`, utilizado por el backend.
* **`MYSQL_ROOT_PASSWORD`**: Contraseña del administrador de MySQL, independiente del usuario `admin` de la aplicación.

Estas contraseñas se establecen al inicializar un volumen vacío. Editar el archivo
después no cambia las credenciales de una base existente.

---

## Configuración del Archivo `.config/database.env`

El backend utiliza este archivo para conectarse a MySQL. `iniciar.ps1` lo genera
con los datos de Docker cuando no existe. Para configurarlo manualmente, copia
`.config/database.docker.env.example` como `.config/database.env` y completa:

```env
DB_URL=jdbc:mysql://127.0.0.1:3307/sistema_reservas
DB_USER=reservas_app
DB_PASSWORD=tu_clave_de_reservas_app
```

### Descripción de Variables

* **`DB_URL`**: Dirección JDBC de la base. El puerto debe coincidir con `MYSQL_PORT` de `.config/docker.env`.
* **`DB_USER`**: Usuario MySQL de conexión; Docker crea `reservas_app`.
* **`DB_PASSWORD`**: Debe ser exactamente la misma contraseña que `MYSQL_PASSWORD`.

Para MySQL instalado directamente en el sistema, utiliza como plantilla
`.config/database.env.example` y ajusta el puerto, usuario y contraseña a esa
instalación. El script de inicio automático utiliza la configuración de Docker.

---

## Configuración del Archivo `.config/admin.env`

El script de inicio utiliza este archivo para crear el administrador de la
aplicación cuando no existe. Puedes copiar `.config/admin.env.example` como
`.config/admin.env`; si no existe, el script lo genera con este contenido:

```env
APP_ADMIN_PASSWORD=admin
```

### Descripción de Variables

* **`APP_ADMIN_PASSWORD`**: Contraseña inicial del usuario de aplicación `admin`. Acepta `admin` o una contraseña personalizada de al menos seis caracteres.

El acceso inicial por defecto es **admin / admin**. Cambiar este archivo no
restablece la contraseña de un administrador existente; el cambio de contraseña
se realiza desde la aplicación.

Los tres archivos `.config/*.env` anteriores están excluidos de Git. Las
plantillas `.example` documentan su estructura y se incluyen en el repositorio.
La configuración de Groq es independiente y no es necesaria para iniciar sesión.

---

## Requisitos Previos e Instalación de Maven

Asegúrate de tener instalado en tu entorno de desarrollo:

1. **Java Development Kit (JDK) 17 o superior** (se recomienda fuertemente JDK 21).
2. **Docker Desktop** iniciado, con contenedores Linux y Docker Compose.
3. **Maven Wrapper** incluido en el repositorio (`mvnw.cmd` en Windows y `mvnw` en Linux/macOS). Descarga Maven 3.9.16 en la primera ejecución; no requiere instalar Maven por separado.

### Maven instalado en el sistema (opcional)

* **En Linux (Ubuntu/Debian):**
```bash
sudo apt update
sudo apt install maven
```


* **En macOS (usando Homebrew):**
```bash
brew install maven
```


* **En Windows:**
1. Descarga el binario comprimido de Maven desde el [sitio web oficial](https://maven.apache.org/download.cgi).
2. Extrae el contenido en una ruta de tu preferencia (ej. `C:\Program Files\Apache\maven`).
3. Añade la ruta de la carpeta `bin` a las **Variables de Entorno** del sistema (`PATH`).
4. Verifica la instalación ejecutando en tu terminal:
```bash
mvn -version
```
---

## Estructura del Proyecto

```text
pom.xml                         # POM padre: agrega los tres módulos
mvnw / mvnw.cmd                 # Maven Wrapper
iniciar.ps1                     # Arranque y cierre local de JavaFX, backend y MySQL
compose.yaml                    # MySQL 8.4 con volumen persistente
.config/                        # Ejemplos de configuración local
common/
├── pom.xml
└── src/main/java/              # Modelos y adaptadores compartidos
backend/
├── pom.xml
└── src/
    ├── main/                   # Servicios, lógica, IA y datos MySQL
    └── test/                   # Pruebas de negocio y persistencia
frontend/
├── pom.xml
└── src/
    ├── main/                   # JavaFX, presentación y reportes PDF
    └── test/                   # Pruebas de login y lógica de presentación
database/                       # MER DBML, esquema MySQL y catálogo de ejemplo
docs/                           # Documentación de arquitectura
data/xml/                       # Archivos de datos del proyecto 01
```

---

## Compilación y Ejecución

El recorrido de inicio es **JavaFX → sockets → Backend → MySQL en Docker**.
`common` es una biblioteca compartida: se compila con el proyecto y no se ejecuta
como una aplicación independiente.

Los siguientes pasos usan **PowerShell**, desde la raíz del repositorio.
En Linux/macOS, el comando de Maven Wrapper es `./mvnw`.

### 🚀 Inicio automático en Windows

Con **JDK 17 o superior** instalado y **Docker Desktop en ejecución**, abre
PowerShell en la raíz del repositorio y ejecuta:

```powershell
.\iniciar.ps1
```

El script configura los archivos locales cuando no existen, inicia MySQL,
compila `common`, `backend` y `frontend`, crea el administrador si no existe y
abre JavaFX conectado al backend. Conserva las credenciales y los datos existentes.
En una instalación nueva, entra con el usuario **admin** y la contraseña **admin**.
La clave inicial se guarda en `.config/admin.env`, un archivo local excluido de Git.
Si ya existe un administrador o ese archivo tiene otra clave, se conserva su configuración.

Al cerrar JavaFX, el script detiene el backend que inició y el servicio MySQL de
este proyecto, incluso si MySQL ya estaba encendido. También los detiene al
terminar `-Comprobar` o si ocurre un error después de intentar iniciar MySQL.
El volumen conserva los datos para el siguiente arranque. Docker Desktop
permanece abierto.
Los registros del backend quedan en `target/local/backend.log` y
`target/local/backend-error.log`.

```powershell
# Comprobar MySQL y backend sin abrir JavaFX
.\iniciar.ps1 -Comprobar

# Reutilizar la compilación anterior cuando no cambió el código
.\iniciar.ps1 -SinCompilar

# Usar otro puerto para el backend y su cliente
.\iniciar.ps1 -PuertoBackend 1235
```

Si PowerShell bloquea los scripts, puedes habilitar únicamente esta ejecución:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\iniciar.ps1
```

También puedes ejecutar cada paso manualmente como se describe a continuación.

### 1. 📥 Obtener el proyecto y comprobar Java

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

### 2. ⚙️ Configurar MySQL

Crear los archivos locales solo si no existen:

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

### 3. 🐳 Iniciar MySQL

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

### 4. 🔨 Compilar los tres módulos

```powershell
.\mvnw.cmd install -DskipTests
```

Este comando compila e instala `common`, `backend` y `frontend` en el repositorio
Maven local. Ejecutarlo también después de cambiar clases compartidas.

### 5. 🔐 Crear el administrador

Ejecutar una vez para la base de datos elegida. La contraseña solicitada será la
que usarás en la pantalla de login; debe tener al menos seis caracteres.

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

### 6. 🖥️ Ejecutar el backend — terminal 1

```powershell
.\mvnw.cmd -pl backend exec:java
```

Mantener esa terminal abierta. Debe mostrar:

```text
Servidor escuchando en el puerto 1234
```

### 7. 📅 Ejecutar el frontend — terminal 2

Abrir otra terminal en la misma raíz del repositorio, con el JDK configurado:

```powershell
.\mvnw.cmd -pl frontend javafx:run
```

Ingresar con **ID `admin`** y la contraseña del paso 5. Debe abrirse el panel
principal con el nombre **Administrador** y los contadores leídos desde el backend.
Si se cargó el catálogo, el panel mostrará **2 recursos**.

El login, la consulta del panel inicial, el cambio de contraseña y el cierre de
sesión utilizan el proxy de sockets. Las demás vistas conservan sus llamadas a
los servicios locales. Las operaciones de red de login y carga del panel se
realizan en segundo plano para mantener la interfaz disponible.

Para conectar ese frontend a un backend en otra computadora, definir antes de
arrancarlo:

```powershell
$env:SERVER_HOST = "IP_DEL_EQUIPO_BACKEND"
$env:SERVER_PORT = "1234"
```

### 8. ✅ Verificar la conexión

Las pruebas del recorrido de ingreso usan sockets reales y la base configurada.
Ejecutarlas con una base exclusiva para pruebas:

```powershell
.\mvnw.cmd verify "-Dit.test=LoginIntegracionIT" -Dfailsafe.failIfNoSpecifiedTests=false
```

Se ejecutan las pruebas unitarias y la integración del login, el panel remoto,
el cambio de contraseña y logout. Groq no es necesario para iniciar sesión;
la configuración de IA se utiliza al invocar el asistente de reservas.

### 🧪 Integración continua en GitHub Actions

El workflow **Tests** (`.github/workflows/tests.yml`) se ejecuta en los pushes y
pull requests dirigidos a `main` y `develop`. También puede iniciarse desde
**Actions → Tests → Run workflow**.

Utiliza JDK 21, Maven Wrapper y una instancia MySQL 8.4 exclusiva de CI,
inicializada con `database/schema.sql`. Compila los tres módulos y ejecuta todas
las pruebas unitarias junto con estas integraciones:

* `UsuariosIntegracionIT`: persistencia de usuarios, hashes, estado activo y cambio de contraseña.
* `FuncionariosIntegracionIT`: perfiles de funcionarios y su clave inicial.
* `ServerIntegracionIT`: operaciones y permisos del servidor por sockets.
* `LoginIntegracionIT`: ingreso, panel remoto, cambio de contraseña y cierre de sesión desde el cliente.

Las demás suites de integración quedan fuera de este comando; el resultado del
workflow corresponde a las pruebas enumeradas. No requiere secretos de Groq ni
archivos de configuración de tu computadora. Los reportes se publican como
artefacto **reportes-pruebas** y MySQL se elimina al terminar la ejecución.

### 9. ⏹️ Detener la ejecución

Con `iniciar.ps1`, basta con cerrar la ventana JavaFX: el script detiene el
backend y MySQL automáticamente.

Si seguiste los pasos manuales, cierra JavaFX, detén el backend con `Ctrl+C` y
apaga MySQL:

```powershell
docker compose --env-file .config/docker.env down
```

El volumen conserva los datos. Los scripts de inicialización y las contraseñas
de creación de MySQL solo se aplican a un volumen vacío.

### 🛠️ Problemas de conexión

| Mensaje o síntoma | Comprobación |
| --- | --- |
| Java 8 o `Unrecognized option: --module-path` | Revisar `JAVA_HOME` y la salida de `.\mvnw.cmd -version` |
| No se puede conectar con Docker | Abrir Docker Desktop y comprobar que el motor esté iniciado |
| MySQL no aparece `healthy` | Consultar `docker compose --env-file .config/docker.env logs mysql` |
| `Access denied` de MySQL | Comparar `DB_PASSWORD` con la contraseña usada al crear el volumen; editar un archivo no cambia las credenciales de una base existente |
| No se pudo comunicar con el servidor | Iniciar primero el backend y comprobar host y puerto `1234` |
| Credenciales incorrectas | Usar el usuario de aplicación `admin`, no `reservas_app`, y la contraseña del paso 5 |
| Dependencia `common` o `backend` no encontrada | Ejecutar `.\mvnw.cmd install -DskipTests` desde la raíz |

Más información: [Docker y persistencia](database/DOCKER.md),
[usuarios y credenciales](database/USUARIOS.md) y
[protocolo de autenticación](docs/protocolo-autenticacion.md).
