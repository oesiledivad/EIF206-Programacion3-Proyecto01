package una.proyecto.model;

public abstract class  Usuarios {
private String clave;
private String id;
private String rol;

public Usuarios( String id, String rol){
    this.clave=id;//luego se modigica con el metodo changePassword
    this.rol=rol;
    this.id=id;
}
public abstract void  chagePassword(String password);
public String getClave(){return this.clave;}
public String getId(){return this.id;}
public String getRol(){return this.rol;}
public void setClave(String clave){this.clave=clave;}

}

