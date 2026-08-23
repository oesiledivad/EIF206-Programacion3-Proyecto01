package una.proyecto.model ;
public class Funcionarios extends Usuarios {
    private String phone;
    private String name;
    //Posee una lista de reservas
    public Funcionarios(String clave, String id, String rol, String name, String phone){
        super(id,rol);
        this.phone=phone;
        this.name=name;
    }
    @Override
    public void chagePassword(String password){
    this.setClave(password);
    }
    public String getPhone(){
        return this.phone;
    }
    public String getName(){
        return this.name;
    }
}
