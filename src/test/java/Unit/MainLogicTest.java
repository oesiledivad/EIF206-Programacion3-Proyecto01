package Unit;

import una.proyecto.logic.MainLogic;
import una.proyecto.utils.SessionManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class MainLogicTest {

    private MainLogic mainLogic;
    private SessionManager session;

    @BeforeEach
    void setUp() {
        mainLogic = new MainLogic();
        session = SessionManager.getInstance();
    }

    @AfterEach
    void tearDown() {
        session.logout();
    }

    // ---------- getCurrentUserInfo ----------

    @Test
    void getCurrentUserInfo_sinSesion_retornaGuest() {
        var info = mainLogic.getCurrentUserInfo();

        assertEquals("Invitado", info.getDisplayName());
        assertEquals("Invitado", info.getDisplayRole());
        assertFalse(info.isAdmin());
        assertNull(info.getId());
    }

    @Test
    void getCurrentUserInfo_conNombre_usaNombreComoDisplayName() {
        session.login("123456789", "Juan Perez", "FUNCIONARIO");

        var info = mainLogic.getCurrentUserInfo();

        assertEquals("Juan Perez", info.getDisplayName());
    }

    @Test
    void getCurrentUserInfo_nombreVacio_usaIdComoDisplayName() {
        session.login("123456789", "", "FUNCIONARIO");

        var info = mainLogic.getCurrentUserInfo();

        assertEquals("123456789", info.getDisplayName());
    }

    @Test
    void getCurrentUserInfo_nombreNullEIdVacio_usaUsuarioGenerico() {
        session.login("", null, "FUNCIONARIO");

        var info = mainLogic.getCurrentUserInfo();

        assertEquals("Usuario", info.getDisplayName());
    }

    @Test
    void getCurrentUserInfo_roleNull_seNormalizaAFuncionario() {
        session.login("123456789", "Juan", null);

        var info = mainLogic.getCurrentUserInfo();

        assertEquals("FUNCIONARIO", info.getDisplayRole());
    }

    @Test
    void getCurrentUserInfo_roleAdminCaseInsensitive_seNormaliza() {
        session.login("admin", "Admin User", "admin");

        var info = mainLogic.getCurrentUserInfo();

        assertEquals("ADMINISTRADOR", info.getDisplayRole());
        assertTrue(info.isAdmin());
    }

    @Test
    void getCurrentUserInfo_roleAdministrador_seNormaliza() {
        session.login("admin", "Admin User", "ADMINISTRADOR");

        var info = mainLogic.getCurrentUserInfo();

        assertEquals("ADMINISTRADOR", info.getDisplayRole());
    }

    @Test
    void getCurrentUserInfo_roleUser_seNormalizaAFuncionario() {
        session.login("123456789", "Juan", "user");

        var info = mainLogic.getCurrentUserInfo();

        assertEquals("FUNCIONARIO", info.getDisplayRole());
    }

    @Test
    void getCurrentUserInfo_roleDesconocido_seRetornaTalCual() {
        session.login("123456789", "Juan", "SUPERVISOR");

        var info = mainLogic.getCurrentUserInfo();

        assertEquals("SUPERVISOR", info.getDisplayRole());
    }

    // ---------- getMenuPermissions ----------

    @Test
    void getMenuPermissions_admin_tienePermisosDeGestion() {
        session.login("admin", "Admin", "ADMIN");

        var permisos = mainLogic.getMenuPermissions();

        assertTrue(permisos.isAdmin());
        assertTrue(permisos.canManageUsers());
        assertTrue(permisos.canManageCategories());
        assertTrue(permisos.canManageResources());
        assertFalse(permisos.canMakeReservations());
    }

    @Test
    void getMenuPermissions_funcionario_tienePermisosLimitados() {
        session.login("123456789", "Juan", "FUNCIONARIO");

        var permisos = mainLogic.getMenuPermissions();

        assertFalse(permisos.isAdmin());
        assertFalse(permisos.canManageUsers());
        assertFalse(permisos.canManageCategories());
        assertFalse(permisos.canManageResources());
        assertTrue(permisos.canMakeReservations());
    }

    @Test
    void getMenuPermissions_vistasComunes_siempreVisibles() {
        session.login("123456789", "Juan", "FUNCIONARIO");

        var permisos = mainLogic.getMenuPermissions();

        assertTrue(permisos.canViewCalendar());
        assertTrue(permisos.canViewActivities());
        assertTrue(permisos.canViewStatistics());
    }

    // ---------- logoutUser ----------

    @Test
    void logoutUser_cierraLaSesion() {
        session.login("123456789", "Juan", "FUNCIONARIO");

        mainLogic.logoutUser();

        assertFalse(session.isLoggedIn());
    }

    // ---------- hasAccessToView ----------

    @Test
    void hasAccessToView_vistaComun_accesibleSinImportarRolNiSesion() {
        // Sin sesión activa
        assertTrue(mainLogic.hasAccessToView("dashboard-view"));
        assertTrue(mainLogic.hasAccessToView("reservas-funcionario-view"));
    }

    @Test
    void hasAccessToView_vistaAdmin_conRolAdmin_true() {
        session.login("admin", "Admin", "ADMIN");

        assertTrue(mainLogic.hasAccessToView("funcionarios-administrador-view"));
        assertTrue(mainLogic.hasAccessToView("categorias-administrador-view"));
        assertTrue(mainLogic.hasAccessToView("recursos-administrador-view"));
    }

    @Test
    void hasAccessToView_vistaAdmin_conRolFuncionario_false() {
        session.login("123456789", "Juan", "FUNCIONARIO");

        assertFalse(mainLogic.hasAccessToView("funcionarios-administrador-view"));
    }

    @Test
    void hasAccessToView_vistaAdmin_sinSesion_false() {
        assertFalse(mainLogic.hasAccessToView("funcionarios-administrador-view"));
    }

    @Test
    void hasAccessToView_vistaDesconocida_false() {
        assertFalse(mainLogic.hasAccessToView("vista-que-no-existe"));
    }

    // ---------- getViewTitle ----------

    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource({
            "dashboard-view, Dashboard",
            "reservas-funcionario-view, Reservaciones",
            "funcionarios-administrador-view, Funcionarios",
            "categorias-administrador-view, Categorías",
            "recursos-administrador-view, Recursos",
            "calendarizacion-view, Calendario",
            "calendarizacion-actividades-view, Actividades",
            "estadisticas-view, Estadísticas",
            "vista-inexistente, Sistema de Reserva"
    })
    void getViewTitle_retornaTituloCorrecto(String viewName, String tituloEsperado) {
        assertEquals(tituloEsperado, mainLogic.getViewTitle(viewName));
    }
}