# Common

Modelos compartidos del Sistema de Reserva de Recursos. Este módulo es una
biblioteca utilizada por [`frontend`](../frontend/README.md) y
[`backend`](../backend/README.md).

## Contenido

| Paquete | Contenido |
| --- | --- |
| `una.proyecto.model` | Usuarios, funcionarios, administradores, categorías, recursos y reservas |
| `una.proyecto.model` | Estado de reserva y estructuras de datos para calendarios y estadísticas |
| `una.proyecto.model` | Contrato `LoginService` y respuesta `DashboardData` del panel inicial |
| `una.proyecto.protocolo` | Operaciones y estados de comunicación |
| `una.proyecto.serialization` | Adaptadores JAXB para `LocalDate` y `LocalTime` |

El descriptor `src/main/java/module-info.java` declara el módulo
`una.proyecto.common` y exporta los paquetes de modelos y serialización.

## Dependencias

La dependencia de aplicación es Jakarta XML Binding API. Los modelos contienen
anotaciones JAXB y utilizan los adaptadores de fecha y hora para la serialización
XML.

`common` no depende de `frontend` ni de `backend`. Las clases compartidas permiten
que ambos módulos utilicen las mismas representaciones de los datos.

## Compilación

Usar JDK 17 o superior —se recomienda JDK 21— y Maven. Ejecutar desde la raíz:

```sh
./mvnw -pl common -am package
```

El resultado se genera en `common/target/common-1.0-SNAPSHOT.jar`.

Para instalar la biblioteca en el repositorio Maven local:

```sh
./mvnw -pl common -am install
```

Common se compila como dependencia de las dos aplicaciones; no necesita abrir
un proceso propio ni configurar credenciales. Las pruebas
de comportamiento de los modelos se encuentran en los módulos que los utilizan.

[Documentación general](../README.md) · [Arquitectura](../docs/organizacion-proyecto-02.md)
