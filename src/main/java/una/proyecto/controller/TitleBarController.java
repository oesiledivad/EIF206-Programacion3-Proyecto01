package una.proyecto.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;
import una.proyecto.utils.WindowHelper;

public class TitleBarController {
    @FXML public Label lblWindowTitle;
    @FXML public FontIcon maximizeIcon;
    @FXML private HBox titleBar;
    @FXML private Button btnMinimize;
    @FXML private Button btnMaximize;
    @FXML private Button btnClose;

    @FXML
    public void initialize() {
        WindowHelper.makeWindowDraggable(titleBar, btnMinimize, btnMaximize, btnClose, this::updateMaximizeIcon);
    }

    private void updateMaximizeIcon() {

        Stage stage = (Stage) btnMaximize.getScene().getWindow();

        if (stage.isMaximized()) {
            maximizeIcon.setIconLiteral("fa-window-restore");
        } else {
            maximizeIcon.setIconLiteral("fa-window-maximize");
        }
    }

    public void setWindowTitle(String title) {
        lblWindowTitle.setText(title);
    }

}