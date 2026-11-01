# Frontend

Aplicación de escritorio del Sistema de Reserva de Recursos, desarrollada con
JavaFX y organizada según el patrón Modelo–Vista–Controlador.

## Contenido

| Ubicación | Contenido |
| --- | --- |
| `src/main/java/una/proyecto/app` | Punto de entrada `MainApp` |
| `src/main/java/una/proyecto/controller` | Controladores de las pantallas |
| `src/main/java/una/proyecto/proxy` | Peticiones por sockets y recepción de alertas |
| `src/main/java/una/proyecto/presentation/logic` | Lógica de presentación de ingreso y navegación |
| `src/main/java/una/proyecto/utils` | Sesión local, navegación, ventanas y generación de PDF |
| `src/main/resources/una/proyecto/ui` | Vistas FXML |
| `src/main/resources/una/proyecto/css` | Estilos y recursos tipográficos |
| `src/main/resources/una/proyecto/fonts` | Fuentes |
| `src/test/java` | Pruebas de ingreso y lógica de presentación |

La interfaz incluye ingreso y cambio de contraseña, reservas, mantenimiento de
funcionarios, categorías y recursos, calendarios, estadísticas y reportes PDF.

## Dependencias

El módulo utiliza los modelos y contratos de [`common`](../common/README.md).
Las consultas y operaciones de datos utilizan `ServiceProxy` contra
[`backend`](../backend/README.md), incluidas las estadísticas por categoría y semana.
El backend es una dependencia exclusiva de pruebas, para iniciar servidores de integración;
no forma parte de la aplicación distribuida. Las vistas usan JavaFX, FormsFX e
Ikonli; los reportes utilizan PDFBox y EasyTable.

## Ejecución

Usar JDK 17 o superior —se recomienda JDK 21— y Maven. Ejecutar los comandos
desde la raíz del repositorio:

```sh
./mvnw install -DskipTests
./mvnw -pl frontend javafx:run
```

Iniciar primero MySQL y el backend siguiendo el [arranque completo](../README.md#compilación-y-ejecución).
En Windows PowerShell, usar `.\mvnw.cmd` en lugar de `./mvnw`.
La instalación resuelve las dependencias entre módulos en el repositorio Maven
local. El objetivo `javafx:run` se aplica únicamente a `frontend`.

En IntelliJ, importar el `pom.xml` raíz y usar la raíz del repositorio como
directorio de trabajo al ejecutar `una.proyecto.app.MainApp`.

## Abrir otro cliente en Windows

Con una primera aplicación iniciada mediante `iniciar.ps1`, abrir otra terminal
PowerShell en la raíz y ejecutar:

```powershell
.\iniciar.ps1 -SoloFrontend
```

El nuevo cliente utiliza el mismo backend y no administra MySQL ni el servidor.
Para otro destino, agregar `-ServidorBackend` y `-PuertoBackend`.
Cerrar esta ventana adicional conserva los servicios en ejecución.

Las alertas se muestran como una `ListView` con tarjetas FXML y estilos CSS.
El panel derecho siempre está visible, independientemente de la pantalla actual.
La notificación emergente permite enfocar la alerta más reciente sin cambiar de vista.
Para probar su recepción, ingresar con usuarios distintos en las dos ventanas
y crear una reserva desde un funcionario. La
[guía de múltiples clientes](../README.md#-abrir-otro-frontend-y-probar-las-alertas)
describe el recorrido completo.

## Configuración y pruebas

El frontend utiliza `SERVER_HOST` y `SERVER_PORT` para localizar el backend.
No necesita credenciales MySQL ni claves de Groq: ambas configuraciones pertenecen
al servidor, como se describe en el [README general](../README.md).

Las consultas de estadísticas se ejecutan en segundo plano. Mientras se consulta
un período se deshabilitan sus fechas y los botones de generar e imprimir;
los errores del servidor se muestran junto a los filtros correspondientes.

Para ejecutar las pruebas del módulo y sus dependencias sin llamadas externas a IA:

```sh
docker compose -f compose.test.yaml up -d --wait
./mvnw -pl frontend -am clean verify
```

Los reportes propios del módulo se generan en `target/surefire-reports/` y
`target/failsafe-reports/`, dentro de `frontend/`.
Para ejecutar únicamente las unitarias sin MySQL, usar `./mvnw -pl frontend -am clean test`.

[Documentación general](../README.md) · [Arquitectura](../docs/organizacion-proyecto-02.md)
