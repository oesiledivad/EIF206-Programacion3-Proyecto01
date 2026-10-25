# Autenticación por sockets

El contrato está definido en `common/una.proyecto.protocolo.Protocol` y lo atiende
`backend/una.proyecto.server.Worker`. Los modelos se comparten mediante `common`.

## Conexión

1. Abrir un socket hacia `Protocol.SERVER:Protocol.PORT`.
2. Crear `ObjectOutputStream`, ejecutar `flush()` y crear `ObjectInputStream`.
3. Enviar `writeInt(Protocol.SYNC)` y ejecutar `flush()`.
4. Leer mediante `readObject()` el identificador de conexión (`sid`, un UUID).

El `sid` identifica la conexión; la sesión de usuario se establece mediante login.
Todas las peticiones de una conexión deben enviarse y leerse en orden. El proxy
debe sincronizar cada intercambio completo para evitar mezclar respuestas entre
hilos.

## Operaciones

| Código | Constante | Parámetros, en orden | Resultado exitoso |
| --- | --- | --- | --- |
| 10 | `AUTH_LOGIN` | ID con `writeObject(String)`, contraseña con `writeObject(String)` | `Usuario` activo con nombre y rol, sin credenciales |
| 11 | `AUTH_LOGOUT` | Ninguno | `null` |
| 12 | `AUTH_CAMBIAR_CLAVE` | Contraseña actual y nueva con `writeObject(String)` | `null` |
| 99 | `DISCONNECT` | Ninguno | Cierre del socket, sin respuesta |

Después de enviar cada petición, ejecutar `flush()`. Se recomienda `reset()` del
stream al terminar cada envío para evitar reutilizar versiones anteriores de
objetos mutables.

Excepto `DISCONNECT`, todas las respuestas tienen el mismo formato:

```java
int estado = input.readInt();
Object resultado = input.readObject();
```

- `STATUS_NO_ERROR` (`0`): resultado indicado en la tabla.
- `STATUS_ERROR` (`1`): mensaje de error como `String`.

El cambio de contraseña se aplica al usuario autenticado de esa conexión; la
petición no recibe un ID de usuario. La nueva contraseña requiere al menos seis
caracteres y debe ser diferente de la actual. Los espacios forman parte de la
contraseña y no deben recortarse.

Un login fallido elimina cualquier sesión anterior de esa conexión. Logout es
idempotente: limpia la sesión y cierra su canal de alertas, pero conserva el canal
de peticiones para permitir otro login. La baja lógica de un usuario invalida el
acceso en la siguiente operación autenticada. El servidor obtiene nombre, rol y
estado de la base de datos.

## Alertas

Con el canal síncrono autenticado, abrir otro socket y crear los streams en el
mismo orden. Enviar `writeInt(Protocol.ASYNC)`, `writeObject(sid)` y `flush()`.
Leer un entero `STATUS_NO_ERROR` para confirmar que el canal quedó asociado.
Una conexión sin sesión activa o un `sid` desconocido ocasionan el cierre del
canal de alertas.

Cada notificación posterior contiene `readInt()` con el tipo de evento y
`readObject()` con sus datos. `NOTIFICACION_RESERVA_CREADA` (`900`) contiene una
`Reserva`. El servidor envía el evento a conexiones autenticadas de otros usuarios.
Al volver a iniciar sesión después de logout, abrir de nuevo el canal de alertas.

## Reservas y permisos

Las operaciones `RESERVA_CREAR`, `RESERVA_LISTAR`, `RESERVA_BUSCAR` y
`RESERVA_CANCELAR` requieren sesión activa. Se conserva su formato de parámetros:
crear recibe una `Reserva`; buscar y cancelar reciben el ID; listar no recibe
parámetros.

Un funcionario solo crea, busca y cancela reservas propias. La cancelación exige
un horario futuro. Listar devuelve sus reservas; un administrador puede listar
y consultar todas. Crear y cancelar requieren rol `FUNCIONARIO`.

Los errores de autorización también consumen los parámetros de la petición, de
modo que el siguiente intercambio conserva el orden del protocolo.

[Backend](../backend/README.md) · [Usuarios y credenciales](../database/USUARIOS.md)
