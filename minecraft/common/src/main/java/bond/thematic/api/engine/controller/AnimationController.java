package bond.thematic.api.engine.controller;

import java.util.Collections;
import java.util.Map;

public record AnimationController(String name, String initialState, Map<String, AnimationState> states) {
    public AnimationController {
        states = states != null ? Map.copyOf(states) : Map.of();
    }

    public AnimationState getState(String name) {
        return states.get(name);
    }

    public AnimationState getInitialState() {
        return states.get(initialState);
    }
}
