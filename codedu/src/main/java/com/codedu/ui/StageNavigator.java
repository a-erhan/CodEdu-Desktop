package com.codedu.ui;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.function.Consumer;

@Component
public class StageNavigator {

    private final FxmlViewLoader viewLoader;

    public StageNavigator(FxmlViewLoader viewLoader) {
        this.viewLoader = viewLoader;
    }

    public <T> void replaceScene(Stage stage, String fxmlPath, Class<T> controllerType, Consumer<T> initController)
            throws IOException {
        if (stage == null) {
            return;
        }

        final Parent[] rootHolder = new Parent[1];
        T controller = viewLoader.loadFresh(fxmlPath, controllerType, view -> rootHolder[0] = view);
        if (initController != null) {
            initController.accept(controller);
        }

        Parent root = rootHolder[0];
        double w = Math.max(800, stage.getWidth());
        double h = Math.max(600, stage.getHeight());
        stage.setScene(new Scene(root, w, h));
    }

    public <T> void replaceSceneFixed(Stage stage, String fxmlPath, Class<T> controllerType, Consumer<T> initController,
                                     double width, double height) throws IOException {
        if (stage == null) {
            return;
        }

        final Parent[] rootHolder = new Parent[1];
        T controller = viewLoader.loadFresh(fxmlPath, controllerType, view -> rootHolder[0] = view);
        if (initController != null) {
            initController.accept(controller);
        }
        stage.setScene(new Scene(rootHolder[0], width, height));
    }
}

