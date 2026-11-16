# Compilación y ejecución sin Docker

Este recorrido utiliza **MySQL Community Server 8.4 instalado en el sistema**.
Frontend y backend se ejecutan como procesos Java separados. `common` se compila
como biblioteca compartida. No se utiliza `iniciar.ps1`, porque ese script
administra MySQL mediante Docker Compose.

Los comandos usan PowerShell desde la raíz del repositorio, con JDK 17 o superior
(JDK 21 recomendado) y Maven Wrapper. La [instalación de las herramientas](../README.md#requisitos)
se explica en el README. En Linux/macOS, utiliza `./mvnw` para los comandos Maven
y el cliente MySQL instalado en tu sistema.

## 1. Instalar y comprobar MySQL

Descarga [MySQL Community Server 8.4 LTS](https://dev.mysql.com/downloads/mysql/8.4.html).
En Windows, ejecuta el MSI del servidor y completa MySQL Configurator:

1. Configura la conexión TCP/IP en el puerto `3306`, o anota el puerto elegido.
2. Define y conserva la contraseña del administrador MySQL `root`.
3. Configura MySQL como servicio de Windows y arráncalo.
4. MySQL Workbench es opcional; puedes utilizar el cliente de línea de comandos.

Consulta la [guía oficial de instalación para Windows](https://dev.mysql.com/doc/refman/8.4/en/windows-installation.html)
si el instalador solicita componentes adicionales. Para localizar el servicio:

```powershell
Get-Service -Name '*MySQL*'
```

Si está detenido, inícialo desde **Servicios** de Windows o desde PowerShell
como administrador, usando su nombre real. Por ejemplo, si se llama `MySQL84`:

```powershell
Start-Service -Name MySQL84
```

Abre el cliente desde la raíz del repositorio. Ajusta la ruta si instalaste MySQL
en otra ubicación y el puerto si elegiste uno distinto:

```powershell
& "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe" --default-character-set=utf8mb4 -h 127.0.0.1 -P 3306 -u root -p
```

Introduce la contraseña de MySQL root cuando se solicite. Este usuario es
distinto del administrador `admin` de la aplicación.

## 2. Crear el esquema y el usuario del backend

Dentro del cliente MySQL, ejecuta:

```sql
SOURCE database/schema.sql;
USE sistema_reservas;
SHOW TABLES;
```

Deben aparecer seis tablas: `usuarios`, `funcionarios`, `categorias`, `recursos`,
`reservas` y `reserva_recurso`.

Crea la cuenta local que usará JDBC. Sustituye la contraseña del ejemplo por una
contraseña propia y utiliza el mismo valor en el archivo de configuración del
siguiente paso:

```sql
CREATE USER IF NOT EXISTS 'reservas_app'@'localhost'
    IDENTIFIED BY 'tu_clave_de_reservas_app';

GRANT SELECT, INSERT, UPDATE, DELETE ON sistema_reservas.*
    TO 'reservas_app'@'localhost';
```

`CREATE USER IF NOT EXISTS` conserva la contraseña de una cuenta existente.
Si ya utilizabas `reservas_app`, configura su contraseña actual.

El catálogo de demostración es opcional. Para cargar dos categorías y dos recursos:

```sql
SOURCE database/seed.sql;
EXIT;
```

Si no quieres cargar el catálogo, ejecuta únicamente `EXIT;`. Los scripts SQL
no crean usuarios de la aplicación; el administrador se crea en el paso 5.

## 3. Configurar la conexión JDBC

Crea `.config/database.env` desde la plantilla si todavía no existe:

```powershell
if (-not (Test-Path .config/database.env)) {
    Copy-Item .config/database.env.example .config/database.env
}
```

Edita `.config/database.env` con los datos de tu instalación:

```env
DB_URL=jdbc:mysql://127.0.0.1:3306/sistema_reservas
DB_USER=reservas_app
DB_PASSWORD=tu_clave_de_reservas_app
```

Si antes usabas Docker, actualiza el puerto y las credenciales del archivo
existente. Las variables de entorno `DB_URL`, `DB_USER` y `DB_PASSWORD` tienen
prioridad sobre el archivo: verifica que no apunten a otra base en las terminales
que utilizarás para ejecutar el backend.

En este recorrido no se necesitan `.config/docker.env` ni `.config/admin.env`.
El backend solo requiere `database.env`; Groq se configura de manera opcional
en [`.env`](../README.md#env--asistente-de-ia).

Comprueba que la cuenta del backend pueda consultar el esquema, introduciendo
su contraseña cuando se solicite:

```powershell
& "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe" -h 127.0.0.1 -P 3306 -u reservas_app -p -D sistema_reservas -e "SHOW TABLES;"
```

## 4. Compilar los módulos

```powershell
.\mvnw.cmd -version
.\mvnw.cmd install -DskipTests
```

El primer comando debe mostrar el JDK elegido. El segundo compila e instala
`common`, `backend` y `frontend` en el repositorio Maven local, sin ejecutar las
integraciones que utilizan la base exclusiva de pruebas.

## 5. Crear el administrador de la aplicación

Ejecuta desde la misma raíz, con la conexión JDBC configurada:

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

Puedes usar la clave inicial `admin` o una contraseña personalizada de al menos
seis caracteres. La utilidad guarda su hash en MySQL. Si `admin` ya existe,
informa que no lo modificó; utiliza la contraseña con la que se creó.

## 6. Iniciar el backend — terminal 1

```powershell
.\mvnw.cmd -pl backend exec:java
```

Mantén esa terminal abierta. Debe mostrar:

```text
Servidor escuchando en el puerto 1234
```

Para cambiar el puerto, define `$env:SERVER_PORT` antes de ejecutar el backend y
usa el mismo valor en los clientes.

## 7. Iniciar el frontend — terminal 2

Abre otra terminal en la raíz del repositorio y ejecuta:

```powershell
.\mvnw.cmd -pl frontend javafx:run
```

Ingresa con **ID `admin`** y la contraseña del paso 5. Debe abrirse el panel
principal. El frontend usa el backend local en `localhost:1234` por defecto;
no necesita credenciales de MySQL.

Para abrir otro cliente, ejecuta ese mismo comando en una tercera terminal.
Si el backend está en otra computadora o utiliza otro puerto, define antes:

```powershell
$env:SERVER_HOST = "IP_DEL_EQUIPO_BACKEND"
$env:SERVER_PORT = "1234"
.\mvnw.cmd -pl frontend javafx:run
```

La [prueba entre computadoras](../README.md#-conectar-dos-computadoras-en-la-misma-red)
explica cómo obtener la IP y comprobar el puerto. Usa usuarios distintos para
probar las alertas.

## 8. Verificar y detener

Sigue el [recorrido funcional](pruebas-funcionales.md) para probar las pantallas,
reservas, alertas y reportes. Las pruebas unitarias no necesitan Docker ni MySQL:

```powershell
.\mvnw.cmd clean test
```

El `clean verify` predeterminado utiliza una base exclusiva en el puerto `3308`,
configurada para `compose.test.yaml`. No lo dirijas a la base de uso diario.
La [sección de pruebas](../README.md#pruebas) explica esa configuración.

Para detener la ejecución, cierra los frontends y termina el backend con `Ctrl+C`.
MySQL seguirá funcionando como servicio. Si quieres detenerlo, utiliza
**Servicios** de Windows o `Stop-Service -Name MySQL84` desde PowerShell como
administrador, sustituyendo el nombre por el de tu instalación. Los datos se conservan.

[Volver al README](../README.md) · [Usuarios y credenciales](../database/USUARIOS.md)
