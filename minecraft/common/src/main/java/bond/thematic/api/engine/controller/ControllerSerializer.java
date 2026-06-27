package bond.thematic.api.engine.controller;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.*;

public class ControllerSerializer implements JsonDeserializer<AnimationControllerSet> {

    public static final Gson GSON;

    static {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        builder.registerTypeAdapter(AnimationControllerSet.class, new ControllerSerializer());
        GSON = builder.create();
    }

    @Override
    public AnimationControllerSet deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject root = json.getAsJsonObject();
        JsonObject controllersObj = root.getAsJsonObject("animation_controllers");
        if (controllersObj == null) return new AnimationControllerSet(Map.of());

        Map<String, AnimationController> controllers = new LinkedHashMap<>();
        for (var entry : controllersObj.entrySet()) {
            String controllerName = entry.getKey();
            AnimationController controller = parseController(controllerName, entry.getValue().getAsJsonObject());
            controllers.put(controllerName, controller);
        }
        return new AnimationControllerSet(controllers);
    }

    private AnimationController parseController(String name, JsonObject obj) {
        String initialState = obj.has("initial_state") ? obj.get("initial_state").getAsString() : "default";
        JsonObject statesObj = obj.getAsJsonObject("states");
        if (statesObj == null) return new AnimationController(name, initialState, Map.of());

        Map<String, AnimationState> states = new LinkedHashMap<>();
        for (var entry : statesObj.entrySet()) {
            states.put(entry.getKey(), parseState(entry.getKey(), entry.getValue().getAsJsonObject()));
        }
        return new AnimationController(name, initialState, states);
    }

    private AnimationState parseState(String name, JsonObject obj) {
        List<String> animations = new ArrayList<>();
        if (obj.has("animations")) {
            JsonElement anims = obj.get("animations");
            if (anims.isJsonArray()) {
                for (JsonElement e : anims.getAsJsonArray()) {
                    animations.add(e.getAsString());
                }
            } else if (anims.isJsonPrimitive()) {
                animations.add(anims.getAsString());
            }
        }

        List<AnimationTransition> transitions = new ArrayList<>();
        if (obj.has("transitions")) {
            for (JsonElement t : obj.getAsJsonArray("transitions")) {
                if (t.isJsonObject()) {
                    JsonObject transitionObj = t.getAsJsonObject();
                    if (transitionObj.has("target") && transitionObj.has("condition")) {
                        transitions.add(new AnimationTransition(
                                transitionObj.get("target").getAsString(),
                                transitionObj.get("condition").getAsString()
                        ));
                    }
                }
            }
        }

        return new AnimationState(name, animations, transitions);
    }

    public static AnimationControllerSet fromJson(String json) {
        return GSON.fromJson(json, AnimationControllerSet.class);
    }

    public static String toJson(AnimationControllerSet set) {
        JsonObject root = new JsonObject();
        JsonObject controllersObj = new JsonObject();
        for (AnimationController controller : set.controllers().values()) {
            controllersObj.add(controller.name(), serializeController(controller));
        }
        root.add("animation_controllers", controllersObj);
        return GSON.toJson(root);
    }

    private static JsonObject serializeController(AnimationController controller) {
        JsonObject obj = new JsonObject();
        obj.addProperty("initial_state", controller.initialState());
        JsonObject statesObj = new JsonObject();
        for (AnimationState state : controller.states().values()) {
            statesObj.add(state.name(), serializeState(state));
        }
        obj.add("states", statesObj);
        return obj;
    }

    private static JsonObject serializeState(AnimationState state) {
        JsonObject obj = new JsonObject();
        if (!state.animations().isEmpty()) {
            JsonArray anims = new JsonArray();
            for (String a : state.animations()) {
                anims.add(a);
            }
            obj.add("animations", anims);
        }
        if (!state.transitions().isEmpty()) {
            JsonArray transitions = new JsonArray();
            for (AnimationTransition t : state.transitions()) {
                JsonObject to = new JsonObject();
                to.addProperty("target", t.targetState());
                to.addProperty("condition", t.condition());
                transitions.add(to);
            }
            obj.add("transitions", transitions);
        }
        return obj;
    }
}
