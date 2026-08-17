package una.proyecto;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class HolaMundoController {
    @FXML
    private Button btnPresioname;
    @FXML
    private Label lblPresioname;


    public void initialize() {
        lblPresioname.setVisible(false);
    }

    public void handlePresionameButton(ActionEvent actionEvent) {
        lblPresioname.setVisible(true);
        lblPresioname.setText("Hello Kitty  = Hola Diablo");
    }
}
