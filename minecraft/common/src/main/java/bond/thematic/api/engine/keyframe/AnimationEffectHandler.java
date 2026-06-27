package bond.thematic.api.engine.keyframe;

import net.minecraft.entity.LivingEntity;

public interface AnimationEffectHandler {
    default void start(LivingEntity entity, EffectContext ctx) {}
    default void tick(LivingEntity entity, EffectContext ctx) {}
    default void stop(LivingEntity entity) {}
    default boolean isFinished(LivingEntity entity) { return true; }

    record EffectContext(
            double animTime,
            double deltaTime,
            String[] args
    ) {
        public String arg(int index) {
            return args != null && index < args.length ? args[index] : "";
        }
    }
}
