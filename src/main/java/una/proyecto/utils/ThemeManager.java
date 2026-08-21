package una.proyecto.utils;

import javafx.scene.Scene;

public class ThemeManager {
    private static boolean isDarkMode = false;
    public static boolean IsDarkMode() {
        return isDarkMode;
    }

    public static void setDarkMode(boolean darkMode) {
        isDarkMode = darkMode;
    }

    public static void applyTheme(Scene scene) {
        if (scene != null && scene.getRoot() != null) {
            if (IsDarkMode()) {
                if (!scene.getRoot().getStyleClass().contains("dark-mode")) {
                    scene.getRoot().getStyleClass().add("dark-mode");
                }
            } else {
                scene.getRoot().getStyleClass().remove("dark-mode");
            }
        }
    }
}