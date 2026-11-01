# Registro de consultas SQL

El backend puede registrar las consultas ejecutadas mediante `Database`, tanto
en conexiones compartidas como en las conexiones propias de cada transacción.
Funciona con MySQL en Docker o instalado directamente, sin activar registros
globales en MySQL ni configurar librerías adicionales.

## Activación

Agrega esta variable a `.config/database.env`:

```env
DB_LOG_SQL=true
```

También puedes definirla en el entorno de la terminal que ejecutará el backend:

```powershell
$env:DB_LOG_SQL = 'true'
.\iniciar.ps1 -SoloBackend
```

La variable de entorno tiene prioridad sobre el archivo. Reinicia el backend
después de cambiarla. Si no se define o su valor es `false`, no se crea el log.
Las utilidades Java de creación del administrador y demostración también usan
esta configuración.

## Lectura

El archivo se crea al registrar la primera operación en
`target/local/backend-sql.log`, relativo al directorio de trabajo. Los comandos
del proyecto utilizan la raíz del repositorio. Realiza primero una operación
para que exista el archivo y síguelo desde PowerShell:

```powershell
Get-Content .\target\local\backend-sql.log -Tail 50 -Wait
```

Ejemplos de formato:

```text
2026-11-01T23:00:00Z | 2.451 ms | OK | SELECT nombre FROM usuarios WHERE id_usuario = ?
2026-11-01T23:00:01Z | 1.310 ms | OK filas=1 | INSERT INTO categorias (descripcion) VALUES (?)
2026-11-01T23:00:02Z | 0.915 ms | ERROR SQLState=23000 codigo=1062 | INSERT INTO recursos (id_recurso, id_categoria, descripcion) VALUES (?, ?, ?)
2026-11-01T23:00:03Z | 0.462 ms | OK | COMMIT
```

La fecha está en UTC (`Z`). La duración mide la llamada JDBC de ejecución,
incluida la comunicación con MySQL. No mide la lectura posterior de todas las
filas del resultado ni el tiempo exclusivo de procesamiento del servidor.
Las actualizaciones incluyen el número de filas afectadas y los lotes indican
cuántas operaciones contienen. Las consultas no consumen sus resultados para
contarlos. Se registran también los `COMMIT` y `ROLLBACK` explícitos.

## Contenido y alcance

- Se guarda la estructura SQL, conservando los parámetros como `?`.
- Los literales de texto y números se sustituyen por `?`, y se omiten los comentarios SQL.
- No se registran los valores enviados con `setString`, `setObject` u otros setters JDBC.
- Los errores incluyen `SQLState` y código del proveedor, conservando la excepción
  original para la aplicación. Su mensaje no se escribe en este archivo.
- No se guardan contraseñas, hashes ni filas devueltas por las consultas.

Este registro cubre el acceso JDBC del proyecto. No incluye las consultas de
Workbench, los comandos del cliente `mysql`, las comprobaciones de Docker ni
las operaciones internas que el driver realiza para administrar sus conexiones.
El SQL ocultado sirve para diagnóstico; no es un script para reconstruir la base.

El archivo acumula entradas entre ejecuciones y está excluido de Git por estar
en `target/`. `mvn clean` elimina los registros junto con los demás archivos
generados. Cada entrada cierra el archivo después de escribir; si no puede
escribirse, se muestra un aviso y la operación de base de datos continúa.

## Desactivación

Configura `DB_LOG_SQL=false` y vuelve a iniciar el backend. Si lo activaste en
la terminal, cambia también su variable o elimínala antes de reiniciar:

```powershell
Remove-Item Env:DB_LOG_SQL -ErrorAction SilentlyContinue
```

[Volver al README](../README.md) · [Backend](../backend/README.md)
