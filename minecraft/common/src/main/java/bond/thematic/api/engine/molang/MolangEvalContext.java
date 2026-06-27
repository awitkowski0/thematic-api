package bond.thematic.api.engine.molang;

import net.minecraft.entity.LivingEntity;

public class MolangEvalContext {
    private static final Molang MOLANG = new Molang();
    private static final ThreadLocal<LivingEntity> ENTITY = new ThreadLocal<>();
    private static final ThreadLocal<Double> ANIM_TIME = new ThreadLocal<>();

    public static void set(LivingEntity entity, double animTime) {
        ENTITY.set(entity);
        ANIM_TIME.set(animTime);
    }

    public static void clear() {
        ENTITY.remove();
        ANIM_TIME.remove();
    }

    public static double evaluate(String expression) {
        LivingEntity entity = ENTITY.get();
        Double animTime = ANIM_TIME.get();
        if (entity == null || expression == null) return 0.0;
        MOLANG.entityQueries().setEntity(entity);
        if (animTime != null) {
            MOLANG.builtins().setAnimTime(animTime);
            MOLANG.builtins().setLifeTime(animTime);
            MOLANG.builtins().setCurrentTick(animTime * 20.0);
        }
        return MOLANG.eval(expression);
    }
}
