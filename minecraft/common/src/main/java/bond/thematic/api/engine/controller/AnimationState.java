package bond.thematic.api.engine.controller;

import java.util.Collections;
import java.util.List;

public record AnimationState(String name, List<String> animations, List<AnimationTransition> transitions) {
    public AnimationState {
        animations = animations != null ? List.copyOf(animations) : List.of();
        transitions = transitions != null ? List.copyOf(transitions) : List.of();
    }
}
