package bond.thematic.api.engine.keyframe;

import net.minecraft.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;

public class TimelineInstructionHandler {
    private static final Map<String, InstructionParser> CUSTOM_PARSERS = new HashMap<>();

    @FunctionalInterface
    public interface InstructionParser {
        void execute(LivingEntity entity, String[] args, AnimationEffectHandler.EffectContext ctx);
    }

    public static void register(String prefix, InstructionParser parser) {
        CUSTOM_PARSERS.put(prefix, parser);
    }

    public static void execute(String instruction, LivingEntity entity, AnimationEffectHandler.EffectContext ctx) {
        if (instruction == null || instruction.isBlank()) return;
        String[] parts = instruction.split(";");
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) continue;
            executeSingle(trimmed, entity, ctx);
        }
    }

    private static void executeSingle(String instruction, LivingEntity entity, AnimationEffectHandler.EffectContext ctx) {
        String[] tokens = instruction.split(":", 2);
        String prefix = tokens[0];
        String[] args = tokens.length > 1 ? tokens[1].split(":") : new String[0];

        switch (prefix) {
            case "particle" -> {
                var handler = new ParticleKeyframeHandler();
                handler.start(entity, new AnimationEffectHandler.EffectContext(ctx.animTime(), ctx.deltaTime(), args));
            }
            case "sound" -> {
                var handler = new SoundKeyframeHandler();
                handler.start(entity, new AnimationEffectHandler.EffectContext(ctx.animTime(), ctx.deltaTime(), args));
            }
            case "camera_shake" -> {
                var handler = new CameraShakeHandler();
                handler.start(entity, new AnimationEffectHandler.EffectContext(ctx.animTime(), ctx.deltaTime(), args));
            }
            case "camera_tilt" -> {
                if (args.length >= 3) {
                    float roll = parseFloat(args[0], 0);
                    float pitch = parseFloat(args[1], 0);
                    float yaw = parseFloat(args[2], 0);
                    bond.thematic.api.engine.camera.CameraTiltState.set(roll, pitch, yaw);
                }
            }
            default -> {
                InstructionParser parser = CUSTOM_PARSERS.get(prefix);
                if (parser != null) {
                    parser.execute(entity, args, ctx);
                }
            }
        }
    }

    private static float parseFloat(String s, float def) {
        try { return Float.parseFloat(s); } catch (NumberFormatException e) { return def; }
    }
}
