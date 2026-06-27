package bond.thematic.api.engine.controller;

import java.util.Collections;
import java.util.Map;

public record AnimationControllerSet(Map<String, AnimationController> controllers) {
    public AnimationControllerSet {
        controllers = controllers != null ? Map.copyOf(controllers) : Map.of();
    }

    public AnimationController get(String name) {
        return controllers.get(name);
    }

    public boolean isEmpty() {
        return controllers.isEmpty();
    }
}
