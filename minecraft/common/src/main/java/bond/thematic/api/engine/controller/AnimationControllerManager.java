package bond.thematic.api.engine.controller;

import bond.thematic.api.engine.molang.Molang;
import net.minecraft.entity.LivingEntity;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AnimationControllerManager {
    private static final AnimationControllerManager INSTANCE = new AnimationControllerManager();
    private final Map<String, AnimationControllerSet> loadedSets = new HashMap<>();
    private final Map<UUID, Map<String, ControllerRuntime>> entityRuntimes = new ConcurrentHashMap<>();

    public static AnimationControllerManager getInstance() {
        return INSTANCE;
    }

    public void loadControllerSet(String id, String json) {
        AnimationControllerSet set = ControllerSerializer.fromJson(json);
        loadedSets.put(id, set);
    }

    public void loadControllerSet(String id, AnimationControllerSet set) {
        loadedSets.put(id, set);
    }

    public AnimationControllerSet getControllerSet(String id) {
        return loadedSets.get(id);
    }

    public ControllerRuntime getOrCreateRuntime(LivingEntity entity, String controllerId) {
        Map<String, ControllerRuntime> runtimes = entityRuntimes
                .computeIfAbsent(entity.getUuid(), k -> new HashMap<>());
        return runtimes.computeIfAbsent(controllerId, k -> {
            AnimationControllerSet set = loadedSets.get(controllerId);
            if (set == null || set.isEmpty()) return null;
            AnimationController controller = set.controllers().values().iterator().next();
            if (controller == null) return null;
            return new ControllerRuntime(controller, new Molang());
        });
    }

    public void tickEntity(LivingEntity entity, double animTime) {
        Map<String, ControllerRuntime> runtimes = entityRuntimes.get(entity.getUuid());
        if (runtimes == null) return;
        for (ControllerRuntime runtime : runtimes.values()) {
            runtime.tick(entity, animTime);
        }
    }

    public void removeEntity(LivingEntity entity) {
        entityRuntimes.remove(entity.getUuid());
    }

    public void clear() {
        entityRuntimes.clear();
        loadedSets.clear();
    }
}
