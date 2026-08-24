package una.proyecto.model;

import una.proyecto.dao.CategoriasDAO;

public class PruebaCategorias {

    public static void main(String[] args) {

        CategoriasDAO dao = new CategoriasDAO();

        ListaCategorias lista = new ListaCategorias();

        lista.agregar(new Categorias(0, "Sala para 10 personas"));
        lista.agregar(new Categorias(0, "Laptop Windows 11"));
        lista.agregar(new Categorias(0, "Proyector"));

        dao.guardar(lista);

        System.out.println("Categorías guardadas.");

        ListaCategorias listaCargada = dao.cargar();

        System.out.println("Categorías cargadas:");

        for (Categorias categoria : listaCargada.getCategorias()) {
            System.out.println(categoria);
        }
    }
}