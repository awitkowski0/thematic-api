package bond.thematic.api.layered;

import bond.thematic.api.core.data.KeyframeAnimation;
import bond.thematic.api.TransformType;
import bond.thematic.api.core.util.Vec3f;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * StateMachineAnimation allows for logic-driven animation switching.
 * It takes a function that determines the animation based on the player's state.
 * It handles fading between animations when the state changes.
 */
public class StateMachineAnimation implements IAnimation {
    private final PlayerEntity player;
    private final Function<PlayerEntity, KeyframeAnimation> stateProvider;
    private KeyframeAnimation currentData;
    private KeyframeAnimationPlayer currentPlayer;
    private KeyframeAnimationPlayer nextPlayer;
    private float fadeProgress = 1.0f;
    private final float fadeSpeed;
    private boolean initialized = false;

    /**
     * @param player The player this animation is for
     * @param fadeTime How long (in ticks) to fade between states
     * @param stateProvider Function to determine the current desired animation
     */
    public StateMachineAnimation(PlayerEntity player, float fadeTime, Function<PlayerEntity, KeyframeAnimation> stateProvider) {
        this.player = player;
        this.stateProvider = stateProvider;
        this.fadeSpeed = 1.0f / Math.max(1, fadeTime);
        this.currentData = null;
    }

    @Override
    public void tick() {
        this.initialized = true;
        KeyframeAnimation desired = stateProvider.apply(player);

        if (desired != currentData) {
            String from = (currentData != null) ? (String)currentData.extraData.get("name") : "null";
            String to = (desired != null) ? (String)desired.extraData.get("name") : "null";
            System.out.println("[StateMachine] Transition: " + from + " -> " + to);
            
            // State changed, start a new fade
            currentData = desired;
            if (currentPlayer == null) {
                if (desired != null) {
                    currentPlayer = new KeyframeAnimationPlayer(desired);
                }
                fadeProgress = 1.0f;
            } else {
                if (desired != null) {
                    nextPlayer = new KeyframeAnimationPlayer(desired);
                    fadeProgress = 0.0f;
                } else {
                    // Fading out to nothing
                    nextPlayer = null;
                    fadeProgress = 0.0f;
                }
            }
        }

        if (fadeProgress < 1.0f) {
            fadeProgress += fadeSpeed;
            if (fadeProgress >= 1.0f) {
                currentPlayer = nextPlayer;
                nextPlayer = null;
                fadeProgress = 1.0f;
            }
        }

        if (currentPlayer != null && currentPlayer.isActive()) {
            currentPlayer.tick();
        }
        if (nextPlayer != null && nextPlayer.isActive()) {
            nextPlayer.tick();
        }
    }

    @Override
    public boolean isActive() {
        return !initialized ||
               (currentPlayer != null && currentPlayer.isActive()) || 
               (nextPlayer != null && nextPlayer.isActive()) ||
               (fadeProgress < 1.0f);
    }

    @Override
    public @NotNull Vec3f get3DTransform(@NotNull String modelName, @NotNull TransformType type, float tickDelta, @NotNull Vec3f value0) {
        if (nextPlayer == null) {
            if (currentPlayer != null && currentPlayer.isActive()) {
                return currentPlayer.get3DTransform(modelName, type, tickDelta, value0);
            }
            return value0;
        }

        // Handle Cross-Fade
        Vec3f currentVal = (currentPlayer != null && currentPlayer.isActive()) ? 
            currentPlayer.get3DTransform(modelName, type, tickDelta, value0) : value0;
        
        Vec3f nextVal = (nextPlayer != null && nextPlayer.isActive()) ? 
            nextPlayer.get3DTransform(modelName, type, tickDelta, value0) : value0;

        float alpha = fadeProgress;
        
        // Linear interpolation between the two animation states
        return new Vec3f(
            lerp(alpha, currentVal.getX(), nextVal.getX()),
            lerp(alpha, currentVal.getY(), nextVal.getY()),
            lerp(alpha, currentVal.getZ(), nextVal.getZ())
        );
    }

    private float lerp(float delta, float start, float end) {
        return start + delta * (end - start);
    }

    @Override
    public void setupAnim(float tickDelta) {
        if (currentPlayer != null) currentPlayer.setupAnim(tickDelta);
        if (nextPlayer != null) nextPlayer.setupAnim(tickDelta);
    }

    @Override
    public boolean isPartAnimated(String partName) {
        return (currentPlayer != null && currentPlayer.isPartAnimated(partName)) || 
               (nextPlayer != null && nextPlayer.isPartAnimated(partName));
    }

    @Override
    public KeyframeType getKeyframeType() {
        // If either animation is STATIC, we treat the controller as STATIC
        if (currentPlayer != null && currentPlayer.getKeyframeType() == KeyframeType.STATIC) return KeyframeType.STATIC;
        if (nextPlayer != null && nextPlayer.getKeyframeType() == KeyframeType.STATIC) return KeyframeType.STATIC;
        return KeyframeType.ADDITIVE;
    }
}
