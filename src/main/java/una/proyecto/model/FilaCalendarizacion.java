package una.proyecto.model;

public class FilaCalendarizacion {

    private String hora;
    private String lunes;
    private String martes;
    private String miercoles;
    private String jueves;
    private String viernes;
    private String sabado;
    private String domingo;

    public FilaCalendarizacion(String hora) {
        this.hora = hora;
        this.lunes = "";
        this.martes = "";
        this.miercoles = "";
        this.jueves = "";
        this.viernes = "";
        this.sabado = "";
        this.domingo = "";
    }

    public String getHora() {
        return hora;
    }

    public String getLunes() {
        return lunes;
    }

    public String getMartes() {
        return martes;
    }

    public String getMiercoles() {
        return miercoles;
    }

    public String getJueves() {
        return jueves;
    }

    public String getViernes() {
        return viernes;
    }

    public String getSabado() {
        return sabado;
    }

    public String getDomingo() {
        return domingo;
    }

    public void setLunes(String lunes) {
        this.lunes = lunes;
    }

    public void setMartes(String martes) {
        this.martes = martes;
    }

    public void setMiercoles(String miercoles) {
        this.miercoles = miercoles;
    }

    public void setJueves(String jueves) {
        this.jueves = jueves;
    }

    public void setViernes(String viernes) {
        this.viernes = viernes;
    }

    public void setSabado(String sabado) {
        this.sabado = sabado;
    }

    public void setDomingo(String domingo) {
        this.domingo = domingo;
    }
}
