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
El sistema utiliza una arquitectura por capas que separa la interfaz gráfica, la lógica de negocio, el acceso a los datos y las entidades del dominio. Esta separación facilita el mantenimiento, las pruebas y la reutilización del código.

### Estructura general: 
Se agregan las funciones principales, se omiten algunas que no se consideran tan relevantes.

```
UI: Vistas de la interfaz gráfica con fxml  

 ├── calendarizacion-actividades-view.fxml
 ├── calendarizacion-view.fxml
 ├── categorias-administrador-view.fxml
 ├── estadisticas-view.fxml
 ├── funcionarios-administrador-view.fxml
 ├── login-view.fxml
 ├── main-view.fxml
 ├── recursos-administrador-view.fxml
 ├── reservas-funcionario-view.fxml

                 │
                 ▼

Controller: Gestiona eventos e interacción con UI

 ├── CalendarizacionActividadesController
 ├── CalendarizacionController
 ├── CategoriasAdministradorController
 ├── EstadisticasController
 ├── FuncionariosAdministradorController
 ├── LoginController
 ├── MainViewController
 ├── RecursosController
 ├── ReservasController
                  │
                  ▼

Service: Expone las operaciones de la aplicacion y conecta la logica con el controller sin que estos conocezcan.

 ├── AuthService
 ├── CalendarizacionService
 ├── CategoriaService
 ├── EstadisticasService
 ├── FuncionarioService
 ├── RecursoService
 ├── ReservaService

                  │
                  ▼

Logic: Reglas y lógica de negocio

Dentro de este aparatado tambien está el aiGenerator dentro de la carpeta ia

 ├── CategoriaLogic
 ├── EstadisticasLogic 
 ├── FuncionarioLogic
 ├── LoginLogic
 ├── MainLogic
 ├── RecursoLogic
 ├── ReservaLogic
 ├── UsuarioLogic

                 │
                 ▼
Datos: Aqui se encuentra el CRUD (create, read, update, delete) para los archivos xml.
Cada uno con su respectivo Wrapper para facilitar la serialización de colecciones mediante JAXB.

 ├── CategoriaDatos
 ├── RecursoDatos
 ├── ReservaDatos
 ├── UsuarioDatos

                 │
                 ▼
Model: Contiene las entidades y objetos que representan la información utilizada por el sistema.
Lo principal: constructores, getters, setters, toString

 ├── Administrador
 ├── Categoria
 ├── Funcionario
 ├── Recurso
 ├── Reserva
 ├── Usuario

Capa de utilidades: Contiene componentes reutilizables que proporcionan funcionalidades de apoyo a diferentes partes del sistema.

AppFactory: construcción y configuración de las dependencias de la aplicación.
Navigation: navegación entre las diferentes vistas.
SessionManager: administración de la sesión del usuario.
ThemeManager: gestión del tema visual.
WindowHelper y ResizeHelper: manejo de ventanas.
GeneradorPDFS, ReportePDF y TablePDF: generación de reportes PDF.
XmlUtil: operaciones auxiliares relacionadas con XML.
LocalDateAdapter y LocalTimeAdapter: adaptación de fechas y horas para JAXB.

```

### Organización del proyecto

```text
src/
├── main/
│   ├── java/
│   │   └── una/proyecto/
│   │       ├── app/
│   │       ├── controller/
│   │       ├── datos/
│   │       │   └── wrapper/
│   │       ├── logic/
│   │       │   └── ia/
│   │       ├── model/
│   │       ├── service/
│   │       └── utils/
│   │
│   └── resources/
│       └── una/proyecto/
│           ├── css/
│           ├── datos/
│           ├── fonts/
│           └── ui/
│
└── test/
    └── java/
        ├── Integration/
        └── Unit/
```




