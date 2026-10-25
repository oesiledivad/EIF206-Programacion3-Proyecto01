package una.proyecto.bootstrap;

import una.proyecto.datos.UsuarioDatos;
import una.proyecto.model.Administrador;

import java.util.Arrays;

public final class CrearAdministrador {
    private CrearAdministrador() { }

    public static void main(String[] args) {
        char[] password;
        if (System.console() != null) {
            password = System.console().readPassword("Contraseña para admin: ");
        } else {
            String value = System.getenv("APP_ADMIN_PASSWORD");
            password = value == null ? null : value.toCharArray();
        }
        if (password == null || password.length < 6) {
            throw new IllegalArgumentException("Ingrese una contraseña de al menos 6 caracteres mediante consola o APP_ADMIN_PASSWORD.");
        }
        try {
            Administrador admin = new Administrador("admin", "ADMIN", "Administrador");
            admin.setPassword(new String(password));
            if (!new UsuarioDatos().addUser(admin)) {
                throw new IllegalStateException("El usuario admin ya existe; no se modificó su contraseña ni su estado.");
            }
            System.out.println("Administrador creado.");
        } finally {
            Arrays.fill(password, '\0');
        }
    }
}
