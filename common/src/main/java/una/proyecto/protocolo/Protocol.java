package una.proyecto.protocolo;

// Acuerdos de comunicacion entre frontend y backend.
// Los dos lados deben usar exactamente estos valores.
public final class Protocol {

    // Donde escucha el backend
    public static final String SERVER = "localhost";
    public static final int PORT = 1234;

    // Tipo de conexion que abre el cliente al saludar
    public static final int SYNC = 1;
    public static final int ASYNC = 2;

    // Estado de cada respuesta
    public static final int STATUS_NO_ERROR = 0;
    public static final int STATUS_ERROR = 1;

    public static final int AUTH_LOGIN = 10;
    public static final int AUTH_LOGOUT = 11;
    public static final int AUTH_CAMBIAR_CLAVE = 12;
    public static final int DASHBOARD_RESUMEN = 20;

    // Operaciones generales
    public static final int DISCONNECT = 99;

    // Reservas (100)
    public static final int RESERVA_CREAR = 100;
    public static final int RESERVA_LISTAR = 101;
    public static final int RESERVA_BUSCAR = 102;
    public static final int RESERVA_CANCELAR = 103;
    public static final int RESERVA_EXTRAER_IA = 104;

    // Categorias (200)
    public static final int CATEGORIA_LISTAR = 200;
    public static final int CATEGORIA_BUSCAR_DESCRIPCION = 201;
    public static final int CATEGORIA_CREAR = 202;
    public static final int CATEGORIA_ACTUALIZAR = 203;
    public static final int CATEGORIA_ELIMINAR = 204;
    public static final int CATEGORIA_SIN_RECURSOS = 205;
    public static final int CATEGORIA_CON_RECURSOS = 206;

    // Calendarizacion (300)
    public static final int CALENDARIZACION_RECURSOS_CATEGORIA = 300;
    public static final int CALENDARIZACION_RESERVAS_DIA = 301;
    public static final int CALENDARIZACION_RESERVAS_SEMANA = 302;

    // Notificaciones asincronas (900)
    public static final int NOTIFICACION_RESERVA_CREADA = 900;

    // Recursos (400)
    public static final int RECURSO_LISTAR = 400;
    public static final int RECURSO_BUSCAR = 401;
    public static final int RECURSO_FILTRAR = 402;
    public static final int RECURSO_CREAR = 403;
    public static final int RECURSO_ACTUALIZAR = 404;
    public static final int RECURSO_ELIMINAR = 405;
    public static final int RECURSO_POR_CATEGORIA = 406;
    public static final int RECURSO_TEXTO_POR_CATEGORIAS = 407;

    // Funcionarios (500)
    public static final int FUNCIONARIO_LISTAR = 500;
    public static final int FUNCIONARIO_BUSCAR_ID = 501;
    public static final int FUNCIONARIO_BUSCAR_NOMBRE = 502;
    public static final int FUNCIONARIO_CREAR = 503;
    public static final int FUNCIONARIO_ACTUALIZAR = 504;
    public static final int FUNCIONARIO_ELIMINAR = 505;

    private Protocol() {
    }
}
