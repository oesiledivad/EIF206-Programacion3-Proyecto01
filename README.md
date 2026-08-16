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

**5.Lista de Recursos:** Filtrar por categoria








