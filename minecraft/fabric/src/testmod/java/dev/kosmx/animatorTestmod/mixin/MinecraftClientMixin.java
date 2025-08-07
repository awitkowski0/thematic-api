package dev.kosmx.animatorTestmod.mixin;

import bond.thematic.api.minecraftApi.PlayerAnimationAccess;
import bond.thematic.api.minecraftApi.PlayerAnimationRegistry;
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

        bond.thematic.api.layered.AnimationStack animationStack = PlayerAnimationAccess.getPlayerAnimLayer(Minecraft.getInstance().player);
        bond.thematic.api.layered.KeyframeAnimationPlayer animPlayer = new bond.thematic.api.layered.KeyframeAnimationPlayer(PlayerAnimationRegistry.getAnimation(new ResourceLocation("testmod", "thunderclap")));
        animationStack.addAnimLayer(0, animPlayer);
        //PlayerAnimTestmod.playTestAnimation();
        cir.setReturnValue(true);
    }
}
