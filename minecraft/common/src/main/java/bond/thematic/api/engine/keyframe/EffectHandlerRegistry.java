package bond.thematic.api.engine.keyframe;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class EffectHandlerRegistry {
    private static final Map<String, Supplier<AnimationEffectHandler>> REGISTRY = new HashMap<>();

    public static void register(String id, Supplier<AnimationEffectHandler> factory) {
        REGISTRY.put(id, factory);
    }

    public static void registerAnnotated(Class<? extends AnimationEffectHandler> clazz) {
        EffectKeyframe annotation = clazz.getAnnotation(EffectKeyframe.class);
        if (annotation == null) return;
        String id = annotation.id();
        register(id, () -> {
            try {
                return clazz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Failed to create handler: " + id, e);
            }
        });
    }

    public static AnimationEffectHandler create(String id) {
        Supplier<AnimationEffectHandler> factory = REGISTRY.get(id);
        if (factory == null) return null;
        return factory.get();
    }

    public static boolean hasHandler(String id) {
        return REGISTRY.containsKey(id);
    }

    public static Map<String, Supplier<AnimationEffectHandler>> getAll() {
        return Map.copyOf(REGISTRY);
    }
}
