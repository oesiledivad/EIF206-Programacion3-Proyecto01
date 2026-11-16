package una.proyecto.model;

@FunctionalInterface
public interface LoginService {
    Usuario authenticate(String id, String password);
}
