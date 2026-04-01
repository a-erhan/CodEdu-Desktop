package com.codedu.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class FxmlViewLoader {

    private final ApplicationContext applicationContext;
    private final FxmlViewCache cache;

    public FxmlViewLoader(ApplicationContext applicationContext, FxmlViewCache cache) {
        this.applicationContext = applicationContext;
        this.cache = cache;
    }

    public <T> T loadCached(String fxmlPath, Class<T> controllerType, ViewHost host) throws IOException {
        FxmlViewCache.CachedView cached = cache.get(fxmlPath);
        if (cached != null && controllerType.isInstance(cached.controller())) {
            host.setContent(cached.view());
            return controllerType.cast(cached.controller());
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        loader.setControllerFactory(applicationContext::getBean);
        Parent view = loader.load();
        Object controller = loader.getController();
        cache.put(fxmlPath, view, controller);
        host.setContent(view);
        return controllerType.cast(controller);
    }

    public <T> T loadFresh(String fxmlPath, Class<T> controllerType, ViewHost host) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        loader.setControllerFactory(applicationContext::getBean);
        Parent view = loader.load();
        Object controller = loader.getController();
        host.setContent(view);
        return controllerType.cast(controller);
    }

    public interface ViewHost {
        void setContent(Parent view);
    }
}

