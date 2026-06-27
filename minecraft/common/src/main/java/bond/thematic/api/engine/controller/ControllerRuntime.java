package bond.thematic.api.engine.controller;

import bond.thematic.api.engine.molang.Molang;
import net.minecraft.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

public class ControllerRuntime {
    private final AnimationController controller;
    private final Molang molang;
    private String currentState;
    private double stateStartTime;

    public ControllerRuntime(AnimationController controller, Molang molang) {
        this.controller = controller;
        this.molang = molang;
        this.currentState = controller.initialState();
        this.stateStartTime = 0.0;
    }

    public boolean tick(LivingEntity entity, double animTime) {
        AnimationState state = controller.getState(currentState);
        if (state == null) return false;

        boolean changed = false;
        for (AnimationTransition transition : state.transitions()) {
            molang.entityQueries().setEntity(entity);
            molang.animationQueries().setStateTime(animTime - stateStartTime);
            molang.animationQueries().setCurrentState(hashState(currentState));
            molang.builtins().setAnimTime(animTime);
            molang.builtins().setLifeTime(animTime);
            molang.builtins().setCurrentTick(animTime * 20.0);

            double result = molang.eval(transition.condition(), 0.0);
            if (result != 0.0) {
                currentState = transition.targetState();
                stateStartTime = animTime;
                changed = true;
                break;
            }
        }
        return changed;
    }

    public void setState(String state, double animTime) {
        this.currentState = state;
        this.stateStartTime = animTime;
    }

    public String getCurrentState() {
        return currentState;
    }

    public double getStateTime(double animTime) {
        return animTime - stateStartTime;
    }

    public AnimationController getController() {
        return controller;
    }

    public List<String> getCurrentAnimations() {
        AnimationState state = controller.getState(currentState);
        if (state == null) return List.of();
        return state.animations();
    }

    private static double hashState(String name) {
        int h = name.hashCode();
        return (double) h;
    }
}
