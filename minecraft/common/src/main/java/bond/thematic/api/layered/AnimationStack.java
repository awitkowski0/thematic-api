package bond.thematic.api.layered;

import bond.thematic.api.TransformType;
import bond.thematic.api.core.util.Pair;
import bond.thematic.api.core.util.Vec3f;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * Player animation stack, can contain multiple active or passive layers, will always be evaluated from the lowest index.
 * Highest index = it can override everything else
 */
public class AnimationStack implements IAnimation {
    private ArrayList<Pair<Integer, IAnimation>> layers = new ArrayList<>();


    @Override
    public boolean isActive() {
        try {
            if (layers == null || layers.isEmpty()) {
                return false;
            }

            for (Pair<Integer, IAnimation> layer : layers) {
                try {
                    if (layer != null && layer.getRight() != null && layer.getRight().isActive()) {
                        return true;
                    }
                } catch (Exception e) {
                    System.err.println("Error checking animation layer activity: " + e.getMessage());
                    // Continue checking other layers
                }
            }
            return false;
        } catch (Exception e) {
            System.err.println("Animation stack error in isActive: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void tick() {
        try {
            if (layers == null || layers.isEmpty()) {
                return;
            }

            for (Pair<Integer, IAnimation> layer : layers) {
                try {
                    if (layer != null && layer.getRight() != null && layer.getRight().isActive()) {
                        layer.getRight().tick();
                    }
                } catch (Exception e) {
                    System.err.println("Error ticking animation layer: " + e.getMessage());
                    // Continue ticking other layers
                }
            }
        } catch (Exception e) {
            System.err.println("Animation stack error in tick: " + e.getMessage());
        }
    }

    @Override
    public @NotNull Vec3f get3DTransform(@NotNull String modelName, @NotNull TransformType type, float tickDelta, @NotNull Vec3f value0) {
        try {
            // Check if layers is empty to avoid unnecessary iteration
            if (layers == null || layers.isEmpty()) {
                return value0;
            }

            for (Pair<Integer, IAnimation> layer : layers) {
                try {
                    // Null check on layer and its contents
                    if (layer != null && layer.getRight() != null && layer.getRight().isActive()) {
                        value0 = layer.getRight().get3DTransform(modelName, type, tickDelta, value0);
                    }
                } catch (Exception e) {
                    // Log the exception or handle it gracefully
                    System.err.println("Error processing animation layer: " + e.getMessage());
                    // Continue with the next layer instead of crashing
                }
            }
            return value0;
        } catch (Exception e) {
            // Catch any other exceptions that might occur
            System.err.println("Animation stack error: " + e.getMessage());
            // Return the original value if something goes wrong
            return value0;
        }
    }
    @Override
    public void setupAnim(float tickDelta) {
        try {
            if (layers == null || layers.isEmpty()) {
                return;
            }

            for (Pair<Integer, IAnimation> layer : layers) {
                try {
                    if (layer != null && layer.getRight() != null) {
                        layer.getRight().setupAnim(tickDelta);
                    }
                } catch (Exception e) {
                    System.err.println("Error setting up animation layer: " + e.getMessage());
                    // Continue with other layers
                }
            }
        } catch (Exception e) {
            System.err.println("Animation stack error in setupAnim: " + e.getMessage());
        }
    }

    /**
     * Add an animation layer.
     * If there are multiple with the same priority, the one, added first will have larger priority
     * @param priority priority
     * @param layer    animation layer
     * note: Same priority entries logic is subject to change
     */
    public void addAnimLayer(int priority, bond.thematic.api.layered.IAnimation layer) {
        try {
            if (layer == null) {
                System.err.println("Attempted to add null animation layer");
                return;
            }

            if (layers == null) {
                layers = new ArrayList<>(); // This would require changing the final modifier
            }

            int search = 0;
            //Insert the layer into the correct slot
            while (layers.size() > search && layers.get(search).getLeft() < priority) {
                search++;
            }
            layers.add(search, new Pair<>(priority, layer));
        } catch (Exception e) {
            System.err.println("Error adding animation layer: " + e.getMessage());
        }
    }

    /**
     * Remove an animation layer
     * @param layer needle
     * @return true if any elements were removed.
     */
    public boolean removeLayer(IAnimation layer) {
        try {
            if (layers == null || layers.isEmpty() || layer == null) {
                return false;
            }

            return layers.removeIf(integerIAnimationPair ->
                                           integerIAnimationPair != null &&
                                                   integerIAnimationPair.getRight() == layer);
        } catch (Exception e) {
            System.err.println("Error removing animation layer: " + e.getMessage());
            return false;
        }
    }

    /**
     * Remove EVERY layer with priority
     * @param layerLevel search and destroy
     * @return true if any elements were removed.
     */
    public boolean removeLayer(int layerLevel) {
        try {
            if (layers == null || layers.isEmpty()) {
                return false;
            }

            return layers.removeIf(integerIAnimationPair ->
                                           integerIAnimationPair != null &&
                                                   integerIAnimationPair.getLeft() == layerLevel);
        } catch (Exception e) {
            System.err.println("Error removing animation layers by level: " + e.getMessage());
            return false;
        }
    }

    @Override
    public KeyframeType getKeyframeType() {
        // Return the keyframe type of the highest-priority active layer,
        // since AnimationApplier.updatePart() uses this to determine STATIC vs ADDITIVE mode.
        // Default to ADDITIVE if no active layer has its own type.
        try {
            if (layers == null || layers.isEmpty()) {
                return KeyframeType.ADDITIVE;
            }
            // Iterate in reverse (highest priority last = most important)
            for (int i = layers.size() - 1; i >= 0; i--) {
                Pair<Integer, IAnimation> layer = layers.get(i);
                if (layer != null && layer.getRight() != null && layer.getRight().isActive()) {
                    KeyframeType type = layer.getRight().getKeyframeType();
                    if (type != KeyframeType.ADDITIVE) {
                        return type;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Animation stack error in getKeyframeType: " + e.getMessage());
        }
        return KeyframeType.ADDITIVE;
    }

    @Override
    public boolean isPartAnimated(String partName) {
        if (this.layers == null || this.layers.isEmpty()) {
            return false;
        }

        for (Pair<Integer, IAnimation> layer : this.layers) {
            if (layer != null && layer.getRight() != null && layer.getRight().isActive()) {
                if (layer.getRight().isPartAnimated(partName)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public KeyframeType getKeyframeType(String partName) {
        try {
            if (layers == null || layers.isEmpty()) {
                return KeyframeType.ADDITIVE;
            }
            for (int i = layers.size() - 1; i >= 0; i--) {
                Pair<Integer, IAnimation> layer = layers.get(i);
                if (layer != null && layer.getRight() != null && layer.getRight().isActive()) {
                    KeyframeType type = layer.getRight().getKeyframeType(partName);
                    if (type != KeyframeType.ADDITIVE) {
                        return type;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Animation stack error in getKeyframeType(String): " + e.getMessage());
        }
        return KeyframeType.ADDITIVE;
    }
}