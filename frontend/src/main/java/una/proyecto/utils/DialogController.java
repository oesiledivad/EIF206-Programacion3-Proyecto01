package una.proyecto.utils;

import javafx.stage.Stage;

/**
 * Interfaz que deben implementar los controladores de diálogos
 * para recibir el Stage del diálogo desde Navigation.
 */
public interface DialogController {

    /**
     * Establece el Stage del diálogo.
     * @param dialogStage Stage del diálogo.
     */
    void setDialogStage(Stage dialogStage);
}