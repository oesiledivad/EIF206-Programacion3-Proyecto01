package Integration;

import org.junit.jupiter.api.*;
import una.proyecto.datos.Database;
import una.proyecto.datos.UsuarioDatos;
import una.proyecto.model.Funcionario;
import una.proyecto.proxy.ServiceProxy;
import una.proyecto.server.Server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ConexionServidorIntegracionIT {
    private static final String ID = "IT-FUNC-CON";
    private static final String CLAVE = "clavePrueba";
    private Server server;
    private Thread servidor;
    private ServiceProxy proxy;

    @BeforeEach void preparar() throws Exception {
        limpiarUsuario();
        Funcionario usuario = new Funcionario(ID, "FUNCIONARIO", "Usuario Conexion", "00000000");
        usuario.setPassword(CLAVE);
        assertTrue(new UsuarioDatos().addUser(usuario));
        iniciarServidor(0);
        proxy = new ServiceProxy("127.0.0.1", server.getPuerto());
    }

    private void iniciarServidor(int port) throws Exception {
        server = new Server(port);
        servidor = new Thread(server::iniciar, "Conexion-Servidor-IT");
        servidor.setDaemon(true);servidor.start();
    }

    @AfterEach void limpiar() throws Exception {
        if(proxy!=null) proxy.close();
        if(server!=null) server.close();
        if(servidor!=null) {servidor.join(5000);assertFalse(servidor.isAlive());}
        limpiarUsuario();
    }

    private void limpiarUsuario() throws Exception {
        try(Connection cnx=Database.instance().openConnection()) {
            for(String tabla:new String[]{"funcionarios","usuarios"}) {
                try(PreparedStatement statement=cnx.prepareStatement("DELETE FROM "+tabla+" WHERE id_usuario = ?")) {
                    statement.setString(1,ID);statement.executeUpdate();
                }
            }
        }
    }

    @Test void conexionInicialNoAutenticaNiHabilitaAlertas() {
        proxy.conectar();
        assertThrows(IllegalStateException.class,()->proxy.iniciarAlertas(reserva->{}));
        assertThrows(ServiceProxy.RemoteException.class,proxy::dashboard);
        assertNotNull(proxy.authenticate(ID,CLAVE));
        assertNotNull(proxy.dashboard());
    }

    @Test void logoutNoAvisaErrorYPermiteAbrirOtroCanalAlIngresar() throws Exception {
        CountDownLatch aviso=new CountDownLatch(1);
        assertNotNull(proxy.authenticate(ID,CLAVE));
        proxy.iniciarAlertas(reserva->{},error->aviso.countDown());
        proxy.logout();
        assertFalse(aviso.await(300,TimeUnit.MILLISECONDS));
        assertNotNull(proxy.authenticate(ID,CLAVE));
        proxy.iniciarAlertas(reserva->{},error->aviso.countDown());
        assertNotNull(proxy.dashboard());
        proxy.detenerAlertas();
        assertFalse(aviso.await(300,TimeUnit.MILLISECONDS));
    }

    @Test void caidaDelBackendNotificaUnaVezAunqueFalleOtraPeticion() throws Exception {
        CountDownLatch aviso=new CountDownLatch(1);
        AtomicInteger cantidad=new AtomicInteger();
        assertNotNull(proxy.authenticate(ID,CLAVE));
        proxy.iniciarAlertas(reserva->{},error->{cantidad.incrementAndGet();aviso.countDown();});
        server.close();
        assertThrows(IllegalStateException.class,proxy::dashboard);
        assertTrue(aviso.await(3,TimeUnit.SECONDS));
        assertEquals(1,cantidad.get());
    }

    @Test void puedeReintentarConexionCuandoElBackendVuelve() throws Exception {
        int port=server.getPuerto();
        server.close();servidor.join(5000);
        assertThrows(IllegalStateException.class,proxy::conectar);
        iniciarServidor(port);
        proxy.conectar();
        assertNotNull(proxy.authenticate(ID,CLAVE));
        assertNotNull(proxy.dashboard());
    }
}
