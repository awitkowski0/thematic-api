package bond.thematic.api.engine.keyframe;

import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ParticleKeyframeHandler implements AnimationEffectHandler {
    private static final Map<Identifier, Function<ParticleType<?>, ParticleEffect>> complexDefaults = new HashMap<>();

    public static void registerComplexDefault(Identifier id, Function<ParticleType<?>, ParticleEffect> factory) {
        complexDefaults.put(id, factory);
    }

    @Override
    public void start(LivingEntity entity, EffectContext ctx) {
        try {
            String[] args = ctx.args();
            if (args.length < 1) return;
            World world = entity.getWorld();
            ParticleEffect effect = resolveParticle(args[0]);
            if (effect == null) return;
            double x = entity.getX();
            double y = entity.getY() + entity.getHeight() * 0.5;
            double z = entity.getZ();
            double vx = 0, vy = 0, vz = 0;
            if (args.length >= 4) {
                vx = parseDouble(args[1], 0);
                vy = parseDouble(args[2], 0);
                vz = parseDouble(args[3], 0);
            }
            if (world instanceof ServerWorld serverWorld) {
                serverWorld.spawnParticles(effect, x, y, z, 1, vx, vy, vz, 0);
            } else if (world.isClient) {
                world.addParticle(effect, x, y, z, vx, vy, vz);
            }
        } catch (Exception e) {
            System.err.println("[ParticleKeyframeHandler] Failed to spawn particle: " + e);
        }
    }

    private static ParticleEffect resolveParticle(String id) {
        Identifier ident = Identifier.tryParse(id);
        if (ident == null) {
            System.err.println("[ParticleKeyframeHandler] Invalid particle identifier: " + id);
            return null;
        }
        ParticleType<?> type = Registries.PARTICLE_TYPE.get(ident);
        if (type == null) {
            System.err.println("[ParticleKeyframeHandler] Particle type not found in registry: " + ident);
            return null;
        }
        if (type instanceof ParticleEffect effect) return effect;
        Function<ParticleType<?>, ParticleEffect> factory = complexDefaults.get(ident);
        if (factory != null) return factory.apply(type);
        System.err.println("[ParticleKeyframeHandler] No complex default factory registered for: " + ident);
        return null;
    }

    private static double parseDouble(String s, double def) {
        try { return Double.parseDouble(s); } catch (NumberFormatException e) { return def; }
    }
}
