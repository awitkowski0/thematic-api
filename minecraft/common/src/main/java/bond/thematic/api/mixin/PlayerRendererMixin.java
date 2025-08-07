package bond.thematic.api.mixin;

import bond.thematic.api.TransformType;
import bond.thematic.api.core.util.Vec3f;
import bond.thematic.api.IAnimatedPlayer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerRendererMixin extends LivingEntityRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {
    public PlayerRendererMixin(EntityRendererFactory.Context context, PlayerEntityModel<AbstractClientPlayerEntity> entityModel, float f) {
        super(context, entityModel, f);
    }

    @Inject(method = "setupTransforms(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/client/util/math/MatrixStack;FFF)V", at = @At("RETURN"))
    private void applyBodyTransforms(AbstractClientPlayerEntity abstractClientPlayerEntity, MatrixStack matrixStack, float f, float bodyYaw, float tickDelta, CallbackInfo ci){
        var animationPlayer = ((IAnimatedPlayer) abstractClientPlayerEntity).playerAnimator_getAnimation();
        animationPlayer.setTickDelta(tickDelta);

        if(animationPlayer.isActive()){
            //These are additive properties
            Vec3f vec3d = animationPlayer.get3DTransform("body", TransformType.POSITION, Vec3f.ZERO);
            matrixStack.translate(vec3d.getX(), vec3d.getY() + 0.7, vec3d.getZ());
            Vec3f vec3f = animationPlayer.get3DTransform("body", TransformType.ROTATION, Vec3f.ZERO);
            matrixStack.multiply(RotationAxis.POSITIVE_Z.rotation(vec3f.getZ()));    //roll
            matrixStack.multiply(RotationAxis.POSITIVE_Y.rotation(vec3f.getY()));    //pitch
            matrixStack.multiply(RotationAxis.POSITIVE_X.rotation(vec3f.getX()));    //yaw
            matrixStack.translate(0, - 0.7d, 0);
        }
    }
}