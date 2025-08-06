package com.funalex.themAnim.core.data.gson;

import com.google.gson.*;
import com.funalex.themAnim.api.TransformType;
import com.funalex.themAnim.core.data.AnimationFormat;
import com.funalex.themAnim.core.data.KeyframeAnimation;
import com.funalex.themAnim.core.util.Ease;
import com.funalex.themAnim.core.util.Easing;

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
                    }
                }
            }
        }
    }

    private static void keyframeSerializer(KeyframeAnimation.AnimationBuilder emoteData, JsonObject node) {
        for (Map.Entry<String, JsonElement> entry : node.entrySet()) {
            try {
                readBone(
                        emoteData.getOrCreatePart(snake2Camel(entry.getKey())),
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

        // If it's a simple array, treat as initial/default vector
        if (jsonTransform.isJsonArray()) {
            readCollection(targetStates, 0, Ease.LINEAR, jsonTransform.getAsJsonArray(), emoteData, isPosition);
            return;
        }

        // If it's an object, more complex parsing
        if (jsonTransform.isJsonObject()) {
            JsonObject transformObj = jsonTransform.getAsJsonObject();

            // Handle 'vector' key
            if (transformObj.has("vector")) {
                readCollection(targetStates, 0, Ease.LINEAR,
                               transformObj.get("vector").getAsJsonArray(), emoteData, isPosition);
            }

            // Handle keyframe-specific entries
            for (Map.Entry<String, JsonElement> entry : transformObj.entrySet()) {
                if (entry.getKey().equals("vector") || entry.getKey().equals("easing")) continue;

                try {
                    int tick = (int) (Float.parseFloat(entry.getKey()) * 20);

                    if (entry.getValue().isJsonArray()) {
                        // Simple vector at a specific tick
                        readCollection(targetStates, tick, Ease.LINEAR,
                                       entry.getValue().getAsJsonArray(), emoteData, isPosition);
                    } else if (entry.getValue().isJsonObject()) {
                        // Detailed keyframe data
                        readDataAtTick(
                                entry.getValue().getAsJsonObject(),
                                stateCollection,
                                tick,
                                emoteData,
                                isPosition
                        );
                    }
                } catch (NumberFormatException e) {
                    //LOGGER.warn("Skipping invalid tick key '{}' for {}", entry.getKey(), transformType);
                }
            }
        }
    }

    private static void readDataAtTick(JsonObject currentNode, KeyframeAnimation.StateCollection stateCollection,
                                       int tick, KeyframeAnimation.AnimationBuilder emoteData, boolean isPos) {
        Ease ease = determineEase(currentNode);
        KeyframeAnimation.StateCollection.State[] targetVec = isPos ? getOffs(stateCollection) : getRots(stateCollection);

        // Handle 'pre' vector
        if (currentNode.has("pre")) {
            readCollection(targetVec, tick, ease, getVector(currentNode.get("pre")), emoteData, isPos);
        }

        // Handle 'vector'
        if (currentNode.has("vector")) {
            readCollection(targetVec, tick, ease, currentNode.get("vector").getAsJsonArray(), emoteData, isPos);
        }

        // Handle 'post' vector
        if (currentNode.has("post")) {
            readCollection(targetVec, tick, ease, getVector(currentNode.get("post")), emoteData, isPos);
        }
    }

    private static Ease determineEase(JsonObject currentNode) {
        Ease ease = Ease.LINEAR;

        // Check lerp_mode first
        if (currentNode.has("lerp_mode")) {
            String lerp = safeParseString(currentNode.get("lerp_mode"), "linear");
            ease = lerp.equals("catmullrom") ? Ease.INOUTSINE : Easing.easeFromString(lerp);
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

    private static void readCollection(KeyframeAnimation.StateCollection.State[] a, int tick, Ease ease, JsonArray array, KeyframeAnimation.AnimationBuilder emoteData, boolean isPos){
        if(a.length != 3)throw new ArrayStoreException("wrong array length");
        for(int i = 0; i < 3; i++){
            float value = array.get(i).getAsFloat();
            if (isPos) {
                if (a[0] == emoteData.body.x) {
                    value = value / 16f;
                    if (i == 0) value = -value;
                }
                else if (i == 1) {
                    value = -value;
                }
            } else {
                if (a[0] == emoteData.body.pitch && i != 2) {
                    value = -value;
                }
            }
            value += a[i].defaultValue;
            a[i].addKeyFrame(tick, value, ease, 0, true);
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