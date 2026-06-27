package bond.thematic.api.core.data.gson;

import java.util.HashMap;
import java.util.Map;

public class KeyframeEffectsData {
    public static final String PARTICLE_KEY = "particle_effects";
    public static final String SOUND_KEY = "sound_effects";
    public static final String TIMELINE_KEY = "timeline";

    public record ParticleKeyframeData(String effect, String locator, String script) {
        public ParticleKeyframeData(String effect, String locator, String script) {
            this.effect = effect != null ? effect : "";
            this.locator = locator;
            this.script = script;
        }
    }

    public record SoundKeyframeData(String effect, String locator) {
        public SoundKeyframeData(String effect, String locator) {
            this.effect = effect != null ? effect : "";
            this.locator = locator;
        }
    }

    public static Map<Float, ParticleKeyframeData> parseParticleEffects(com.google.gson.JsonObject obj) {
        Map<Float, ParticleKeyframeData> map = new HashMap<>();
        if (obj == null) return map;
        for (var entry : obj.entrySet()) {
            float time = Float.parseFloat(entry.getKey());
            var data = entry.getValue().getAsJsonObject();
            String effect = data.has("effect") ? data.get("effect").getAsString() : "";
            String locator = data.has("locator") ? data.get("locator").getAsString() : null;
            String script = data.has("pre_effect_script") ? data.get("pre_effect_script").getAsString() : null;
            map.put(time, new ParticleKeyframeData(effect, locator, script));
        }
        return map;
    }

    public static Map<Float, SoundKeyframeData> parseSoundEffects(com.google.gson.JsonObject obj) {
        Map<Float, SoundKeyframeData> map = new HashMap<>();
        if (obj == null) return map;
        for (var entry : obj.entrySet()) {
            float time = Float.parseFloat(entry.getKey());
            var data = entry.getValue().getAsJsonObject();
            String effect = data.has("effect") ? data.get("effect").getAsString() : "";
            String locator = data.has("locator") ? data.get("locator").getAsString() : null;
            map.put(time, new SoundKeyframeData(effect, locator));
        }
        return map;
    }

    public static Map<Float, String> parseTimeline(com.google.gson.JsonObject obj) {
        Map<Float, String> map = new HashMap<>();
        if (obj == null) return map;
        for (var entry : obj.entrySet()) {
            float time = Float.parseFloat(entry.getKey());
            String instruction = entry.getValue().getAsString();
            map.put(time, instruction);
        }
        return map;
    }
}
