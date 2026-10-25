# Backend

Servicios, reglas de negocio y acceso a datos del Sistema de Reserva de Recursos.
Este módulo utiliza los modelos compartidos de [`common`](../common/README.md)
y conserva la persistencia XML mediante JAXB.

## Contenido

| Ubicación | Contenido |
| --- | --- |
| `src/main/java/una/proyecto/service` | Servicios de autenticación, funcionarios, categorías, recursos, reservas, calendarios y estadísticas |
| `src/main/java/una/proyecto/logic` | Validaciones y reglas de negocio |
| `src/main/java/una/proyecto/logic/ia` | Interpretación de solicitudes de reserva con Groq |
| `src/main/java/una/proyecto/datos` | Acceso a los archivos XML |
| `src/main/java/una/proyecto/datos/wrapper` | Contenedores de serialización JAXB |
| `src/main/java/una/proyecto/datos/xml` | Utilidades XML |
| `src/main/java/una/proyecto/bootstrap` | Construcción de servicios mediante `AppFactory` |
| `src/main/resources/una/proyecto/datos` | Recurso XML de usuarios |
| `src/test/java` | Pruebas unitarias y de integración |

## Uso y dependencias

Los servicios son invocados localmente por el
[`frontend`](../frontend/README.md). El módulo se compila como una biblioteca JAR
y no declara un punto de entrada ejecutable.

Sus dependencias de aplicación son `common`, JAXB, el cliente HTTP de Java,
JSON y Dotenv. El módulo es independiente de JavaFX.

## Compilación y pruebas

Usar JDK 17 o superior —se recomienda JDK 21— y Maven. Ejecutar desde la raíz:

```sh
mvn -pl backend -am package -DskipTests
```

Para ejecutar las pruebas locales y excluir la integración con el servicio de IA:

```sh
mvn -pl backend -am verify "-Dit.test=*IT,!IAgeneratorIntegracionIT" -Dfailsafe.failIfNoSpecifiedTests=false
```

Para incluir también las pruebas de integración de IA:

```sh
mvn -pl backend -am verify
```

Para verificar pruebas del servidor sin un frontend, si no un cliente simulado:

```sh
mvn -pl backend -am clean verify "-Dit.test=ServerIntegracionIT" "-Dfailsafe.failIfNoSpecifiedTests=false"
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

Las rutas de datos se resuelven desde la raíz del repositorio:

- `data/xml/usuarios.xml`
- `data/xml/categorias.xml`
- `data/xml/recursos.xml`
- `data/xml/reservas.xml`

El asistente de IA utiliza `GROQ_API_KEY`, `GROQ_URL` y `GROQ_MODEL`, definidos
en el entorno o en el `.env` de la raíz. Las pruebas de integración de IA llaman
al servicio externo y se omiten si no hay clave configurada. Las pruebas unitarias
de IA usan un cliente HTTP simulado.

El diseño relacional y los scripts de MySQL se documentan en
[`database`](../database/README.md). La ejecución de esos scripts es independiente
de los servicios XML descritos aquí.

[Documentación general](../README.md) · [Arquitectura](../docs/organizacion-proyecto-02.md)
