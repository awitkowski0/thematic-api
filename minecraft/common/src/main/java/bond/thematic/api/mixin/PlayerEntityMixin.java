package bond.thematic.api.mixin;

import bond.thematic.api.layered.AnimationStack;
import bond.thematic.api.layered.IAnimation;
import bond.thematic.api.IAnimatedPlayer;
import bond.thematic.api.animation.AnimationApplier;
import bond.thematic.api.minecraftApi.PlayerAnimationAccess;
import bond.thematic.api.minecraftApi.PlayerAnimationFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin implements IAnimatedPlayer {

    //Unique params might be renamed
    @Unique
    private final Map<Identifier, IAnimation> modAnimationData = new HashMap<>();
    @Unique
    private final AnimationStack animationStack = createAnimationStack();
    @Unique
    private final AnimationApplier animationApplier = new AnimationApplier(animationStack);

    @SuppressWarnings("ConstantConditions")
    @Unique
    private AnimationStack createAnimationStack() {
        AnimationStack stack = new AnimationStack();
        if (AbstractClientPlayerEntity.class.isInstance(this)) {
            PlayerAnimationFactory.ANIMATION_DATA_FACTORY.prepareAnimations((AbstractClientPlayerEntity)(Object) this, stack, modAnimationData);
            PlayerAnimationAccess.REGISTER_ANIMATION_EVENT.invoker().registerAnimation((AbstractClientPlayerEntity)(Object) this, stack);
        }
        return stack;
    }

    @Override
    public AnimationStack getAnimationStack() {
        return animationStack;
    }

    @Override
    public AnimationApplier playerAnimator_getAnimation() {
        return animationApplier;
    }

    @Override
    public @Nullable IAnimation playerAnimator_getAnimation(@NotNull Identifier id) {
        return modAnimationData.get(id);
    }

    @Override
    public @Nullable IAnimation playerAnimator_setAnimation(@NotNull Identifier id, @Nullable IAnimation animation) {
        if (animation == null) {
            return modAnimationData.remove(id);
        } else {
            return modAnimationData.put(id, animation);
        }
    }

    @SuppressWarnings("ConstantConditions") // When injected into PlayerEntity, instance check can tell if a ClientPlayer or ServerPlayer
    @Inject(method = "tick", at = @At("HEAD"))
    private void tick(CallbackInfo ci) {
        if (AbstractClientPlayerEntity.class.isInstance(this)) {
            animationStack.tick();
        }
    }
}
