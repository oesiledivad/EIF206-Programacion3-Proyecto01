# EIF206-Programacion3-Proyecto01 (2026-II)
## Sistema De Reserva De Recursos
## Descripcion:
- Sistema de escritorio en Java que gestiona las reservas de recursos para la realizacion de trabajo de sus funcionarios.
- Funcionario ingresa al ingresar al sistema: fecha, hora de inicio, hora de finalizacion y va seleccionando las categorias de recursos (sala, computadora, proyector, etc). Se indica si tuvo exito (habia disponibilidad de al menos una unidad de cada recurso necesitado) o si no tuvo exito. Si hubo exito la reserva se hara con el primer recurso disponible, sino el usuario hara los cambios en la reserva actual e intentara de nuevo.
- El usuario puede llenar los datos usando IA, describiendo: actividad, recursos y demas. El sistema usando (LLM), extraera los datos y llenara el formulario. El usuario los puede modificar antes de aplicarlos.
- Interfaz grafica, formato de archivos: XML, arquitectura por capaz, la interfaz debe ajustarse al (MVC), dos tipos de usuario: administrador y funcionario cada uno  con id, clave y rol.
## Funcionalidades:
Todas deben incluir la opcion de *generar reporte PDF*.

**1.Log In:** Los usuarios podran ingresar al sistema usando su id y clave. Pueden cambiar su clave en cualquier momento.

**2.Reservas:** Un funcionario podra ver sus reservas, crear o cancelar reservas futuras. Solo lo puede hacer el funcionario.

**3.Lista de Funcionarios:** Busqueda de funcionarios por id o nombre, inclusion, consulta, modificacion y borrado de funcionarios. De cada funcionario se requiere: informacion como usuario, nombre y telefono. Al agregar su clave y id quedaran, luego el usuario puede cambiarla. Para cambiarla solo lo puede hacer un administrador.

**4.Lista de categorias de recursos:** Buscar categorias por descripcion, inclusion, consulta, modificacion y borrado. Se requiere id y descripcion.

**5.Lista de Recursos:** Filtrar por categoria, CRUD completo, cada recurso: id/número de activo, categoría (FK), descripción

**6.Calendarizacion de Recursos:** Selecciona fecha + categoría → matriz (filas = horas, columnas = recursos de esa categoría)
Celdas muestran si está reservado (actividad + funcionario o administrador)

**7.Programacion de actividades:** Para una semana → matriz (filas = horas, columnas = días de la semana)
Celdas muestran actividad + funcionario responsable

**8.Estadisticas:** Rango de fechas para recursos → categorías reservadas + cantidad + gráfico de barras
Rango de fechas para actividades → semanas + cantidad de actividades + gráfico de barras

## Arquitectura por capas

### Capa Modelo (entidades / dominio)

```
Usuario (abstracta)
 ├─ id: String
 ├─ clave: String
 ├─ rol: RolEnum {ADMIN, FUNCIONARIO}
 ├─ cambiarClave(claveActual, claveNueva)
 └─ validar()

 ├── Administrador extends Usuario
 └── Funcionario extends Usuario
      ├─ nombre: String
      ├─ telefono: String
      └─ (relación 1..N con Reserva)

CategoriaRecurso
 ├─ id: String (autogenerado, ej. CAT-000001)
 ├─ descripcion: String
 └─ getters/setters + validar()

Recurso
 ├─ id: String (número de activo)
 ├─ categoria: CategoriaRecurso
 ├─ descripcion: String
 └─ validar()

Reserva
 ├─ id: String (ej. RES-000001)
 ├─ funcionario: Funcionario
 ├─ actividad: String
 ├─ fecha: LocalDate
 ├─ horaInicio: LocalTime
 ├─ horaFin: LocalTime
 ├─ estado: EstadoReserva {ACTIVA, CANCELADA}
 ├─ recursosAsignados: List<Recurso>
 └─ cancelar()
```

### Capa Persistencia / DAO (acceso a XML)

```
DAOGenerico<T> (interfaz)
 ├─ guardar(T objeto)
 ├─ buscarPorId(String id)
 ├─ listarTodos(): List<T>
 ├─ modificar(T objeto)
 └─ eliminar(String id)

 ├── FuncionarioDAO
 ├── CategoriaRecursoDAO
 ├── RecursoDAO
 └── ReservaDAO

XMLManager (utilitaria)
 ├─ leerXML(String archivo): Document
 ├─ escribirXML(Document doc, String archivo)
 └─ usa JAXB o DOM para (de)serializar cada entidad
```
