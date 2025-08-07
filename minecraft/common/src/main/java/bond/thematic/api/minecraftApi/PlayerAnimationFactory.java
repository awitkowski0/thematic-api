package bond.thematic.api.minecraftApi;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.util.Identifier;

/**
 * Animation factory, the factory will be invoked whenever a client-player is constructed.
 * The returned animation will be automatically registered and added to playerAssociated data.
 * <p>
 * {@link PlayerAnimationAccess#REGISTER_ANIMATION_EVENT} is invoked <strong>after</strong> factories are done.
 */
public interface PlayerAnimationFactory {

    FactoryHolder ANIMATION_DATA_FACTORY = new FactoryHolder();

    @Nullable bond.thematic.api.layered.IAnimation invoke(@NotNull AbstractClientPlayerEntity player);

    class FactoryHolder {
        private FactoryHolder() {}

        private static final List<Function<AbstractClientPlayerEntity, DataHolder>> factories = new ArrayList<>();

        /**
         * Animation factory
         * @param id       animation id or <code>null</code> if you don't want to add to playerAssociated data
         * @param priority animation priority
         * @param factory  animation factory
         */
        public void registerFactory(@Nullable Identifier id, int priority, @NotNull PlayerAnimationFactory factory) {
            factories.add(player -> Optional.ofNullable(factory.invoke(player)).map(animation -> new DataHolder(id, priority, animation)).orElse(null));
        }

        @ApiStatus.Internal
        private record DataHolder(@Nullable Identifier id, int priority, @NotNull bond.thematic.api.layered.IAnimation animation) {}

        @ApiStatus.Internal
        public void prepareAnimations(AbstractClientPlayerEntity player, bond.thematic.api.layered.AnimationStack playerStack, Map<Identifier, bond.thematic.api.layered.IAnimation> animationMap) {
            for (Function<AbstractClientPlayerEntity, DataHolder> factory: factories) {
                DataHolder dataHolder = factory.apply(player);
                if (dataHolder != null) {
                    playerStack.addAnimLayer(dataHolder.priority(), dataHolder.animation());
                    if (dataHolder.id() != null) {
                        animationMap.put(dataHolder.id(), dataHolder.animation());
                    }
                }
            }
        }
    }
}
