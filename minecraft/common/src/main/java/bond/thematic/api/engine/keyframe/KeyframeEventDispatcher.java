package bond.thematic.api.engine.keyframe;

import bond.thematic.api.core.data.KeyframeAnimation;
import bond.thematic.api.core.data.gson.KeyframeEffectsData;
import net.minecraft.entity.LivingEntity;

import java.util.List;
import java.util.Map;

public class KeyframeEventDispatcher {

    public static void tick(LivingEntity entity, List<KeyframeAnimation> animations, int currentTick) {
        for (KeyframeAnimation anim : animations) {
            tickAnimation(entity, anim, currentTick);
        }
    }

    private static void tickAnimation(LivingEntity entity, KeyframeAnimation anim, int currentTick) {
        @SuppressWarnings("unchecked")
        Map<Float, String> timeline = (Map<Float, String>) anim.extraData.get(KeyframeEffectsData.TIMELINE_KEY);
        if (timeline != null) {
            for (var entry : timeline.entrySet()) {
                int eventTick = (int) (entry.getKey() * 20);
                if (eventTick == currentTick) {
                    String instruction = entry.getValue();
                    AnimationEffectHandler.EffectContext ctx = new AnimationEffectHandler.EffectContext(
                            currentTick / 20.0, 0.05, new String[0]);
                    TimelineInstructionHandler.execute(instruction, entity, ctx);
                }
            }
        }

        @SuppressWarnings("unchecked")
        Map<Float, KeyframeEffectsData.ParticleKeyframeData> particles =
                (Map<Float, KeyframeEffectsData.ParticleKeyframeData>) anim.extraData.get(KeyframeEffectsData.PARTICLE_KEY);
        if (particles != null) {
            for (var entry : particles.entrySet()) {
                int eventTick = (int) (entry.getKey() * 20);
                if (eventTick == currentTick) {
                    KeyframeEffectsData.ParticleKeyframeData data = entry.getValue();
                    ParticleKeyframeHandler handler = new ParticleKeyframeHandler();
                    handler.start(entity, new AnimationEffectHandler.EffectContext(
                            currentTick / 20.0, 0.05, data.effect() != null ? new String[]{data.effect(), data.locator()} : new String[0]));
                }
            }
        }

        @SuppressWarnings("unchecked")
        Map<Float, KeyframeEffectsData.SoundKeyframeData> sounds =
                (Map<Float, KeyframeEffectsData.SoundKeyframeData>) anim.extraData.get(KeyframeEffectsData.SOUND_KEY);
        if (sounds != null) {
            for (var entry : sounds.entrySet()) {
                int eventTick = (int) (entry.getKey() * 20);
                if (eventTick == currentTick) {
                    KeyframeEffectsData.SoundKeyframeData data = entry.getValue();
                    SoundKeyframeHandler handler = new SoundKeyframeHandler();
                    handler.start(entity, new AnimationEffectHandler.EffectContext(
                            currentTick / 20.0, 0.05, data.effect() != null ? new String[]{data.effect(), data.locator()} : new String[0]));
                }
            }
        }
    }
}
