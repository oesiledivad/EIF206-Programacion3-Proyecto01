package una.proyecto.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;
import una.proyecto.utils.WindowHelper;
import una.proyecto.utils.Navigation;

public class TitleBarController {
    @FXML public Label lblWindowTitle;
    @FXML public FontIcon maximizeIcon;
    @FXML private StackPane titleBar;
    @FXML private Button btnMinimize;
    @FXML private Button btnMaximize;
    @FXML private Button btnClose;

    @FXML
    public void initialize() {
        Navigation.setTitleBarController(this);

        WindowHelper.makeWindowDraggable(titleBar, btnMinimize, btnMaximize, btnClose, this::updateMaximizeIcon);
    }
    public void updateMaximizeIcon() {
        Stage stage = (Stage) btnMaximize.getScene().getWindow();
        if (stage != null) {
            if (stage.isMaximized()) {
                maximizeIcon.setIconLiteral("fa-window-restore");
            } else {
                maximizeIcon.setIconLiteral("fa-window-maximize");
            }
        }
    }

    public void setWindowTitle(String title) {
        if (lblWindowTitle != null) {
            lblWindowTitle.setText(title);
        }
    }

    public void disableMinimizeButton() {
        btnMinimize.setDisable(true);
    }

    public void disableMaximizeButton() {
        btnMaximize.setDisable(true);
    }

    public void enableMinimizeButton() {
        btnMinimize.setDisable(false);
    }

    public void enableMaximizeButton() {
        btnMaximize.setDisable(false);
    }
}