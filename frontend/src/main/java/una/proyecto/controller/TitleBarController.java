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
    @FXML
    public Label lblWindowTitle;
    @FXML
    public FontIcon maximizeIcon;
    @FXML
    private StackPane titleBar;
    @FXML
    private Button btnMinimize;
    @FXML
    private Button btnMaximize;
    @FXML
    private Button btnClose;

    private boolean isDraggable = true;

    @FXML
    public void initialize() {
        Navigation.setTitleBarController(this);

        applyWindowBehavior();
    }

    /**
     * Aplica el comportamiento según el estado de isDraggable.
     */
    public void applyWindowBehavior() {
        if (titleBar != null && titleBar.getScene() != null) {
            Stage stage = (Stage) titleBar.getScene().getWindow();
            if (stage != null) {
                WindowHelper.setupAutoUnmaximizeOnResize(stage, this::updateMaximizeIcon);
            }
        }
        if (isDraggable && titleBar != null) {
            WindowHelper.makeWindowDraggable(titleBar, btnMinimize, btnMaximize, btnClose, this::updateMaximizeIcon);
        } else {
            WindowHelper.makeWindowStatic(titleBar, btnMinimize, btnMaximize, btnClose, this::updateMaximizeIcon);
        }
    }

    /**
     * Permite desactivar el arrastre desde la vista de Login antes de que se carguen los componentes
     * o mediante un método de configuración.
     */
    public void setDraggable(boolean draggable) {
        this.isDraggable = draggable;
        applyWindowBehavior();
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

    public StackPane getTitleBar() {
        return titleBar;
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