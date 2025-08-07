package bond.thematic.api.mixin;

import bond.thematic.api.TransformType;
import bond.thematic.api.core.impl.AnimationProcessor;
import bond.thematic.api.core.util.Vec3f;
import bond.thematic.api.IAnimatedPlayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemFeatureRenderer.class)
public class HeldItemMixin {
    @Inject(method = "renderItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"))    private void changeItemLocation(LivingEntity livingEntity, ItemStack itemStack, ModelTransformationMode itemDisplayContext, Arm arm, MatrixStack matrices, VertexConsumerProvider multiBufferSource, int i, CallbackInfo ci) {
        if(livingEntity instanceof IAnimatedPlayer player) {
            if (player.playerAnimator_getAnimation().isActive()) {
                AnimationProcessor anim = player.playerAnimator_getAnimation();

                Vec3f rot = anim.get3DTransform(arm == Arm.LEFT ? "leftItem" : "rightItem", TransformType.ROTATION, Vec3f.ZERO);
                Vec3f pos = anim.get3DTransform(arm == Arm.LEFT ? "leftItem" : "rightItem", TransformType.POSITION, Vec3f.ZERO).scale(1/16f);

                matrices.translate(pos.getX(), pos.getY(), pos.getZ());

                matrices.multiply(RotationAxis.POSITIVE_Z.rotation(rot.getZ()));    //roll
                matrices.multiply(RotationAxis.POSITIVE_Y.rotation(rot.getY()));    //pitch
                matrices.multiply(RotationAxis.POSITIVE_X.rotation(rot.getX()));    //yaw
            }
        }
    }
}