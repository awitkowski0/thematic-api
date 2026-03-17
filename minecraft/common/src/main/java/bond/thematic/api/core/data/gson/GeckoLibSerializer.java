package bond.thematic.api.core.data.gson;

import com.google.gson.*;
import bond.thematic.api.TransformType;
import bond.thematic.api.core.data.AnimationFormat;
import bond.thematic.api.core.data.KeyframeAnimation;
import bond.thematic.api.core.util.Ease;
import bond.thematic.api.core.util.Easing;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GeckoLibSerializer {

    public static List<KeyframeAnimation> serialize(JsonObject node) {
        try {
            return readAnimations(node.get("animations").getAsJsonObject());
        } catch (Exception e) {
            throw new JsonParseException("Failed to parse animations", e);
        }
    }

    private static List<KeyframeAnimation> readAnimations(JsonObject jsonEmotes) {
        List<KeyframeAnimation> emotes = new ArrayList<>();
        for (Map.Entry<String, JsonElement> entry : jsonEmotes.entrySet()) {
            try {
                KeyframeAnimation.AnimationBuilder builder = new KeyframeAnimation.AnimationBuilder(AnimationFormat.JSON_MC_ANIM);
                String name = entry.getKey();
                JsonObject node = entry.getValue().getAsJsonObject();
                builder.name = name;

                processAnimationMetadata(builder, node);

                if (node.has("bones")) {
                    keyframeSerializer(builder, node.get("bones").getAsJsonObject());
                }

                emotes.add(builder.build());
            } catch (Exception e) {
            }
        }
        return emotes;
    }

    private static void processAnimationMetadata(KeyframeAnimation.AnimationBuilder builder, JsonObject node) {
        if (node.has("animation_length")) {
            float length = safeParseFloat(node.get("animation_length"), 0f);
            builder.endTick = (int) Math.ceil(length * 20);

            processLoopMetadata(builder, node);
        } else if (node.has("loop") && safeParseBoolean(node.get("loop"), false)) {
            builder.endTick = builder.stopTick = 1;
            builder.isLooped = true;
            builder.returnTick = 0;
        } else {
            throw new JsonParseException("Could not recognize GeckoLib animation: " + builder.name);
        }

        builder.fullyEnableParts();
        builder.optimizeEmote();
    }

    private static void processLoopMetadata(KeyframeAnimation.AnimationBuilder builder, JsonObject node) {
        JsonElement loopElement = node.get("loop");
        if (loopElement != null) {
            if (loopElement.isJsonPrimitive()) {
                if (loopElement.getAsJsonPrimitive().isBoolean()) {
                    builder.isLooped = loopElement.getAsBoolean();
                    if (!builder.isLooped) {
                        builder.endTick--;
                    }
                } else if (loopElement.getAsJsonPrimitive().isString()) {
                    String loopString = loopElement.getAsString();
                    if (loopString.equals("hold_on_last_frame")) {
                        builder.isLooped = true;
                        builder.returnTick = builder.endTick;
                    } else if (loopString.equals("loop") || loopString.equals("true")) {
                        builder.isLooped = true;
                    }
                }
            }
        }
    }

    private static void keyframeSerializer(KeyframeAnimation.AnimationBuilder emoteData, JsonObject node) {
        for (Map.Entry<String, JsonElement> entry : node.entrySet()) {
            try {
                String boneName = snake2Camel(entry.getKey());
                readBone(
                        emoteData.getOrCreatePart(boneName),
                        entry.getValue().getAsJsonObject(),
                        emoteData
                );
            } catch (Exception e) {
            }
        }
    }

    private static void readBone(KeyframeAnimation.StateCollection stateCollection, JsonObject node, KeyframeAnimation.AnimationBuilder emoteData) {
        processTransformation(node, "rotation", stateCollection, emoteData, false);
        processTransformation(node, "position", stateCollection, emoteData, true);
    }

    private static void processTransformation(JsonObject node, String transformType,
                                              KeyframeAnimation.StateCollection stateCollection,
                                              KeyframeAnimation.AnimationBuilder emoteData,
                                              boolean isPosition) {
        if (!node.has(transformType)) return;

        JsonElement jsonTransform = node.get(transformType);
        KeyframeAnimation.StateCollection.State[] targetStates = isPosition ?
                getOffs(stateCollection) : getRots(stateCollection);
        boolean[] wrappedRotationChannels = !isPosition ? detectWrappedRotationChannels(jsonTransform) : null;

        // If it's a simple array, treat as initial/default vector
        if (jsonTransform.isJsonArray()) {
            readCollection(targetStates, 0, Ease.LINEAR, jsonTransform.getAsJsonArray(), emoteData, isPosition, wrappedRotationChannels);
            return;
        }

        // If it's an object, more complex parsing
        if (jsonTransform.isJsonObject()) {
            JsonObject transformObj = jsonTransform.getAsJsonObject();

            // Handle 'vector' key
            if (transformObj.has("vector")) {
                readCollection(targetStates, 0, Ease.LINEAR,
                               transformObj.get("vector").getAsJsonArray(), emoteData, isPosition, wrappedRotationChannels);
            }

            // Handle keyframe-specific entries
            for (Map.Entry<String, JsonElement> entry : transformObj.entrySet()) {
                if (entry.getKey().equals("vector") || entry.getKey().equals("easing")) continue;

                try {
                    int tick = (int) (Float.parseFloat(entry.getKey()) * 20);

                    if (entry.getValue().isJsonArray()) {
                        // Simple vector at a specific tick
                        readCollection(targetStates, tick, Ease.LINEAR,
                                       entry.getValue().getAsJsonArray(), emoteData, isPosition, wrappedRotationChannels);
                    } else if (entry.getValue().isJsonObject()) {
                        // Detailed keyframe data
                        readDataAtTick(
                                entry.getValue().getAsJsonObject(),
                                stateCollection,
                                tick,
                                emoteData,
                                isPosition,
                                wrappedRotationChannels
                        );
                    }
                } catch (NumberFormatException e) {
                    //LOGGER.warn("Skipping invalid tick key '{}' for {}", entry.getKey(), transformType);
                }
            }
        }
    }

    private static void readDataAtTick(JsonObject currentNode, KeyframeAnimation.StateCollection stateCollection,
                                       int tick, KeyframeAnimation.AnimationBuilder emoteData, boolean isPos,
                                       boolean[] wrappedRotationChannels) {
        Ease ease = determineEase(currentNode, isPos);
        KeyframeAnimation.StateCollection.State[] targetVec = isPos ? getOffs(stateCollection) : getRots(stateCollection);

        // Handle 'pre' vector
        if (currentNode.has("pre")) {
            readCollection(targetVec, tick, ease, getVector(currentNode.get("pre")), emoteData, isPos, wrappedRotationChannels);
        }

        // Handle 'vector'
        if (currentNode.has("vector")) {
            readCollection(targetVec, tick, ease, currentNode.get("vector").getAsJsonArray(), emoteData, isPos, wrappedRotationChannels);
        }

        // Handle 'post' vector
        if (currentNode.has("post")) {
            readCollection(targetVec, tick, ease, getVector(currentNode.get("post")), emoteData, isPos, wrappedRotationChannels);
        }
    }

    private static Ease determineEase(JsonObject currentNode, boolean isPosition) {
        Ease ease = Ease.LINEAR;

        // Check lerp_mode first
        if (currentNode.has("lerp_mode")) {
            String lerp = safeParseString(currentNode.get("lerp_mode"), "linear");
            ease = lerp.equals("catmullrom") ? Ease.INOUTSINE : Easing.easeFromString(lerp);

            // GeckoLib catmullrom on Euler rotations can overshoot between keyframes and
            // produce visible torso jitter/snaps in some armorBody-heavy moves.
            if (!isPosition && lerp.equals("catmullrom")) {
                ease = Ease.LINEAR;
            }
        }

        // Explicit easing can override lerp_mode
        if (currentNode.has("easing")) {
            ease = Easing.easeFromString(safeParseString(currentNode.get("easing"), "linear"));
        }

        return ease;
    }

    // Utility methods for safe parsing
    private static float safeParseFloat(JsonElement element, float defaultValue) {
        if (element == null) return defaultValue;

        try {
            if (element.isJsonPrimitive()) {
                JsonPrimitive primitive = element.getAsJsonPrimitive();
                if (primitive.isNumber()) return primitive.getAsFloat();
                if (primitive.isString()) {
                    // Handle string inputs like "linear", "step", etc.
                    String strValue = primitive.getAsString().toLowerCase();
                    switch (strValue) {
                        case "linear":
                        case "step":
                            return 0f;
                        default:
                            try {
                                return Float.parseFloat(strValue);
                            } catch (NumberFormatException e) {
                                //LOGGER.warn("Could not parse float from '{}', using default", strValue);
                                return defaultValue;
                            }
                    }
                }
            }
        } catch (Exception e) {
//            LOGGER.warn("Error parsing float", e);
        }
        return defaultValue;
    }

    private static boolean safeParseBoolean(JsonElement element, boolean defaultValue) {
        if (element == null) return defaultValue;

        try {
            if (element.isJsonPrimitive()) {
                JsonPrimitive primitive = element.getAsJsonPrimitive();
                if (primitive.isBoolean()) return primitive.getAsBoolean();
                if (primitive.isString()) {
                    String strValue = primitive.getAsString().toLowerCase();
                    return strValue.equals("true") || strValue.equals("1");
                }
            }
        } catch (Exception e) {
//            LOGGER.warn("Error parsing boolean", e);
        }
        return defaultValue;
    }

    private static String safeParseString(JsonElement element, String defaultValue) {
        if (element == null) return defaultValue;

        try {
            if (element.isJsonPrimitive()) {
                JsonPrimitive primitive = element.getAsJsonPrimitive();
                if (primitive.isString()) return primitive.getAsString();
                return primitive.toString();
            }
        } catch (Exception e) {
//            LOGGER.warn("Error parsing string", e);
        }
        return defaultValue;
    }

    // Existing utility methods remain the same...
    public static JsonArray getVector(JsonElement element) {
        if (element.isJsonArray()) return element.getAsJsonArray();
        else return ((JsonObject)element).get("vector").getAsJsonArray();
    }

    private static void readCollection(KeyframeAnimation.StateCollection.State[] a, int tick, Ease ease, JsonArray array, KeyframeAnimation.AnimationBuilder emoteData, boolean isPos, boolean[] wrappedRotationChannels){
        if(a.length != 3)throw new ArrayStoreException("wrong array length");
        for(int i = 0; i < 3; i++){
            float value = array.get(i).getAsFloat();
            if (isPos) {
                // IMPORTANT: Only the global character translation (mapped to 'body' field) 
                // is scaled to blocks (1/16). Limbs/Torso use pixel units.
                if (a[0] == emoteData.body.x) {
                    value = value / 16f;
                    if (i == 0) value = -value; // Flip X for block-space global translation
                }
                
                // GeckoLib Y is up, Minecraft Y is down
                if (i == 1) {
                    value = -value;
                }
            } else {
                // Only root body rotations are inverted to match world-space transform axes.
                // Torso/head inversion is applied consistently per wrapped channel.
                boolean isBody = a[0] == emoteData.body.pitch;
                boolean isTorsoOrHead = a[0] == emoteData.torso.pitch || a[0] == emoteData.head.pitch;
                boolean shouldInvertWrappedChannel = isTorsoOrHead && wrappedRotationChannels != null && wrappedRotationChannels[i];
                boolean shouldInvertRot = isBody || shouldInvertWrappedChannel;
                if (shouldInvertRot && i != 2) {
                    value = -value;
                }
            }
            value += a[i].defaultValue;
            a[i].addKeyFrame(tick, value, ease, 0, true);
        }
    }

    private static boolean[] detectWrappedRotationChannels(JsonElement transformElement) {
        boolean[] wrapped = new boolean[] {false, false, false};
        collectWrappedRotationChannels(transformElement, wrapped);
        return wrapped;
    }

    private static void collectWrappedRotationChannels(JsonElement element, boolean[] wrapped) {
        if (element == null) return;

        if (element.isJsonArray()) {
            markWrappedFromArray(element.getAsJsonArray(), wrapped);
            return;
        }

        if (!element.isJsonObject()) return;

        JsonObject obj = element.getAsJsonObject();
        if (obj.has("vector")) {
            markWrappedFromArray(obj.get("vector").getAsJsonArray(), wrapped);
        }
        if (obj.has("pre")) {
            markWrappedFromArray(getVector(obj.get("pre")), wrapped);
        }
        if (obj.has("post")) {
            markWrappedFromArray(getVector(obj.get("post")), wrapped);
        }

        for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
            if (entry.getKey().equals("vector") || entry.getKey().equals("pre") || entry.getKey().equals("post") ||
                    entry.getKey().equals("easing") || entry.getKey().equals("lerp_mode")) {
                continue;
            }
            collectWrappedRotationChannels(entry.getValue(), wrapped);
        }
    }

    private static void markWrappedFromArray(JsonArray array, boolean[] wrapped) {
        if (array == null) return;
        int limit = Math.min(3, array.size());
        for (int i = 0; i < limit; i++) {
            try {
                float value = array.get(i).getAsFloat();
                if (Math.abs(value) > 180f) {
                    wrapped[i] = true;
                }
            } catch (Exception ignored) {
            }
        }
    }


    /**
     * Convert snake_case_string to camelCaseString
     * @param original string_to_convert
     * @return         camelCaseString
     */
    public static String snake2Camel(String original) {
        StringBuilder builder = new StringBuilder();
        StringReader reader = new StringReader(original);
        int c;
        boolean upperNext = false;
        try {
            while ((c = reader.read()) != -1) {
                if (c == '_') {
                    upperNext = true;
                    continue;
                }
                if (upperNext) {
                    builder.appendCodePoint(Character.toUpperCase(c));
                } else {
                    builder.appendCodePoint(c);
                }
                upperNext = false;
            }
        } catch(IOException ignore) {
            return original;
        }
        return builder.toString();
    }

    private static KeyframeAnimation.StateCollection.State[] getRots(KeyframeAnimation.StateCollection stateCollection){
        return new KeyframeAnimation.StateCollection.State[] {stateCollection.pitch, stateCollection.yaw, stateCollection.roll};
    }

    private static KeyframeAnimation.StateCollection.State[] getOffs(KeyframeAnimation.StateCollection stateCollection){
        return new KeyframeAnimation.StateCollection.State[] {stateCollection.x, stateCollection.y, stateCollection.z};
    }

    public static KeyframeAnimation.StateCollection.State[] getTargetVec(KeyframeAnimation.StateCollection stateCollection, TransformType type){
        switch (type) {
            case POSITION:
                return new KeyframeAnimation.StateCollection.State[] {
                        stateCollection.x, stateCollection.y, stateCollection.z
                };

            case ROTATION:
                return new KeyframeAnimation.StateCollection.State[] {
                        stateCollection.pitch, stateCollection.yaw, stateCollection.roll
                };

            case SCALE:
                return new KeyframeAnimation.StateCollection.State[] {
                        stateCollection.scaleX, stateCollection.scaleY, stateCollection.scaleZ
                };

            case BEND:
                return new KeyframeAnimation.StateCollection.State[] {
                        stateCollection.bend, stateCollection.bendDirection
                };

            default:
                return new KeyframeAnimation.StateCollection.State[0];
        }
    }
}