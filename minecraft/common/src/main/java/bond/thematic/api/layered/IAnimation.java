package bond.thematic.api.layered;


import bond.thematic.api.TransformType;
import bond.thematic.api.firstPerson.FirstPersonConfiguration;
import bond.thematic.api.firstPerson.FirstPersonMode;
import bond.thematic.api.core.util.Vec3f;
import org.jetbrains.annotations.NotNull;

/**
 * An entry in {@link AnimationStack}, used to get the animated parts current transform
 */
public interface IAnimation {
    enum KeyframeType {
        ADDITIVE,
        STATIC
    }

    enum PlayMode {
        ONCE,
        LOOP,
        HOLD
    }

    /**
     * Get the type of the keyframe animation
     * 
     * @return keyframe type
     */
    default KeyframeType getKeyframeType() {
        return KeyframeType.ADDITIVE;
    }

    /**
     * Get the playback mode of the animation
     * 
     * @return playback mode
     */
    default PlayMode getPlayMode() {
        return PlayMode.ONCE;
    }

    /**
     * Animation tick, on lag free client 20 [tick/sec]
     * You can get the animations time from other places, but it will be invoked when the animation is ACTIVE
     */
    default void tick(){}

    /**
     * Is the animation currently active.
     * Tick will only be invoked when ACTIVE
     * @return active
     */
    boolean isActive();

    /**
     * Get the transformed value to a model part, transform type.
     * @param modelName The questionable model part
     * @param type      Transform type
     * @param tickDelta Time since the last tick. 0-1
     * @param value0    The value before the transform. For identity transform return with it.
     * @return The new transform value
     */
    @NotNull Vec3f get3DTransform(@NotNull String modelName, @NotNull TransformType type, float tickDelta, @NotNull Vec3f value0);

    /**
     * Called before rendering a character
     * @param tickDelta Time since the last tick. 0-1
     */
    void setupAnim(float tickDelta);

    /**
     * Is the part explicitly animated in this layer/animation?
     * @param partName The part name
     * @return true if animated
     */
    default boolean isPartAnimated(String partName) {
        return false;
    }
}