package bond.thematic.api.engine.keyframe;

import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class SoundKeyframeHandler implements AnimationEffectHandler {
    @Override
    public void start(LivingEntity entity, EffectContext ctx) {
        String[] args = ctx.args();
        if (args.length < 1) return;
        Identifier id = Identifier.tryParse(args[0]);
        if (id == null) return;
        SoundEvent sound = Registries.SOUND_EVENT.get(id);
        if (sound == null) {
            System.err.println("[SoundKeyframeHandler] Sound not found in registry: " + args[0]);
            return;
        }
        float volume = args.length > 1 ? parseFloat(args[1], 1.0f) : 1.0f;
        float pitch = args.length > 2 ? parseFloat(args[2], 1.0f) : 1.0f;
        entity.getWorld().playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                sound, SoundCategory.PLAYERS, volume, pitch);
    }

    private static float parseFloat(String s, float def) {
        try { return Float.parseFloat(s); } catch (NumberFormatException e) { return def; }
    }
}
