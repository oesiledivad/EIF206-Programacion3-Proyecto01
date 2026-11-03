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

## Contenido

- [Requisitos](#requisitos)
- [Compilación y ejecución](#compilación-y-ejecución)
- [Otros clientes y alertas](#otros-clientes-y-alertas)
- [Configuración](#configuración)
- [Pruebas](#pruebas)
- [Problemas de conexión](#-problemas-de-conexión)
- [Funcionalidades](#funcionalidades)
- [Arquitectura y módulos](#arquitectura-y-módulos)
- [Documentación](#documentación)

---

## Requisitos

| Componente | Equipo con backend y MySQL | Equipo que solo ejecuta frontend |
| --- | --- | --- |
| JDK 17 o superior (21 recomendado) | Sí | Sí |
| Docker Desktop con contenedores Linux y Compose | Sí, para el arranque automático | No |
| MySQL Community Server 8.4 | Docker lo instala; instalación propia para ejecutar sin Docker | No |
| Repositorio con la misma versión del código | Sí | Sí |

Maven Wrapper está incluido: `mvnw.cmd` en Windows y `mvnw` en Linux/macOS.
Descarga Maven en su primera ejecución; no necesitas instalarlo por separado.
Necesitas acceso a Internet para descargar Maven, sus dependencias y, al usar
Docker, la imagen de MySQL. JavaFX y el conector JDBC se resuelven con Maven.

### ☕ Instalar Java

1. Descarga **Eclipse Temurin JDK 21** para tu sistema y arquitectura desde
   [Adoptium](https://adoptium.net/temurin/releases/?version=21). Instala el JDK completo, que incluye `javac`.
2. En Windows, ejecuta el MSI y habilita las opciones para agregar Java al `PATH`
   y configurar `JAVA_HOME`, según la [guía del instalador](https://adoptium.net/installation/windows/).
3. Cierra y vuelve a abrir PowerShell. Comprueba:

```powershell
java -version
javac -version
$env:JAVA_HOME
```

Ambos comandos deben mostrar el JDK elegido. `JAVA_HOME` debe apuntar a su
carpeta de instalación, sin agregar `bin`. Si tienes varias versiones de Java,
revisa también qué JDK muestra `.\mvnw.cmd -version` al obtener el repositorio.

### 🔧 Instalar Git y obtener el proyecto

Instala [Git para Windows](https://git-scm.com/install/windows), abre una nueva
terminal y comprueba `git --version`. Obtén el proyecto o usa tu copia actual:


```powershell
git clone --branch develop https://github.com/oesiledivad/EIF206-Programacion3-Proyecto01.git
cd EIF206-Programacion3-Proyecto01
.\mvnw.cmd -version
```

Maven debe mostrar Java 17 o superior. Si muestra otra versión, ajusta
`JAVA_HOME` y agrega la carpeta `bin` del JDK al `PATH` de esa terminal.

### 📦 Maven Wrapper e instalación de Maven

La opción recomendada es utilizar el Wrapper del repositorio:

```powershell
.\mvnw.cmd -version
```

Si prefieres instalar Maven en Windows, descarga el **Binary zip archive** desde
[Apache Maven](https://maven.apache.org/download.cgi), extráelo y agrega su
carpeta `bin` al `PATH`. Abre otra terminal y comprueba `mvn -version`, siguiendo
la [guía de instalación](https://maven.apache.org/install.html).
Los comandos `mvn` y `.\mvnw.cmd` utilizan el mismo `pom.xml`.

En Linux/macOS puedes usar `./mvnw` o instalar Maven con el gestor del sistema:

```bash
# Ubuntu / Debian
sudo apt update
sudo apt install maven

# macOS con Homebrew
brew install maven
```

### 🐳 Instalar Docker Desktop

Para el arranque automático, instala [Docker Desktop para Windows](https://docs.docker.com/desktop/setup/install/windows-install/)
con el backend de **WSL 2** y utiliza contenedores Linux. Sigue los requisitos
del instalador para tu versión de Windows, incluida la virtualización.
Si necesitas instalar WSL, ejecuta PowerShell como administrador:

```powershell
wsl --install
```

Reinicia si se solicita, abre Docker Desktop y espera a que el motor esté
iniciado. En una nueva terminal comprueba:

```powershell
docker version
docker compose version
docker info
```

`docker info` debe comunicarse con el motor. Docker Compose viene incluido en
Docker Desktop. Solo el equipo que aloja MySQL necesita Docker en este recorrido.

### 💾 MySQL: con Docker o instalado en el sistema

Con `iniciar.ps1`, **no instales MySQL por separado**: Compose descarga MySQL 8.4,
crea la base y publica el puerto local `3307`. Los datos se conservan en un volumen.

Para ejecutar sin Docker, descarga **MySQL Community Server 8.4 LTS** desde
[MySQL](https://dev.mysql.com/downloads/mysql/8.4.html). En Windows utiliza el MSI
del servidor y MySQL Configurator, de acuerdo con la
[guía de instalación](https://dev.mysql.com/doc/refman/8.4/en/windows-installation.html).
Configura el puerto `3306`, una contraseña de root y el servidor como servicio
de Windows. MySQL Workbench es opcional; instalarlo solo no instala el servidor.

La [guía sin Docker](docs/ejecucion-sin-docker.md) incluye la creación del esquema,
el usuario de conexión, la compilación y el arranque de ambas aplicaciones.

---

## Compilación y Ejecución

Los comandos usan PowerShell desde la raíz del repositorio.

### 🚀 Inicio automático en Windows

Con **JDK 17 o superior** instalado y **Docker Desktop en ejecución**, abre
PowerShell en la raíz del repositorio y ejecuta:

```powershell
.\iniciar.ps1
```

El script configura los archivos locales cuando no existen, inicia MySQL,
compila `common`, `backend` y `frontend`, crea el administrador si no existe y
abre JavaFX conectado al backend. Conserva las credenciales y los datos existentes.

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

# Mantener MySQL y backend activos sin abrir JavaFX
.\iniciar.ps1 -SoloBackend

# Iniciar con cinco ejemplos de cada entidad
.\iniciar.ps1 -DatosDemo

# Reutilizar la compilación anterior cuando no cambió el código
.\iniciar.ps1 -SinCompilar

# Usar otro puerto para el backend y su cliente
.\iniciar.ps1 -PuertoBackend 1235

# Mostrar los modos, opciones y ejemplos disponibles
.\iniciar.ps1 -h
```

Con `-SoloBackend`, mantén la terminal abierta y conecta los clientes mediante
`-SoloFrontend`. Al pulsar `Ctrl+C`, el script detiene el backend y MySQL,
conservando sus datos. Este modo admite `-SinCompilar`, `-PuertoBackend` y
`-Comprobar`; esta última opción comprueba el arranque y termina.
`-SoloBackend` y `-SoloFrontend` no se pueden combinar.

La ayuda también está disponible con `-Ayuda` o `-help`, sin iniciar servicios
ni comprobar Java o Docker. `-SinCompilar` reutiliza el arranque anterior;
omítelo después de cambiar el código.

Si PowerShell bloquea los scripts, puedes habilitar únicamente esta ejecución:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\iniciar.ps1
```

### Acceso inicial

El administrador se crea con **admin / admin** en una instalación nueva. La
clave inicial se guarda en `.config/admin.env`, excluido de Git. Si el usuario
ya existe o ese archivo define otra clave, se conserva su configuración.
Los funcionarios reciben su ID como clave inicial y pueden cambiarla desde la
aplicación. Consulta la [configuración del administrador](#configadminenv) para
personalizar su clave inicial.

### Datos de demostración opcionales

Para probar el sistema sin ingresar todo a mano, ejecuta:

```powershell
.\iniciar.ps1 -DatosDemo
```

Agrega **cinco funcionarios, cinco categorías, cinco recursos y cinco reservas
futuras**. También puedes usar `.\iniciar.ps1 -SoloBackend -DatosDemo`.
Sin esta opción no se cargan ejemplos. **Al cerrar la aplicación o detener
`-SoloBackend` con `Ctrl+C`, se eliminan solo los ejemplos creados por esa ejecución.**
Los datos que ya existían y los creados manualmente se conservan.
Si un dato manual utiliza un funcionario, recurso o categoría de demostración,
esa dependencia también se conserva y el script lo informa.

Los ejemplos que ya existían antes del arranque no se duplican ni se eliminan
al cerrar esa ejecución. Tampoco se restablecen sus claves o cancelaciones.

Ingresa con un funcionario entre **`900000001` y `900000005`**, usando su mismo
ID como clave inicial. Cada uno tiene una reserva; las nuevas se programan desde
mañana, una por día, de **09:00 a 10:00**. Estas reservas alimentan calendarios,
actividades y estadísticas. Para probar alertas, crea otra reserva con un usuario distinto.

La [guía de datos de demostración](docs/datos-demostracion.md) incluye los ejemplos,
las fechas para consultar y la carga mediante Maven, también sin Docker.

### Elegir otra forma de ejecución

| Modalidad | Instrucciones |
| --- | --- |
| Script automático con MySQL en Docker | Inicio automático de esta sección |
| Solo servidor y MySQL en Docker, sin interfaz | `.\iniciar.ps1 -SoloBackend` |
| MySQL en Docker y procesos iniciados por separado | [Ejecución manual con Docker](docs/ejecucion-manual.md) |
| MySQL instalado en el sistema, sin Docker | [Compilación y ejecución sin Docker](docs/ejecucion-sin-docker.md) |

---

## Otros clientes y alertas

### Otro frontend en la misma computadora

Mantén la primera aplicación abierta con `iniciar.ps1`. Desde **otra terminal
PowerShell**, en la raíz del mismo repositorio, abre otra ventana:

```powershell
.\iniciar.ps1 -SoloFrontend
```

Esta opción reutiliza la compilación del primer arranque y conecta el nuevo
frontend al backend en `127.0.0.1:1234`. No inicia ni detiene backend o MySQL,
y puedes repetir el comando en más terminales para abrir otros clientes.

Si el backend usa otro puerto:

```powershell
.\iniciar.ps1 -SoloFrontend -PuertoBackend 1235
```

Para comprobar únicamente que el puerto del backend acepta conexiones:

```powershell
.\iniciar.ps1 -SoloFrontend -Comprobar
```

### 🌐 Conectar dos computadoras en la misma red

Ambas computadoras deben tener la misma versión del proyecto. Ejecuta los
comandos desde la raíz del repositorio en PowerShell.

**En la computadora que ejecutará MySQL y el backend**, inicia Docker Desktop
y el servidor:

```powershell
.\iniciar.ps1 -SoloBackend
```

Mantén esa terminal abierta. En otra terminal del mismo equipo, ejecuta:

```powershell
ipconfig
```

Busca la **dirección IPv4** del adaptador Wi-Fi o Ethernet conectado a la red.
Por ejemplo, `192.168.1.20`. Esa será la dirección del backend para el otro equipo.

**En la segunda computadora**, necesitas el repositorio y un JDK 17 o superior
(JDK 21 recomendado). No necesitas instalar MySQL ni ejecutar Docker.
En el primer uso, o después de actualizar el código, compila e instala los módulos:

```powershell
.\mvnw.cmd install -DskipTests
```

Abre el frontend indicando la IP del primer equipo:

```powershell
.\iniciar.ps1 -SoloFrontend -ServidorBackend 192.168.1.20 -PuertoBackend 1234
```

Sustituye `192.168.1.20` por la IPv4 real. Si configuraste otro puerto para el
backend, utiliza ese mismo puerto en el cliente. `localhost` y `127.0.0.1`
apuntan a la propia computadora, por lo que no sirven para localizar el otro equipo.

Si Windows solicita acceso para Java en el equipo del backend, permite la
conexión en **redes privadas**. Para comprobar la comunicación, consulta
[Problemas de conexión](#-problemas-de-conexión).

Para utilizar también una interfaz en el equipo servidor, abre otra terminal y
ejecuta `.\iniciar.ps1 -SoloFrontend`. Ingresa con usuarios distintos en ambos
clientes y sigue la prueba de alertas de la sección siguiente.

### 🪟 Abrir otro frontend y probar las alertas

Para probar las alertas con dos ventanas:

1. En la primera, ingresa como administrador y crea un funcionario activo,
   una categoría y un recurso disponible de esa categoría.
2. En la segunda, ingresa con ese funcionario; su contraseña inicial es su ID.
3. Crea una reserva con una categoría que tenga recursos disponibles en el horario elegido.
4. Comprueba que en la primera ventana aparezca una tarjeta con la actividad,
   funcionario y horario, y que aumente el contador de alertas.
5. Usa **Ver detalle** para consultar la reserva y la **×** para descartarla.
   Descartar el aviso solo lo retira de la lista local; la reserva se conserva.

Las ventanas deben utilizar **usuarios distintos**: el servidor no notifica al
mismo usuario que creó la reserva. Las alertas son temporales y se reciben
mientras el cliente está conectado.

El panel de alertas permanece visible a la derecha al cambiar de pantalla.
Al recibir una reserva aparece un aviso temporal de seis segundos; **Ver alerta**
enfoca la tarjeta más reciente del panel. Para abrir sus datos, utiliza **Ver detalle**.

Si se pierde la conexión de alertas, el panel muestra el aviso y el botón
**Reconectar alertas**, conservando las tarjetas recibidas. Si se reinició el
backend, vuelve a iniciar sesión. El cierre de sesión no genera este aviso.

Cierra primero los frontends adicionales y al final la aplicación iniciada sin
`-SoloFrontend`: esta última detiene backend y MySQL cuando se cierra.
Si iniciaste con `-SoloBackend`, detén ese script con `Ctrl+C` al terminar.

---

## Configuración

`iniciar.ps1` genera los archivos locales que no existen. Para configurarlos
a mano, utiliza las plantillas de `.config/`. Los ejemplos siguientes muestran
qué escribir en cada archivo.

### `.config/docker.env`

Docker Compose utiliza este archivo para crear MySQL. Si ejecutas `iniciar.ps1`,
se genera automáticamente con contraseñas locales. Para configurarlo manualmente,
copia `.config/docker.env.example` como `.config/docker.env` y completa:

```env
MYSQL_PORT=3307
MYSQL_PASSWORD=tu_clave_de_reservas_app
MYSQL_ROOT_PASSWORD=tu_clave_de_root
```

* **`MYSQL_PORT`**: Puerto de MySQL en tu computadora; el contenedor utiliza internamente el puerto `3306`.
* **`MYSQL_PASSWORD`**: Contraseña del usuario MySQL `reservas_app`, utilizado por el backend.
* **`MYSQL_ROOT_PASSWORD`**: Contraseña del administrador de MySQL, independiente del usuario `admin` de la aplicación.

Estas contraseñas se establecen al inicializar un volumen vacío. Editar el archivo
después no cambia las credenciales de una base existente.

### `.config/database.env`

El backend utiliza este archivo para conectarse a MySQL. `iniciar.ps1` lo genera
con los datos de Docker cuando no existe. Para configurarlo manualmente, copia
`.config/database.docker.env.example` como `.config/database.env` y completa:

```env
DB_URL=jdbc:mysql://127.0.0.1:3307/sistema_reservas
DB_USER=reservas_app
DB_PASSWORD=tu_clave_de_reservas_app
DB_LOG_SQL=false
```

* **`DB_URL`**: Dirección JDBC de la base. El puerto debe coincidir con `MYSQL_PORT` de `.config/docker.env`.
* **`DB_USER`**: Usuario MySQL de conexión; Docker crea `reservas_app`.
* **`DB_PASSWORD`**: Debe ser exactamente la misma contraseña que `MYSQL_PASSWORD`.
* **`DB_LOG_SQL`**: Activa el registro SQL con `true`. Es opcional y está desactivado por defecto.

Para MySQL instalado directamente en el sistema, utiliza como plantilla
`.config/database.env.example` y ajusta el puerto, usuario y contraseña a esa
instalación. El script de inicio automático utiliza la configuración de Docker.

#### Registro de consultas SQL

Para guardar las consultas del backend, agrega `DB_LOG_SQL=true` en
`.config/database.env` y vuelve a iniciar el servidor. También puedes activarlo
para los procesos de una terminal:

```powershell
$env:DB_LOG_SQL = 'true'
.\iniciar.ps1 -SoloBackend
```

El archivo se guarda en **`target/local/backend-sql.log`**, tanto con MySQL en
Docker como con una instalación propia. Tras realizar una operación en la
aplicación, puedes seguirlo desde otra terminal:

```powershell
Get-Content .\target\local\backend-sql.log -Tail 50 -Wait
```

Cada entrada contiene fecha UTC, duración y resultado o código de error, junto
con la estructura de la consulta. Los parámetros y literales se mantienen como
`?`; no se guardan contraseñas, hashes ni resultados de las consultas. También
se registran `COMMIT` y `ROLLBACK`.

El log se agrega al archivo existente y está excluido de Git por estar dentro
de `target/`. `clean` elimina ese directorio. Para desactivar el registro,
configura `DB_LOG_SQL=false` y reinicia el backend; si utilizaste la variable
de entorno, cámbiala también o elimínala con `Remove-Item Env:DB_LOG_SQL`.
La [guía del registro SQL](docs/registro-sql.md) explica el formato y su alcance.

### `.config/admin.env`

El script de inicio utiliza este archivo para crear el administrador de la
aplicación cuando no existe. Puedes copiar `.config/admin.env.example` como
`.config/admin.env`; si no existe, el script lo genera con este contenido:

```env
APP_ADMIN_PASSWORD=admin
```

* **`APP_ADMIN_PASSWORD`**: Contraseña inicial del usuario de aplicación `admin`. Acepta `admin` o una contraseña personalizada de al menos seis caracteres.

El acceso inicial por defecto es **admin / admin**. Cambiar este archivo no
restablece la contraseña de un administrador existente; el cambio de contraseña
se realiza desde la aplicación.

Los tres archivos `.config/*.env` anteriores están excluidos de Git. Las
plantillas `.example` documentan su estructura y se incluyen en el repositorio.
La configuración de Groq es independiente y no es necesaria para iniciar sesión.

### `.env` — asistente de IA

Para utilizar el asistente, crea `.env` junto al `pom.xml` de la raíz. El backend
lee esta configuración; el frontend no necesita la clave de Groq.

```env
GROQ_API_KEY=tu_api_key_de_groq_aqui
GROQ_URL=https://api.groq.com/openai/v1/chat/completions
GROQ_MODEL=identificador_del_modelo_disponible_en_tu_cuenta
```

#### Descripción de Variables

- **`GROQ_API_KEY`**: Clave de autenticación personal obtenida en la plataforma de Groq.
- **`GROQ_URL`**: Endpoint oficial de la API de Groq consumido por la aplicación.
- **`GROQ_MODEL`**: Identificador del modelo de lenguaje utilizado por el sistema.

> **Nota Importante:** Se recomienda emplear modelos de la familia **Qwen**
> disponibles en Groq por su excelente desempeño en la extracción estructurada
> de datos. Como los catálogos cambian con el tiempo, verifica el modelo activo
> en tu cuenta de Groq antes de ejecutar. El archivo `.env` está excluido de Git
> y **nunca debe subirse al control de versiones**.

Obtén la clave desde [Groq Console](https://console.groq.com/keys) y copia en
`GROQ_MODEL` el identificador exacto de un [modelo disponible](https://console.groq.com/docs/models).
El valor del ejemplo es un marcador que debes reemplazar. La IA es opcional
para iniciar sesión y crear reservas manualmente. Reinicia el backend después
de cambiar su configuración de Groq.

### Conexión del frontend

Con el script, usa `-ServidorBackend` y `-PuertoBackend`. Si ejecutas JavaFX
manualmente, define las variables en esa terminal antes de iniciarlo:

```powershell
$env:SERVER_HOST = "IP_DEL_EQUIPO_BACKEND"
$env:SERVER_PORT = "1234"
```

El frontend solo necesita el destino del backend. Las credenciales de MySQL y
Groq corresponden al servidor.

---

## Pruebas

Los objetivos Maven habituales funcionan desde la raíz, con JDK 17 o superior
(JDK 21 recomendado). Si tienes Maven instalado, puedes usar `mvn` en lugar de
`.\mvnw.cmd`; ambos leen el mismo `pom.xml`.

```powershell
# Compilar y ejecutar todas las pruebas unitarias; no requiere MySQL ni Docker
.\mvnw.cmd clean test

# Iniciar una base MySQL exclusiva para integración
docker compose -f compose.test.yaml up -d --wait

# Ejecutar todas las pruebas unitarias e integraciones locales
.\mvnw.cmd clean verify

# Detener la base de pruebas conservando su volumen
docker compose -f compose.test.yaml stop
```

| Comando Maven | Alcance |
| --- | --- |
| `mvn clean test` | Limpia, compila y ejecuta las pruebas unitarias con Surefire |
| `mvn clean package` | Añade la generación de los JAR |
| `mvn clean verify` | Añade las integraciones con Failsafe; requiere MySQL de pruebas |
| `mvn clean install` | Ejecuta lo anterior e instala los artefactos en el repositorio Maven local |
| `mvn clean verify -DskipITs` | Ejecuta unitarias y genera los JAR, omitiendo únicamente las integraciones |

`compose.test.yaml` utiliza el proyecto Docker **eif206-tests**, el puerto **3308**
y un volumen propio. Inicializa el esquema sin cargar `seed.sql`: cada suite crea
y elimina sus datos. No utiliza los archivos `.config/docker.env` ni
`.config/database.env` de desarrollo. No ejecutes las integraciones contra una
base con datos personales.

Failsafe establece `DB_URL`, `DB_USER` y `DB_PASSWORD` para sus procesos de prueba.
Para utilizar otra base exclusiva, configura las propiedades Maven
`-Dtest.db.url=...`, `-Dtest.db.user=...` y `-Dtest.db.password=...`.

Las pruebas unitarias de IA utilizan HTTP simulado. Las llamadas reales a Groq
están etiquetadas como `ia-externa` y se incluyen solamente con el perfil opcional
`mvn clean verify -Pia`, con la configuración de Groq correspondiente.

Las integraciones de reservas prueban dos funcionarios enviando peticiones
simultáneas: con un solo recurso se guarda una reserva; con dos recursos se
asigna uno distinto a cada funcionario. También verifican las alertas de reservas
confirmadas y el rollback cuando falla el guardado de una asignación.

### 🧪 Integración continua en GitHub Actions

El workflow **Tests** (`.github/workflows/tests.yml`) se ejecuta en los pushes y
pull requests dirigidos a `main` y `develop`. También puede iniciarse desde
**Actions → Tests → Run workflow**.

Utiliza JDK 21, Maven Wrapper y `compose.test.yaml`. Ejecuta `clean verify` sin
filtros de clases: incluye todas las pruebas unitarias y las integraciones de
usuarios, funcionarios, categorías, recursos, reservas, calendarios, estadísticas,
login y alertas. Las suites del servidor y del proxy verifican además permisos y
comunicación por sockets.

Las llamadas externas de IA requieren el perfil opcional y quedan fuera del CI
normal. No se necesitan secretos de Groq ni archivos de configuración personales.
Los reportes se publican como artefacto **reportes-pruebas** y MySQL de CI se
elimina al terminar la ejecución.

---

## 🛠️ Problemas de conexión

Desde el equipo cliente, sustituye la IP y el puerto por los del backend:

```powershell
Test-NetConnection 192.168.1.20 -Port 1234
```

`TcpTestSucceeded : True` confirma que el puerto es accesible. Si devuelve
`False`, revisa que el backend esté abierto, la IP, el puerto y las reglas del
firewall del servidor para Java o ese puerto TCP. La red debe permitir conexiones
entre ambos equipos.

| Mensaje o síntoma | Comprobación |
| --- | --- |
| Java 8 o `Unrecognized option: --module-path` | Revisar `JAVA_HOME` y la salida de `.\mvnw.cmd -version` |
| No se puede conectar con Docker | Abrir Docker Desktop y comprobar que el motor esté iniciado |
| MySQL no aparece `healthy` | Consultar `docker compose --env-file .config/docker.env logs mysql` |
| `Access denied` de MySQL | Comparar `DB_PASSWORD` con la contraseña usada al crear el volumen; editar un archivo no cambia las credenciales de una base existente |
| No se pudo comunicar con el servidor | Iniciar primero el backend y comprobar host y puerto `1234` |
| Credenciales incorrectas | Usar el usuario de aplicación `admin`, no `reservas_app`, y la contraseña con la que se creó el administrador |
| Dependencia `common` o `backend` no encontrada | Ejecutar `.\mvnw.cmd install -DskipTests` desde la raíz |

---

## Funcionalidades

| Área | Funciones |
| --- | --- |
| 🔐 Usuarios | Login, roles de administrador y funcionario, cambio de contraseña y hashes en MySQL |
| 📅 Reservas | Creación, consulta, asignación de recursos disponibles y cancelación de reservas propias futuras |
| 🏢 Mantenimiento | Gestión de funcionarios, categorías y recursos |
| 🗓️ Calendarios | Disponibilidad por recurso y programación semanal de actividades |
| 📊 Estadísticas | Consultas por categoría y semana, gráficos y exportación a PDF |
| 🔔 Alertas | Reservas de otros usuarios, detalle en ventana emergente y descarte local con confirmación |
| 🤖 Asistente de IA | Extracción de actividad, fecha, horario y categorías desde texto mediante Groq |

El [recorrido funcional](docs/pruebas-funcionales.md) explica cómo comprobar
estas funciones con varios clientes.

---

## Arquitectura y módulos

El frontend utiliza MVC con JavaFX, FXML y CSS. `ServiceProxy` comunica las
peticiones por sockets y recibe las alertas del servidor. El backend concentra
los servicios, las reglas de negocio y el acceso a MySQL mediante JDBC.
Los modelos y el protocolo pertenecen a `common`.

```text
Frontend (JavaFX + ServiceProxy) ── sockets ── Backend ── JDBC ── MySQL
             │                                  │
             └────────── common ────────────────┘
```

`common` es una biblioteca: se compila con el proyecto y no se ejecuta como
aplicación. El frontend no se conecta a MySQL ni incluye el backend en su runtime;
utiliza este último únicamente en las pruebas de integración.

| Módulo | Responsabilidad y documentación |
| --- | --- |
| [`frontend`](frontend/README.md) | Presentación, navegación y reportes PDF con PDFBox y EasyTable |
| [`backend`](backend/README.md) | Servidor, servicios, validaciones, IA y repositorios MySQL |
| [`common`](common/README.md) | Modelos, protocolo y serialización compartidos |
| [`database`](database/README.md) | MER, esquema MySQL y catálogo de demostración |

### Estructura del repositorio

```text
pom.xml                         # POM padre: agrega los tres módulos
mvnw / mvnw.cmd                 # Maven Wrapper
iniciar.ps1                     # Arranque y cierre local de JavaFX, backend y MySQL
compose.yaml                    # MySQL de desarrollo con volumen persistente
compose.test.yaml               # MySQL exclusivo para integración
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
    └── test/                   # Pruebas de presentación, proxy y servidor
database/                       # MER DBML, esquema MySQL y catálogo de ejemplo
docs/                           # Arquitectura, ejecución manual y pruebas funcionales
```

---

## Documentación

| Guía | Contenido |
| --- | --- |
| [Ejecución manual con Docker](docs/ejecucion-manual.md) | MySQL en un contenedor y arranque de cada proceso por separado |
| [Ejecución sin Docker](docs/ejecucion-sin-docker.md) | Instalación de MySQL, esquema, configuración, compilación y arranque |
| [Pruebas funcionales](docs/pruebas-funcionales.md) | Recorrido de pantallas, reportes y alertas |
| [Datos de demostración](docs/datos-demostracion.md) | Carga opcional de cinco ejemplos por entidad y credenciales de prueba |
| [Registro SQL](docs/registro-sql.md) | Activación, lectura y alcance del log de consultas del backend |
| [Arquitectura](docs/organizacion-proyecto-02.md) | Dependencias y distribución de clases |
| [Base de datos](database/README.md) | Modelo DBML y scripts de creación |
| [Docker y persistencia](database/DOCKER.md) | MySQL, volúmenes y credenciales de Docker |
| [Usuarios y credenciales](database/USUARIOS.md) | Cuentas de MySQL y usuarios de la aplicación |
| [Protocolo de sockets](docs/protocolo-autenticacion.md) | Operaciones, sesiones y notificaciones |
