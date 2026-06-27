package bond.thematic.api.engine.molang;

public class BuiltinQueries {
    private double animTime;
    private double lifeTime;
    private double frameAlpha;
    private double currentTick;

    public void applyTo(MolangEnvironment env) {
        env.registerQuery("anim_time", () -> animTime);
        env.registerQuery("life_time", () -> lifeTime);
        env.registerQuery("frame_alpha", () -> frameAlpha);
        env.registerQuery("current_tick", () -> currentTick);
    }

    public void setAnimTime(double value) { animTime = value; }
    public void setLifeTime(double value) { lifeTime = value; }
    public void setFrameAlpha(double value) { frameAlpha = value; }
    public void setCurrentTick(double value) { currentTick = value; }
}
