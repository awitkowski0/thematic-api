package bond.thematic.api.engine.keyframe;

import bond.thematic.api.engine.camera.CameraShakeManager;
import net.minecraft.entity.LivingEntity;

public class CameraShakeHandler implements AnimationEffectHandler {
    @Override
    public void start(LivingEntity entity, EffectContext ctx) {
        String[] args = ctx.args();
        float intensity = args.length > 0 ? parseFloat(args[0], 0.5f) : 0.5f;
        int duration = args.length > 1 ? parseInt(args[1], 10) : 10;
        CameraShakeManager.trigger(intensity, duration);
    }

    private static float parseFloat(String s, float def) {
        try { return Float.parseFloat(s); } catch (NumberFormatException e) { return def; }
    }

    private static int parseInt(String s, int def) {
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return def; }
    }
}
