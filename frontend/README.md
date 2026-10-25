# Frontend

Aplicación de escritorio del Sistema de Reserva de Recursos, desarrollada con
JavaFX y organizada según el patrón Modelo–Vista–Controlador.

## Contenido

| Ubicación | Contenido |
| --- | --- |
| `src/main/java/una/proyecto/app` | Punto de entrada `MainApp` |
| `src/main/java/una/proyecto/controller` | Controladores de las pantallas |
| `src/main/java/una/proyecto/proxy` | Cliente de autenticación y consulta del panel por sockets |
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
Ingreso, cambio de contraseña y panel inicial utilizan `ServiceProxy` contra
[`backend`](../backend/README.md). Las demás vistas usan servicios locales. Las vistas usan JavaFX, FormsFX e
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

## Configuración y pruebas

Los servicios locales utilizan la conexión MySQL descrita en la
[guía de usuarios y configuración](../database/USUARIOS.md). La configuración del
asistente de IA se carga desde el archivo `.env` descrito en el
[README general](../README.md).

Para ejecutar las pruebas del módulo y sus dependencias sin llamadas externas a IA:

```sh
./mvnw -pl frontend -am verify "-Dit.test=*IT,!IAgeneratorIntegracionIT" -Dfailsafe.failIfNoSpecifiedTests=false
```

Los reportes propios del módulo se generan en `target/surefire-reports/` y
`target/failsafe-reports/`, dentro de `frontend/`.

[Documentación general](../README.md) · [Arquitectura](../docs/organizacion-proyecto-02.md)
