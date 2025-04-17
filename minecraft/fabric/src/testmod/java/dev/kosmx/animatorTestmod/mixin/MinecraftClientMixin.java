package dev.kosmx.animatorTestmod.mixin;

import com.funalex.themAnim.api.layered.AnimationStack;
import com.funalex.themAnim.api.layered.KeyframeAnimationPlayer;
import com.funalex.themAnim.minecraftApi.PlayerAnimationAccess;
import com.funalex.themAnim.minecraftApi.PlayerAnimationRegistry;
import dev.kosmx.animatorTestmod.PlayerAnimTestmod;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MinecraftClientMixin {

    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void ATTACK(CallbackInfoReturnable<Boolean> cir) {
        System.out.println("MinecraftClientMixin - startAttack");



        AnimationStack animationStack = PlayerAnimationAccess.getPlayerAnimLayer(Minecraft.getInstance().player);
        KeyframeAnimationPlayer animPlayer = new KeyframeAnimationPlayer(PlayerAnimationRegistry.getAnimation(new ResourceLocation("testmod", "thunderclap")));
        animationStack.addAnimLayer(0, animPlayer);
        //PlayerAnimTestmod.playTestAnimation();
        cir.setReturnValue(true);
    }
}
