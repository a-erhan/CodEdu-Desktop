package com.codedu.ui;

import javafx.scene.Parent;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class FxmlViewCache {

    public record CachedView(Parent view, Object controller) {}

    private final Map<String, CachedView> cache = new ConcurrentHashMap<>();

    public CachedView get(String fxmlPath) {
        return cache.get(fxmlPath);
    }

    public void put(String fxmlPath, Parent view, Object controller) {
        cache.put(fxmlPath, new CachedView(view, controller));
    }

    public void invalidate(String fxmlPath) {
        cache.remove(fxmlPath);
    }
}

