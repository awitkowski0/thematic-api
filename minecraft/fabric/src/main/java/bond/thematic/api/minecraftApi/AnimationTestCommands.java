package bond.thematic.api.minecraftApi;

import bond.thematic.api.layered.IAnimation;
import bond.thematic.api.layered.ModifierLayer;
import bond.thematic.api.core.data.KeyframeAnimation;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Debug/test command for playing any loaded animation.
 * Intended for development usage.
 */
public final class AnimationTestCommands {
    private static final Identifier TEST_LAYER_ID = new Identifier("thematic-api", "debug_playanim_layer");
    private static final int TEST_LAYER_PRIORITY = 9000;

    private static boolean registered = false;
    private static String preferredNamespace = null;

    private AnimationTestCommands() {
    }

    /**
     * Register debug commands only in development environments.
     * Useful for mods that want easy animation testing while developing.
     *
     * @param defaultNamespace namespace used for short-name resolution, or null
     */
    public static void registerDevOnly(String defaultNamespace) {
        register(defaultNamespace, true);
    }

    /**
     * Register debug commands for animation playback.
     *
     * @param defaultNamespace namespace used for short-name resolution, or null
     * @param devOnly          if true, command is registered only in dev environment
     */
    public static void register(String defaultNamespace, boolean devOnly) {
        if (devOnly && !FabricLoader.getInstance().isDevelopmentEnvironment()) {
            return;
        }
        if (registered) {
            return;
        }

        preferredNamespace = (defaultNamespace == null || defaultNamespace.isBlank()) ? null : defaultNamespace;
        registered = true;

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommandManager.literal("playanim")
                        .then(ClientCommandManager.literal("list").executes(context -> {
                            List<String> keys = PlayerAnimationRegistry.getAnimations().keySet().stream()
                                    .map(Identifier::toString)
                                    .sorted()
                                    .toList();

                            context.getSource().sendFeedback(Text.literal("Loaded animations: " + keys.size()));
                            if (keys.isEmpty()) {
                                return 1;
                            }

                            int maxLines = Math.min(40, keys.size());
                            for (int i = 0; i < maxLines; i++) {
                                context.getSource().sendFeedback(Text.literal(" - " + keys.get(i)));
                            }
                            if (keys.size() > maxLines) {
                                context.getSource().sendFeedback(Text.literal("...and " + (keys.size() - maxLines) + " more"));
                            }
                            return 1;
                        }))
                        .then(ClientCommandManager.literal("stop").executes(context -> {
                            AbstractClientPlayerEntity player = context.getSource().getPlayer();
                            IAnimation existing = PlayerAnimationAccess.getPlayerAssociatedData(player).get(TEST_LAYER_ID);
                            if (existing instanceof ModifierLayer<?> modifierLayer) {
                                @SuppressWarnings("unchecked")
                                ModifierLayer<IAnimation> layer = (ModifierLayer<IAnimation>) modifierLayer;
                                layer.setAnimation(null);
                                context.getSource().sendFeedback(Text.literal("Stopped test animation."));
                                return 1;
                            }

                            context.getSource().sendError(Text.literal("No test animation is active."));
                            return 0;
                        }))
                        .then(ClientCommandManager.argument("animation", StringArgumentType.string())
                                .suggests(AnimationTestCommands::suggestAnimations)
                                .executes(context -> {
                                    String raw = StringArgumentType.getString(context, "animation").trim();
                                    Identifier animationId = resolveAnimationId(raw);
                                    if (animationId == null) {
                                        context.getSource().sendError(Text.literal("Animation not found/ambiguous. Use namespace:path."));
                                        return 0;
                                    }

                                    KeyframeAnimation animation = PlayerAnimationRegistry.getAnimation(animationId);
                                    if (animation == null) {
                                        context.getSource().sendError(Text.literal("Animation not found: " + animationId));
                                        return 0;
                                    }

                                    AbstractClientPlayerEntity player = context.getSource().getPlayer();
                                    ModifierLayer<IAnimation> layer = getOrCreateLayer(player);
                                    layer.setAnimation(animation.playAnimation());

                                    context.getSource().sendFeedback(Text.literal("Playing animation: " + animationId));
                                    return 1;
                                })))
        );
    }

    private static ModifierLayer<IAnimation> getOrCreateLayer(AbstractClientPlayerEntity player) {
        IAnimation existing = PlayerAnimationAccess.getPlayerAssociatedData(player).get(TEST_LAYER_ID);
        if (existing instanceof ModifierLayer<?> modifierLayer) {
            @SuppressWarnings("unchecked")
            ModifierLayer<IAnimation> layer = (ModifierLayer<IAnimation>) modifierLayer;
            return layer;
        }

        ModifierLayer<IAnimation> layer = new ModifierLayer<>();
        PlayerAnimationAccess.getPlayerAnimLayer(player).addAnimLayer(TEST_LAYER_PRIORITY, layer);
        PlayerAnimationAccess.getPlayerAssociatedData(player).set(TEST_LAYER_ID, layer);
        return layer;
    }

    private static Identifier resolveAnimationId(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        if (raw.contains(":")) {
            try {
                Identifier id = parseIdentifier(raw);
                return PlayerAnimationRegistry.getAnimation(id) != null ? id : null;
            } catch (Exception ignored) {
                return null;
            }
        }

        if (preferredNamespace != null) {
            Identifier preferred = new Identifier(preferredNamespace, raw);
            if (PlayerAnimationRegistry.getAnimation(preferred) != null) {
                return preferred;
            }
        }

        List<Identifier> matches = new ArrayList<>();
        for (Identifier id : PlayerAnimationRegistry.getAnimations().keySet()) {
            if (id.getPath().equals(raw)) {
                matches.add(id);
            }
        }
        if (matches.size() == 1) {
            return matches.get(0);
        }
        return null;
    }

    private static CompletableFuture<Suggestions> suggestAnimations(CommandContext<?> context, SuggestionsBuilder builder) {
        String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);

        for (Identifier id : PlayerAnimationRegistry.getAnimations().keySet()) {
            String full = id.toString();
            if (full.toLowerCase(Locale.ROOT).startsWith(remaining)) {
                builder.suggest(full);
            }
            if (preferredNamespace == null || id.getNamespace().equals(preferredNamespace)) {
                String shortName = id.getPath();
                if (shortName.toLowerCase(Locale.ROOT).startsWith(remaining)) {
                    builder.suggest(shortName);
                }
            }
        }
        return builder.buildFuture();
    }

    private static Identifier parseIdentifier(String raw) {
        String[] split = raw.split(":", 2);
        return new Identifier(split[0], split[1]);
    }
}
