[CmdletBinding(DefaultParameterSetName = 'Completo')]
param(
    [switch]$Comprobar,
    [switch]$SinCompilar,
    [Parameter(ParameterSetName = 'Completo')]
    [Parameter(ParameterSetName = 'Backend')][switch]$DatosDemo,
    [Parameter(ParameterSetName = 'Frontend')][switch]$SoloFrontend,
    [Parameter(ParameterSetName = 'Backend')][switch]$SoloBackend,
    [Parameter(ParameterSetName = 'Frontend')]
    [ValidateNotNullOrEmpty()][string]$ServidorBackend = '127.0.0.1',
    [Alias('h', 'help')][switch]$Ayuda,
    [ValidateRange(1, 65535)][int]$PuertoBackend = 1234
)

if ($Ayuda) {
    Write-Output @'
Sistema de Reserva de Recursos - Inicio local

Uso: .\iniciar.ps1 [opciones]

Sin opciones: inicia MySQL en Docker, compila los modulos, crea el administrador
si no existe, inicia el backend y abre JavaFX. Al cerrar JavaFX detiene los
servicios que administra este script y conserva los datos de MySQL.

Opciones:
  -SoloBackend       Inicia MySQL y backend sin abrir JavaFX. Mantiene la terminal
                     abierta; Ctrl+C detiene ambos servicios y conserva los datos.
  -SoloFrontend      Conecta otro frontend a un backend existente. No administra
                     MySQL ni el servidor. Requiere los modulos ya compilados.
  -ServidorBackend   Host o IP del servidor, con -SoloFrontend. Por defecto: 127.0.0.1.
  -PuertoBackend     Puerto TCP del backend. Por defecto: 1234. Rango: 1 a 65535.
  -SinCompilar       Reutiliza la compilacion y el classpath del arranque anterior.
                     Se aplica al inicio completo o con -SoloBackend.
  -DatosDemo         Agrega 5 funcionarios, categorias, recursos y reservas futuras.
                     Al cerrar elimina solo los ejemplos creados por esta ejecucion.
                     Conserva datos manuales y las dependencias que estos necesiten.
  -Comprobar         Comprueba el arranque y termina sin abrir JavaFX; detiene
                     MySQL y backend al finalizar. Con -SoloFrontend, solo
                     comprueba que el puerto acepte conexiones.
  -Ayuda, -h, -help   Muestra esta ayuda sin iniciar servicios ni requerir Java.

-SoloBackend y -SoloFrontend son modos excluyentes.

Ejemplos:
  .\iniciar.ps1
  .\iniciar.ps1 -SoloBackend
  .\iniciar.ps1 -SoloBackend -SinCompilar -PuertoBackend 1235
  .\iniciar.ps1 -SoloBackend -Comprobar
  .\iniciar.ps1 -DatosDemo
  .\iniciar.ps1 -SoloBackend -DatosDemo
  .\iniciar.ps1 -SoloFrontend
  .\iniciar.ps1 -SoloFrontend -ServidorBackend 192.168.1.20 -PuertoBackend 1234
  .\iniciar.ps1 -SoloFrontend -Comprobar
  .\iniciar.ps1 -h

Requisitos: JDK 17+ (21 recomendado) y Docker Desktop para iniciar MySQL.
Configuracion: .config/docker.env, .config/database.env y .config/admin.env.
IA opcional: .env. Instrucciones completas en README.md y docs/.
Registros del backend: target/local/backend.log y backend-error.log.
'@
    return
}

if ($PSCmdlet.ParameterSetName -eq 'Frontend' -and -not $SoloFrontend) {
    throw 'Use -ServidorBackend junto con -SoloFrontend. Consulte .\iniciar.ps1 -h.'
}

$ErrorActionPreference = 'Stop'
$root = $PSScriptRoot
$backend = $null
$demoRegistro = $null
$mysqlManaged = $false
$lock = $null
$variables = @('JAVA_HOME', 'PATH', 'DB_URL', 'DB_USER', 'DB_PASSWORD',
    'MYSQL_PORT', 'MYSQL_PASSWORD', 'MYSQL_ROOT_PASSWORD',
    'APP_ADMIN_PASSWORD', 'SERVER_HOST', 'SERVER_PORT')
$original = @{}
foreach ($name in $variables) { $original[$name] = [Environment]::GetEnvironmentVariable($name, 'Process') }

function New-Password {
    $bytes = New-Object byte[] 24
    $rng = [Security.Cryptography.RandomNumberGenerator]::Create()
    try { $rng.GetBytes($bytes); return [Convert]::ToBase64String($bytes) }
    finally { $rng.Dispose() }
}

function Read-Config([string]$Path) {
    $values = @{}
    foreach ($line in Get-Content -LiteralPath $Path) {
        if ($line -match '^\s*([A-Z_]+)\s*=(.*)$') {
            $values[$matches[1]] = $matches[2].Trim().Trim('"').Trim("'")
        }
    }
    return $values
}

function Invoke-Maven([string[]]$Arguments) {
    & "$root/mvnw.cmd" @Arguments
    if ($LASTEXITCODE -ne 0) { throw 'Maven no pudo completar el comando.' }
}

function Invoke-Compose([string[]]$Arguments) {
    & docker compose --env-file "$root/.config/docker.env" @Arguments
    if ($LASTEXITCODE -ne 0) { throw 'Docker Compose no pudo completar el comando.' }
}

try {
    Push-Location $root
    $candidates = @($env:JAVA_HOME)
    if (Get-Command java -ErrorAction SilentlyContinue) {
        $candidates += Split-Path (Split-Path (Get-Command java).Source)
    }
    $candidates += @(Get-ChildItem "$env:USERPROFILE/.jdks" -Directory -ErrorAction SilentlyContinue |
        Sort-Object @{ Expression = { $_.Name -match '21' }; Descending = $true } |
        Select-Object -ExpandProperty FullName)
    $jdk = $null
    foreach ($candidate in $candidates) {
        if ($candidate -and (Test-Path "$candidate/bin/javac.exe") -and (Test-Path "$candidate/release")) {
            $release = Get-Content "$candidate/release" -Raw
            if ($release -match 'JAVA_VERSION="(\d+)' -and [int]$matches[1] -ge 17) {
                $jdk = $candidate
                break
            }
        }
    }
    if (-not $jdk) { throw 'Instale un JDK 17 o superior y configure JAVA_HOME. Se recomienda JDK 21.' }
    $env:JAVA_HOME = $jdk
    $env:PATH = "$jdk/bin;$env:PATH"
    Write-Host "Usando JDK: $jdk"

    if ($SoloFrontend) {
        $env:SERVER_HOST = $ServidorBackend
        $env:SERVER_PORT = "$PuertoBackend"
        $cliente = New-Object Net.Sockets.TcpClient
        try {
            $conexion = $cliente.ConnectAsync($ServidorBackend, $PuertoBackend)
            if (-not $conexion.Wait(3000)) { throw 'Tiempo de espera agotado.' }
        } catch {
            throw "No se pudo conectar a ${ServidorBackend}:$PuertoBackend. Inicie primero el backend con .\iniciar.ps1 o revise la direccion y el puerto."
        } finally {
            $cliente.Dispose()
        }
        Write-Host "Backend disponible en ${ServidorBackend}:$PuertoBackend."
        if (-not $Comprobar) {
            Write-Host 'Abriendo otro frontend. Al cerrarlo, el backend y MySQL seguiran funcionando.'
            Invoke-Maven @('-pl', 'frontend', 'javafx:run')
        }
        return
    }

    New-Item -ItemType Directory -Force target/local | Out-Null
    $lock = [IO.File]::Open("$root/target/local/iniciar.lock", 'OpenOrCreate', 'ReadWrite', 'None')

    if (-not (Get-Command docker -ErrorAction SilentlyContinue)) { throw 'Instale Docker Desktop y vuelva a abrir PowerShell.' }
    & docker info *> $null
    if ($LASTEXITCODE -ne 0) { throw 'Abra Docker Desktop, espere a que arranque el motor y ejecute nuevamente el script.' }

    $probe = New-Object Net.Sockets.TcpListener([Net.IPAddress]::Any, $PuertoBackend)
    $probe.Server.ExclusiveAddressUse = $true
    try { $probe.Start() }
    catch { throw "El puerto $PuertoBackend esta ocupado. Cierre el backend anterior o use -PuertoBackend con otro puerto." }
    finally { $probe.Stop() }

    New-Item -ItemType Directory -Force .config | Out-Null
    if (-not (Test-Path .config/docker.env)) {
        @("MYSQL_PORT=3307", "MYSQL_PASSWORD=$(New-Password)", "MYSQL_ROOT_PASSWORD=$(New-Password)") |
            Set-Content .config/docker.env -Encoding ASCII
    }
    $mysql = Read-Config .config/docker.env
    if (-not $mysql['MYSQL_PORT']) { $mysql['MYSQL_PORT'] = '3307' }
    foreach ($name in @('MYSQL_PASSWORD', 'MYSQL_ROOT_PASSWORD')) {
        if (-not $mysql[$name]) { throw "Falta $name en .config/docker.env." }
    }
    foreach ($name in $mysql.Keys) {
        if ($name -in $variables) { [Environment]::SetEnvironmentVariable($name, $mysql[$name], 'Process') }
    }
    $dbUrl = "jdbc:mysql://127.0.0.1:$($mysql['MYSQL_PORT'])/sistema_reservas"
    if (-not (Test-Path .config/database.env)) {
        @("DB_URL=$dbUrl", 'DB_USER=reservas_app', "DB_PASSWORD=$($mysql['MYSQL_PASSWORD'])") |
            Set-Content .config/database.env -Encoding ASCII
    }
    $database = Read-Config .config/database.env
    if ($database['DB_URL'] -ne $dbUrl -or $database['DB_USER'] -ne 'reservas_app' -or
        $database['DB_PASSWORD'] -cne $mysql['MYSQL_PASSWORD']) {
        throw 'La configuracion existente de database.env no coincide con Docker. Revise los pasos manuales del README; no se sobrescribieron sus archivos.'
    }
    foreach ($name in @('DB_URL', 'DB_USER', 'DB_PASSWORD')) {
        [Environment]::SetEnvironmentVariable($name, $database[$name], 'Process')
    }
    $env:SERVER_HOST = '127.0.0.1'
    $env:SERVER_PORT = "$PuertoBackend"

    Write-Host 'Iniciando MySQL...'
    $mysqlManaged = $true
    Invoke-Compose @('up', '-d', '--wait', 'mysql')
    if (-not $SinCompilar) {
        Invoke-Maven @('install', '-DskipTests', '--batch-mode')
        Invoke-Maven @('-pl', 'backend', 'org.apache.maven.plugins:maven-dependency-plugin:3.7.0:build-classpath',
            '-Dmdep.outputFile=target/runtime-classpath.txt', '--batch-mode')
    }
    $classpathFile = "$root/backend/target/runtime-classpath.txt"
    if (-not (Test-Path $classpathFile)) { throw 'Ejecute primero el script sin -SinCompilar.' }

    $query = 'SELECT COUNT(*) FROM usuarios WHERE id_usuario = ''admin'';'
    $adminCount = $query | & docker compose --env-file "$root/.config/docker.env" exec -T mysql sh -c `
        'MYSQL_PWD="$MYSQL_PASSWORD" mysql -N -B -u "$MYSQL_USER" "$MYSQL_DATABASE"'
    if ($LASTEXITCODE -ne 0) { throw 'No se pudo consultar el administrador en MySQL.' }
    if ("$adminCount".Trim() -notmatch '^[01]$') { throw 'Respuesta inesperada al consultar el administrador.' }
    if ("$adminCount".Trim() -eq '0') {
        if (-not (Test-Path .config/admin.env)) {
            'APP_ADMIN_PASSWORD=admin' | Set-Content .config/admin.env -Encoding ASCII
        }
        $env:APP_ADMIN_PASSWORD = (Read-Config .config/admin.env)['APP_ADMIN_PASSWORD']
        if (-not $env:APP_ADMIN_PASSWORD) { throw 'Falta APP_ADMIN_PASSWORD en .config/admin.env.' }
        Invoke-Maven @('-pl', 'backend', 'exec:java', '-Dexec.mainClass=una.proyecto.bootstrap.CrearAdministrador', '--batch-mode')
        Write-Host 'Administrador creado. Usuario: admin. Clave inicial en .config/admin.env (por defecto: admin).'
    } else {
        Write-Host 'Se conserva el administrador existente y su clave.'
    }

    $classpath = "$root/backend/target/classes;" + (Get-Content $classpathFile -Raw).Trim()
    if ($DatosDemo) {
        $demoRegistro = Join-Path $root "target/local/demo-$([guid]::NewGuid().ToString('N')).properties"
        & "$jdk/bin/java.exe" -cp $classpath una.proyecto.bootstrap.CargarDemostracion cargar $demoRegistro
        if ($LASTEXITCODE -ne 0) { throw 'No se pudieron cargar los ejemplos de demostracion.' }
        if (-not (Test-Path -LiteralPath $demoRegistro)) { throw 'No se genero el registro de demostracion. Ejecute sin -SinCompilar.' }
        Write-Host 'Los ejemplos nuevos de esta ejecucion se eliminaran al cerrar; sus datos manuales se conservaran.'
    }

    $stdout = "$root/target/local/backend.log"
    $stderr = "$root/target/local/backend-error.log"
    $backend = Start-Process -FilePath "$jdk/bin/java.exe" -ArgumentList @('-cp', "`"$classpath`"", 'una.proyecto.server.BackendApp') `
        -WorkingDirectory $root -WindowStyle Hidden -PassThru -RedirectStandardOutput $stdout -RedirectStandardError $stderr
    $ready = $false
    for ($attempt = 0; $attempt -lt 60; $attempt++) {
        if ($backend.HasExited) { throw "El backend termino. Revise $stdout y $stderr." }
        if ((Get-Content $stdout -Raw -ErrorAction SilentlyContinue) -match "Servidor escuchando en el puerto $PuertoBackend\b") {
            $ready = $true
            break
        }
        Start-Sleep -Seconds 1
    }
    if (-not $ready) { throw "El backend no arranco a tiempo. Revise $stdout y $stderr." }
    Write-Host "Backend conectado a MySQL y escuchando en $PuertoBackend."
    if ($SoloBackend -and -not $Comprobar) {
        Write-Host 'Solo backend activo. Use -SoloFrontend en otro equipo o terminal para conectarse.'
        Write-Host 'Presione Ctrl+C para detener este backend y MySQL, conservando sus datos.'
        while (-not $backend.HasExited) { Start-Sleep -Seconds 1 }
        if ($backend.ExitCode -ne 0) { throw "El backend termino con error. Revise $stdout y $stderr." }
    } elseif (-not $Comprobar) {
        Write-Host 'Abriendo JavaFX. Al cerrar la ventana se detendran este backend y MySQL.'
        Invoke-Maven @('-pl', 'frontend', 'javafx:run')
    }
} finally {
    try {
        try {
            if ($backend -and -not $backend.HasExited) { Stop-Process -Id $backend.Id -ErrorAction SilentlyContinue }
            if ($demoRegistro -and (Test-Path -LiteralPath $demoRegistro)) {
                Write-Host 'Eliminando los ejemplos creados por esta ejecucion...'
                try {
                    & "$jdk/bin/java.exe" -cp $classpath una.proyecto.bootstrap.CargarDemostracion limpiar $demoRegistro
                    if ($LASTEXITCODE -ne 0) { Write-Warning "No se completo la limpieza. Se conserva el registro: $demoRegistro" }
                } catch {
                    Write-Warning "No se completo la limpieza. Se conserva el registro: $demoRegistro. $($_.Exception.Message)"
                }
            }
        } finally {
            if ($mysqlManaged) {
                Write-Host 'Deteniendo MySQL y conservando sus datos...'
                & docker compose --env-file "$root/.config/docker.env" stop mysql
                if ($LASTEXITCODE -ne 0) { Write-Warning 'No se pudo detener MySQL. Use docker compose --env-file .config/docker.env stop mysql.' }
            }
        }
    } finally {
        if ($lock) { $lock.Dispose() }
        foreach ($name in $variables) {
            if ($null -eq $original[$name]) {
                Remove-Item -LiteralPath "Env:$name" -ErrorAction SilentlyContinue
            } else {
                [Environment]::SetEnvironmentVariable($name, $original[$name], 'Process')
            }
        }
        Pop-Location
    }
}
