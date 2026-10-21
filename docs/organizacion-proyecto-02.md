# Arquitectura del proyecto

El repositorio contiene tres módulos Maven. El POM raíz centraliza la versión de
Java y la configuración de compilación y pruebas.

| Módulo | Contenido | Documentación |
| --- | --- | --- |
| `frontend` | Aplicación JavaFX, MVC y reportes PDF | [README](../frontend/README.md) |
| `backend` | Servicios, lógica, integración con IA y datos XML | [README](../backend/README.md) |
| `common` | Modelos y adaptadores de serialización | [README](../common/README.md) |

El MER y los scripts MySQL se encuentran en [`database`](../database/README.md).

## Dependencias

```text
frontend --> backend --> common
    |______________________^
```

La aplicación de escritorio utiliza los servicios del backend mediante llamadas
locales. El backend depende de los modelos de common y es independiente de JavaFX.
Common no depende de los otros módulos.

Cada módulo tiene su propio `pom.xml` y descriptor `module-info.java`.
Los paquetes Java pertenecen a un solo módulo para evitar paquetes divididos
al utilizar JPMS.

## Distribución de clases

| Clase | Módulo y paquete |
| --- | --- |
| `MainApp` | `frontend`: `una.proyecto.app` |
| `LoginLogic`, `MainLogic` | `frontend`: `una.proyecto.presentation.logic` |
| `SessionManager` | `frontend`: `una.proyecto.utils` |
| `AppFactory` | `backend`: `una.proyecto.bootstrap` |
| `XmlUtil` | `backend`: `una.proyecto.datos.xml` |
| `LocalDateAdapter`, `LocalTimeAdapter` | `common`: `una.proyecto.serialization` |

La lógica de presentación coordina el ingreso y la navegación. La autenticación
de credenciales se realiza mediante `AuthService` en el backend.

## Compilación y ejecución

Usar JDK 17 o superior (JDK 21 recomendado) y Maven. Ejecutar desde la raíz:

```sh
mvn verify
```

Para ejecutar la interfaz:

```sh
mvn install -DskipTests
mvn -pl frontend javafx:run
```

El objetivo `javafx:run` corresponde al frontend. Importar el `pom.xml` raíz en
IntelliJ y configurar la raíz del repositorio como directorio de trabajo.

## Pruebas

Surefire ejecuta las clases `*Test` y Failsafe las clases `*IT`. Los reportes se
encuentran en los directorios `target` de backend y frontend. Las pruebas utilizan
classpath; la aplicación conserva los descriptores JPMS de sus módulos.

Para verificar sin llamar al servicio externo de IA:

```sh
mvn verify "-Dit.test=*IT,!IAgeneratorIntegracionIT" -Dfailsafe.failIfNoSpecifiedTests=false
```

Las pruebas de integración de IA utilizan Groq y requieren `GROQ_API_KEY`.
Las pruebas unitarias de IA utilizan un cliente HTTP simulado.
GitHub Actions ejecuta `mvn verify` desde la raíz para procesar todos los módulos.

## Configuración y datos

Los datos XML se almacenan en `data/xml/`, la configuración de IA en `.env` y los
reportes PDF en `pdfs/`, todos relativos a la raíz del repositorio. Los plugins de
pruebas y de ejecución utilizan ese directorio de trabajo.

El recurso de ejemplo de usuarios pertenece al backend. La capa de datos utiliza
los archivos de `data/xml/`. Los directorios `target/`, los datos locales y `.env`
están excluidos del control de versiones mediante `.gitignore`.

Para instalar el esquema MySQL, seguir la [guía de base de datos](../database/README.md).
