# Backend

Servicios, reglas de negocio y acceso a datos del Sistema de Reserva de Recursos.
Este módulo utiliza los modelos compartidos de [`common`](../common/README.md)
y utiliza MySQL mediante JDBC.

## Contenido

| Ubicación | Contenido |
| --- | --- |
| `src/main/java/una/proyecto/service` | Servicios de autenticación, funcionarios, categorías, recursos, reservas, calendarios y estadísticas |
| `src/main/java/una/proyecto/logic` | Validaciones y reglas de negocio |
| `src/main/java/una/proyecto/logic/ia` | Interpretación de solicitudes de reserva con Groq |
| `src/main/java/una/proyecto/datos` | Repositorios JDBC y configuración MySQL |
| `src/main/java/una/proyecto/datos/wrapper` | Contenedores JAXB |
| `src/main/java/una/proyecto/datos/xml` | Utilidades XML |
| `src/main/java/una/proyecto/bootstrap` | Construcción de servicios y creación del administrador |
| `src/main/java/una/proyecto/server` | Servidor, workers y punto de entrada `BackendApp` |
| `src/main/java/una/proyecto/security` | Hash y verificación de contraseñas |
| `src/main/resources/una/proyecto/datos` | Recurso XML de usuarios |
| `src/test/java` | Pruebas unitarias y de integración |

## Uso y dependencias

El punto de entrada del servidor es `una.proyecto.server.BackendApp`. El servidor
escucha en `Protocol.PORT` y atiende el contrato de autenticación, panel inicial y reservas por
sockets. El [`frontend`](../frontend/README.md) también utiliza servicios locales.

Sus dependencias de aplicación son `common`, MySQL Connector/J, JAXB, el cliente
HTTP de Java, JSON y Dotenv. El módulo es independiente de JavaFX.

## Ejecución

Con MySQL configurado, compilar el repositorio y ejecutar desde su raíz:

```sh
./mvnw install -DskipTests
./mvnw -pl backend exec:java
```

En Windows PowerShell, usar `.\mvnw.cmd` en lugar de `./mvnw`. El servidor escucha
por defecto en `1234`. Seguir el [arranque completo](../README.md#compilación-y-ejecución)
para configurar Docker y crear el administrador.

## Compilación y pruebas

Usar JDK 17 o superior —se recomienda JDK 21— y Maven. Ejecutar desde la raíz:

```sh
./mvnw -pl backend -am package -DskipTests
```

Para ejecutar las pruebas locales y excluir la integración con el servicio de IA:

```sh
./mvnw -pl backend -am verify "-Dit.test=*IT,!IAgeneratorIntegracionIT" -Dfailsafe.failIfNoSpecifiedTests=false
```

Para incluir también las pruebas de integración de IA:

```sh
./mvnw -pl backend -am verify
```

Para verificar pruebas del servidor sin un frontend, si no un cliente simulado:

```sh
./mvnw -pl backend -am clean verify "-Dit.test=ServerIntegracionIT" "-Dfailsafe.failIfNoSpecifiedTests=false"
```



Surefire ejecuta las clases `*Test` y Failsafe las clases `*IT`. Los reportes
se encuentran en `backend/target/surefire-reports/` y
`backend/target/failsafe-reports/`.
## Decisiones de diseño

1. **Implementación del patrón de diseño fachada en**:`src/main/java/una/proyecto/service/Servicios.java`
2. **Uso de una clase anónima `Worker `** : Con esto evitamos que el worker sea un hilo, sin embargo utiliza un hilo apra poder trabajar 
     en paralelo. A esto le llamamos ` Usar ` en lugar de `Ser `. Nos permite tener ciertas ventajas. 

        1.  No expone un run() publico, nadie puede ejecutar su ciclo por error en un hilo equivocado. 
        2.  El Worker queda libre para heredar de otra clase si algún día lo necesita.
3. 


## Configuración y datos

La conexión MySQL se configura con `DB_URL`, `DB_USER` y `DB_PASSWORD`, desde el
entorno o desde `.config/database.env`. Usar la raíz del repositorio como directorio
de trabajo e inicializar la base con los scripts de `database/`.

El asistente de IA utiliza `GROQ_API_KEY`, `GROQ_URL` y `GROQ_MODEL`, definidos
en el entorno o en el `.env` de la raíz. Las pruebas de integración de IA llaman
al servicio externo y se omiten si no hay clave configurada. Las pruebas unitarias
de IA usan un cliente HTTP simulado.

El diseño relacional y los scripts de MySQL se documentan en
[`database`](../database/README.md). Los repositorios JDBC utilizan las tablas de ese esquema.

[Documentación general](../README.md) · [Arquitectura](../docs/organizacion-proyecto-02.md)

## Autenticación y entorno

- [Usuarios MySQL y contraseñas](../database/USUARIOS.md)
- [Contrato de autenticación por sockets](../docs/protocolo-autenticacion.md)
- [MySQL con Docker Compose](../database/DOCKER.md)
