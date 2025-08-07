package bond.thematic.api.minecraftApi;

import bond.thematic.api.core.data.KeyframeAnimation;
import bond.thematic.api.core.data.gson.AnimationSerializing;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resource.ResourceManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

/**
 * Load resources from <code>assets/{modid}/player_animation</code>
 * <br>
 * The animation identifier:
 * <table border="1">
 *   <tr>
 *     <td> namespace </td> <td> Mod namespace </td>
 *   </tr>
 *   <tr>
 *     <td> path </td> <td> Animation name, not the filename </td>
 *   </tr>
 * </table>
 * <br>
 * Use {@link PlayerAnimationRegistry#getAnimation(Identifier)} to fetch an animation
 * <br><br>
 * Extra animations can be added by ResourcePack(s) or other mods
 */
@Environment(EnvType.CLIENT)
public final class PlayerAnimationRegistry {

    private static final HashMap<Identifier, KeyframeAnimation> animations = new HashMap<>();

    /**
     * Get an animation from the registry, using Identifier(MODID, animation_name) as key
     * @param identifier identifier
     * @return animation, <code>null</code> if no animation
     */
    @Nullable
    public static KeyframeAnimation getAnimation(@NotNull Identifier identifier) {
        return animations.get(identifier);
    }

    /**
     * Get Optional animation from registry
     * @param identifier identifier
     * @return Optional animation
     */
    @NotNull
    public static Optional<KeyframeAnimation> getAnimationOptional(@NotNull Identifier identifier) {
        return Optional.ofNullable(getAnimation(identifier));
    }

    /**
     * @return an unmodifiable map of all the animations
     */
    public static Map<Identifier, KeyframeAnimation> getAnimations() {
        return Map.copyOf(animations);
    }

    /**
     * Returns the animations of a specific mod/namespace
     * @param modid namespace (assets/modid)
     * @return map of path and animations
     */
    public static Map<String, KeyframeAnimation> getModAnimations(String modid) {
        HashMap<String, KeyframeAnimation> map = new HashMap<>();
        for (Map.Entry<Identifier, KeyframeAnimation> entry: animations.entrySet()) {
            if (entry.getKey().getNamespace().equals(modid)) {
                map.put(entry.getKey().getPath(), entry.getValue());
            }
        }
        return map;
    }

    /**
     * Load animations using ResourceManager
     * Internal use only!
     */
    @ApiStatus.Internal
    public static void resourceLoaderCallback(@NotNull ResourceManager manager, Logger logger) {
        animations.clear();

        // Track the number of successful and failed animations
        int successfulAnimations = 0;
        int failedAnimations = 0;

        for (var resource: manager.findResources("animations/armor", location -> location.getPath().endsWith(".json")).entrySet()) {
            try (var input = resource.getValue().getInputStream()) {
                // Deserialize the animation json. GeckoLib animation json can contain multiple animations.
                List<KeyframeAnimation> loadedAnimations = AnimationSerializing.deserializeAnimation(input);

                for (var animation : loadedAnimations) {
                    try {
                        // Extract animation name, with detailed logging
                        Object nameObj = animation.extraData.get("name");
                        if (nameObj == null) {
                            logger.warn("Animation in {} has no name. Skipping.", resource.getKey());
                            failedAnimations++;
                            continue;
                        }

                        String animationName = serializeTextToString(nameObj.toString()).toLowerCase(Locale.ROOT);
                        Identifier animationKey = new Identifier(
                                resource.getKey().getNamespace(),
                                animationName
                        );

                        // Save the animation for later use
                        animations.put(animationKey, animation);

                        successfulAnimations++;
                    } catch (Exception e) {
                        // Log individual animation loading failures
                        logger.error("Failed to process an animation in {}", resource.getKey(), e);
                        failedAnimations++;
                    }
                }
            } catch(IOException e) {
                // Detailed error logging for resource loading failures
                logger.error("Error while loading player animation resource: {}", resource.getKey(), e);

                // Log full stack trace
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                logger.error("Full stack trace: {}", sw.toString());

                // Continue loading other resources
                failedAnimations++;
            }
        }

        // Log summary of animation loading
        logger.info("Animation Loading Summary - Successful: {}, Failed: {}", successfulAnimations, failedAnimations);
    }

    /**
     * Helper function to convert animation name to string
     */
    public static String serializeTextToString(String arg) {
        try {
            var component = Text.Serializer.fromJson(arg);
            if (component != null) {
                return component.getString();
            }
        } catch(Exception ignored) { }
        return arg.replace("\"", "");
    }
}