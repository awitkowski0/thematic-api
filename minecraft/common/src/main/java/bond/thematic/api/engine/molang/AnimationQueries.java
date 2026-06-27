package bond.thematic.api.engine.molang;

public class AnimationQueries {
    private double stateTime;
    private double currentState;

    public void applyTo(MolangEnvironment env) {
        env.registerQuery("state_time", () -> stateTime);
        env.registerQuery("current_state", () -> currentState);
    }

    public void setStateTime(double value) { stateTime = value; }
    public void setCurrentState(double value) { currentState = value; }
}
