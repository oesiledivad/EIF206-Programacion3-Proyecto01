# Recorrido funcional

Ejecutar desde la raíz del repositorio siguiendo el
[arranque completo](../README.md#compilación-y-ejecución). Usar una base de
demostración y abrir otro cliente con `./iniciar.ps1 -SoloFrontend`.

Ingresar como administrador en una ventana y como funcionario en la otra.
Las alertas se envían a otros usuarios; dos sesiones del mismo usuario no
reciben entre sí las reservas que ese usuario crea.

## Funcionalidades

| Apartado | Recorrido | Resultado esperado |
| --- | --- | --- |
| Ingreso y cambio de clave | Probar credenciales incorrectas, ingresar y cambiar la contraseña. Cerrar sesión e ingresar con la nueva clave. | Se reporta el ingreso incorrecto y la nueva clave queda persistida. |
| Funcionarios | Como administrador, crear un funcionario con cédula de nueve dígitos y teléfono válido; buscarlo y editarlo. | Los cambios aparecen al consultar. Su contraseña inicial es su cédula. |
| Categorías | Crear una categoría, buscarla y modificar su descripción. | Conserva su ID y muestra la descripción actualizada. |
| Recursos | Crear un recurso de esa categoría y buscarlo mediante los filtros. | La consulta devuelve el recurso con su categoría y descripción. |
| Reservas | Como funcionario, seleccionar la categoría, una fecha futura y horas válidas; reservar. Intentar el mismo horario desde otro funcionario cuando solo exista un recurso. | Se guarda una reserva y el segundo intento informa que no hay disponibilidad. Cancelar la reserva propia permite reservar otra vez. |
| Calendarización | Seleccionar la fecha y categoría de la reserva y pulsar **Cargar**. | La reserva aparece en el horario y recurso asignados. |
| Actividades | Cargar la semana que contiene la reserva. | La actividad aparece en su día y horario con el encargado. |
| Estadísticas | Consultar un período que incluya la reserva y otro sin actividades. Probar también fechas invertidas. | Los totales corresponden al período; las semanas sin actividades muestran cero y las fechas incorrectas producen un mensaje. |
| Alertas | Crear una reserva desde el otro usuario mientras se cambia de pantalla. Abrir **Ver detalle**, cancelar el descarte y luego confirmarlo. | El panel permanece visible, muestra los datos y solo quita la tarjeta al confirmar. La reserva sigue existiendo. |

## Reportes

Exportar desde funcionarios, categorías, recursos, reservas, calendarización,
actividades y las dos pestañas de estadísticas. Abrir los archivos en `pdfs/` y
comprobar títulos, fechas, tildes, filas y gráficos.

Calendarización utiliza `Calendarizacion.pdf` y actividades `Actividades.pdf`.
Cada nueva exportación reemplaza el reporte anterior de su mismo tipo.

## Conexión

Abrir el frontend sin backend: el login debe mostrar el error y permitir
reintentar cuando el servidor esté disponible. Con una sesión iniciada,
interrumpir el canal debe mostrar el estado de desconexión y conservar las
tarjetas. **Reconectar alertas** recupera el canal si la sesión sigue vigente;
un reinicio del backend requiere volver a ingresar.

Para el recorrido en otra computadora, usar la IP del equipo que ejecuta el
backend, como se indica en la [configuración de conexión](../frontend/README.md).
