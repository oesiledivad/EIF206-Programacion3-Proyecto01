package una.proyecto.utils;

import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.input.MouseEvent;

public class ResizeHelper {

    private static final int MARGIN = 8;

    public static void addResizeListener(Stage stage) {
        ResizeListener resizeListener = new ResizeListener(stage);
        stage.getScene().addEventFilter(MouseEvent.MOUSE_PRESSED, resizeListener);
        stage.getScene().addEventFilter(MouseEvent.MOUSE_RELEASED, resizeListener);
        stage.getScene().addEventFilter(MouseEvent.MOUSE_DRAGGED, resizeListener);
        stage.getScene().addEventFilter(MouseEvent.MOUSE_MOVED, resizeListener);
    }

    private static class ResizeListener implements EventHandler<MouseEvent> {
        private final Stage stage;
        private Cursor cursorEvent = Cursor.DEFAULT;
        private boolean resizing = false;
        private double startX = 0;
        private double startY = 0;

        public ResizeListener(Stage stage) {
            this.stage = stage;
        }

        @Override
        public void handle(MouseEvent mouseEvent) {
            EventType<? extends MouseEvent> mouseEventType = mouseEvent.getEventType();
            Scene scene = stage.getScene();

            if (!stage.isResizable()) {
                if (scene.getCursor() != Cursor.DEFAULT) {
                    scene.setCursor(Cursor.DEFAULT);
                }
                return;
            }

            if (MouseEvent.MOUSE_MOVED.equals(mouseEventType)) {
                if (mouseEvent.getX() < MARGIN && mouseEvent.getY() < MARGIN) {
                    cursorEvent = Cursor.NW_RESIZE;
                } else if (mouseEvent.getX() > scene.getWidth() - MARGIN && mouseEvent.getY() < MARGIN) {
                    cursorEvent = Cursor.NE_RESIZE;
                } else if (mouseEvent.getX() < MARGIN && mouseEvent.getY() > scene.getHeight() - MARGIN) {
                    cursorEvent = Cursor.SW_RESIZE;
                } else if (mouseEvent.getX() > scene.getWidth() - MARGIN && mouseEvent.getY() > scene.getHeight() - MARGIN) {
                    cursorEvent = Cursor.SE_RESIZE;
                } else if (mouseEvent.getX() < MARGIN) {
                    cursorEvent = Cursor.H_RESIZE;
                } else if (mouseEvent.getX() > scene.getWidth() - MARGIN) {
                    cursorEvent = Cursor.H_RESIZE;
                } else if (mouseEvent.getY() < MARGIN) {
                    cursorEvent = Cursor.V_RESIZE;
                } else if (mouseEvent.getY() > scene.getHeight() - MARGIN) {
                    cursorEvent = Cursor.V_RESIZE;
                } else {
                    cursorEvent = Cursor.DEFAULT;
                }
                scene.setCursor(cursorEvent);
            } else if (MouseEvent.MOUSE_PRESSED.equals(mouseEventType)) {
                if (cursorEvent != Cursor.DEFAULT) {
                    if (stage.isMaximized()) {
                        stage.setMaximized(false);
                    }
                    resizing = true;
                    startX = mouseEvent.getScreenX();
                    startY = mouseEvent.getScreenY();
                }
            } else if (MouseEvent.MOUSE_RELEASED.equals(mouseEventType)) {
                resizing = false;
                scene.setCursor(Cursor.DEFAULT);
            } else if (MouseEvent.MOUSE_DRAGGED.equals(mouseEventType)) {
                if (!resizing) return;

                double dx = mouseEvent.getScreenX() - startX;
                double dy = mouseEvent.getScreenY() - startY;

                if (cursorEvent == Cursor.NW_RESIZE) {
                    if (stage.getWidth() - dx > stage.getMinWidth()) {
                        stage.setX(stage.getX() + dx);
                        stage.setWidth(stage.getWidth() - dx);
                    }
                    if (stage.getHeight() - dy > stage.getMinHeight()) {
                        stage.setY(stage.getY() + dy);
                        stage.setHeight(stage.getHeight() - dy);
                    }
                } else if (cursorEvent == Cursor.NE_RESIZE) {
                    if (stage.getWidth() + dx > stage.getMinWidth()) {
                        stage.setWidth(stage.getWidth() + dx);
                    }
                    if (stage.getHeight() - dy > stage.getMinHeight()) {
                        stage.setY(stage.getY() + dy);
                        stage.setHeight(stage.getHeight() - dy);
                    }
                } else if (cursorEvent == Cursor.SW_RESIZE) {
                    if (stage.getWidth() - dx > stage.getMinWidth()) {
                        stage.setX(stage.getX() + dx);
                        stage.setWidth(stage.getWidth() - dx);
                    }
                    if (stage.getHeight() + dy > stage.getMinHeight()) {
                        stage.setHeight(stage.getHeight() + dy);
                    }
                } else if (cursorEvent == Cursor.SE_RESIZE) {
                    if (stage.getWidth() + dx > stage.getMinWidth()) {
                        stage.setWidth(stage.getWidth() + dx);
                    }
                    if (stage.getHeight() + dy > stage.getMinHeight()) {
                        stage.setHeight(stage.getHeight() + dy);
                    }
                } else if (cursorEvent == Cursor.H_RESIZE) {
                    if (mouseEvent.getX() < MARGIN) {
                        if (stage.getWidth() - dx > stage.getMinWidth()) {
                            stage.setX(stage.getX() + dx);
                            stage.setWidth(stage.getWidth() - dx);
                        }
                    } else {
                        if (stage.getWidth() + dx > stage.getMinWidth()) {
                            stage.setWidth(stage.getWidth() + dx);
                        }
                    }
                } else if (cursorEvent == Cursor.V_RESIZE) {
                    if (mouseEvent.getY() < MARGIN) {
                        if (stage.getHeight() - dy > stage.getMinHeight()) {
                            stage.setY(stage.getY() + dy);
                            stage.setHeight(stage.getHeight() - dy);
                        }
                    } else {
                        if (stage.getHeight() + dy > stage.getMinHeight()) {
                            stage.setHeight(stage.getHeight() + dy);
                        }
                    }
                }

                startX = mouseEvent.getScreenX();
                startY = mouseEvent.getScreenY();
            }
        }
    }
}