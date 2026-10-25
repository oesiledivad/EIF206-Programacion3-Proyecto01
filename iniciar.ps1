[CmdletBinding()]
param(
    [switch]$Comprobar,
    [switch]$SinCompilar,
    [ValidateRange(1, 65535)][int]$PuertoBackend = 1234
)

$ErrorActionPreference = 'Stop'
$root = $PSScriptRoot
$backend = $null
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
    New-Item -ItemType Directory -Force target/local | Out-Null
    $lock = [IO.File]::Open("$root/target/local/iniciar.lock", 'OpenOrCreate', 'ReadWrite', 'None')

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
    Invoke-Compose @('up', '-d', '--wait')
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
    if (-not $Comprobar) {
        Write-Host 'Abriendo JavaFX. Al cerrar la ventana se detendra este backend.'
        Invoke-Maven @('-pl', 'frontend', 'javafx:run')
    }
} finally {
    if ($backend -and -not $backend.HasExited) { Stop-Process -Id $backend.Id -ErrorAction SilentlyContinue }
    if ($lock) { $lock.Dispose() }
    foreach ($name in $variables) { [Environment]::SetEnvironmentVariable($name, $original[$name], 'Process') }
    Pop-Location
}
