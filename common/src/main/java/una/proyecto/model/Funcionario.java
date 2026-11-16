package una.proyecto.model;

public class Funcionario extends Usuario {

    private String phone;

    public Funcionario() {
        super();
    }

    public Funcionario(
            String id,
            String role,
            String name,
            String phone
    ) {
        super(id, role, name);
        this.phone = phone;
    }

    public Funcionario(String id, String funcionario, String nombreTest) {
        super(id, funcionario, nombreTest);
    }

    @Override
    public void changePassword(String password) {
        setPassword(password);
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}

