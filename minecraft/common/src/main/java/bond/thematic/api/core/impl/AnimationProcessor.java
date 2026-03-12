package bond.thematic.api.core.impl;


import bond.thematic.api.TransformType;
import bond.thematic.api.layered.IAnimation;
import bond.thematic.api.core.util.Pair;
import bond.thematic.api.core.util.Vec3f;
import org.jetbrains.annotations.ApiStatus;

/**
 * Tool to easily play animation to the player.
 * internal, do not use
 */
@ApiStatus.Internal
public class AnimationProcessor {
    private final IAnimation animation;
    private float tickDelta = 0f;

    public AnimationProcessor(IAnimation animation) {
        this.animation = animation;
    }

    public void tick() {
        animation.tick();
    }

    public boolean isActive() {
        return animation.isActive();
    }

    public Vec3f get3DTransform(String modelName, TransformType type, Vec3f value0) {
        return animation.get3DTransform(modelName, type, this.tickDelta, value0);
    }

    public void setTickDelta(float tickDelta) {
        this.tickDelta = tickDelta;
        this.animation.setupAnim(tickDelta);
    }
    public Pair<Float, Float> getBend(String modelName) {
        Vec3f bendVec = this.get3DTransform(modelName, TransformType.BEND, Vec3f.ZERO);
        return new Pair<>(bendVec.getX(), bendVec.getY());
    }

    public boolean isPartAnimated(String modelName) {
        return animation.isPartAnimated(modelName);
    }

    public IAnimation.KeyframeType getKeyframeType() {
        return animation.getKeyframeType();
    }
}
