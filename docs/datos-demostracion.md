# Datos de demostración

La carga es opcional. El arranque habitual conserva la base sin agregar ejemplos.
Con `-DatosDemo`, se agregan cinco funcionarios, cinco categorías, cinco recursos
y cinco reservas con sus asignaciones. Las actividades y calendarios se obtienen
de esas reservas; no son tablas adicionales. El administrador del sistema es
independiente de estos cinco funcionarios.

## Cargar con el script

Desde la raíz del repositorio, con JDK y Docker Desktop disponibles:

```powershell
# Cargar los ejemplos y abrir la aplicación
.\iniciar.ps1 -DatosDemo

# Cargar los ejemplos y mantener solo el servidor
.\iniciar.ps1 -SoloBackend -DatosDemo

# Comprobar carga, arranque y limpieza sin abrir la interfaz
.\iniciar.ps1 -DatosDemo -Comprobar
```

El script prepara MySQL y el administrador, carga los ejemplos y después inicia
el backend. `-DatosDemo` no se combina con `-SoloFrontend`: los datos se cargan
una vez en el equipo del servidor y quedan disponibles para todos los clientes.

Al cerrar el frontend principal o detener `-SoloBackend` con `Ctrl+C`, el script
detiene su servidor, elimina sus ejemplos y después detiene MySQL. Con
`-Comprobar`, esa limpieza se hace al terminar la comprobación.

## Contenido

Estos nombres, identificaciones y teléfonos son ficticios. Los identificadores
`900000001` a `900000005` están reservados para la demostración.

| Usuario | Nombre | Categoría | Recurso | Actividad |
| --- | --- | --- | --- | --- |
| `900000001` | Ana Mora | DEMO - Sala de reuniones | `DEMO-SALA-001` | Reunión de coordinación |
| `900000002` | Luis Rojas | DEMO - Proyector | `DEMO-PROYECTOR-001` | Capacitación técnica |
| `900000003` | Sofía Vargas | DEMO - Equipo portátil | `DEMO-PORTATIL-001` | Planificación de equipo |
| `900000004` | Carlos Soto | DEMO - Auditorio | `DEMO-AUDITORIO-001` | Presentación de resultados |
| `900000005` | Elena Castro | DEMO - Laboratorio | `DEMO-LABORATORIO-001` | Taller de programación |

La **contraseña inicial de cada funcionario es su ID** y se almacena como hash.
Por ejemplo, `900000001` / `900000001`. Las actividades también llevan el prefijo
`DEMO -` para distinguirlas en las consultas.

Las reservas nuevas se crean desde **mañana hasta cinco días después de la
carga**, una por día, de **09:00 a 10:00**. Cada funcionario tiene una reserva
propia y cada reserva utiliza el recurso de su categoría.

## Probar la aplicación

1. Ingresa con el administrador para consultar funcionarios, categorías y recursos.
2. Abre otro frontend e ingresa con cualquiera de los cinco funcionarios.
3. En **Mis reservas**, consulta o cancela su reserva futura.
4. En **Calendario**, selecciona la fecha y categoría de una reserva de demostración.
5. En **Actividades**, carga la semana que contiene esa fecha. Si las reservas
   abarcan dos semanas, consulta ambas.
6. En **Estadísticas**, utiliza un período que incluya las fechas de las cinco reservas.
7. Crea una reserva nueva desde otro usuario para probar las alertas.

Las reservas precargadas no generan alertas retroactivas: el panel muestra las
reservas nuevas de otros usuarios recibidas durante la sesión actual.

## Repetir la carga

Cada ejecución registra los IDs de los ejemplos que realmente inserta en
`target/local/demo-<identificador>.properties`. Al cerrar, solo elimina esos
ejemplos, incluidas sus reservas canceladas. El administrador `admin` se conserva.
El registro se elimina después de completar la limpieza.

Los datos que existían antes del arranque no se eliminan ni se duplican.
No se restablecen sus contraseñas, no se reactivan perfiles o recursos y no se
reprograman sus reservas. Si los ejemplos de una ejecución anterior ya estaban
cargados, esa nueva ejecución no los registra como propios ni los borra al salir.

Los datos creados manualmente durante la sesión se conservan. Si una reserva
manual usa un recurso o funcionario de demostración, también se conserva esa
dependencia. Lo mismo ocurre si un recurso manual utiliza una categoría de
demostración. El script informa cuántas dependencias tuvo que conservar.

La carga se realiza en una transacción. Si un ID está ocupado por otro perfil o
un recurso ya tiene una reserva que impide insertar el ejemplo, se informa el
conflicto y se revierte la carga de ese intento. No se eliminan datos existentes.

## Cargar sin Docker o desde Maven

Con el esquema instalado y `.config/database.env` configurado, compila y ejecuta
la utilidad del backend. Hazlo antes de iniciar el servidor:

```powershell
.\mvnw.cmd install -DskipTests
New-Item -ItemType Directory -Force target/local | Out-Null
.\mvnw.cmd -pl backend exec:java "-Dexec.mainClass=una.proyecto.bootstrap.CargarDemostracion" "-Dexec.args=cargar target/local/demo-manual.properties"
```

Este comando carga los cinco ejemplos y registra sus IDs; no crea el
administrador ni inicia MySQL. La [guía sin Docker](ejecucion-sin-docker.md)
explica esos pasos. Tras cerrar los clientes y detener el backend, deja MySQL
activo y limpia los ejemplos con:

```powershell
.\mvnw.cmd -pl backend exec:java "-Dexec.mainClass=una.proyecto.bootstrap.CargarDemostracion" "-Dexec.args=limpiar target/local/demo-manual.properties"
```

El registro no contiene contraseñas y pertenece a la conexión de base de datos
con la que se creó. Si la limpieza falla, se conserva para reintentar: usa el
comando anterior con la ruta exacta indicada por el script y la misma
configuración JDBC. Conserva ese archivo hasta completar la limpieza; `clean`
elimina `target/`.

`database/seed.sql` sigue disponible como catálogo pequeño de dos categorías
y dos recursos; es una alternativa distinta de esta demostración completa.

[Volver al README](../README.md) · [Pruebas funcionales](pruebas-funcionales.md)
