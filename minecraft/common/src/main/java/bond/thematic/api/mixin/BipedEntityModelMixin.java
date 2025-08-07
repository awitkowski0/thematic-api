package bond.thematic.api.mixin;

import bond.thematic.api.core.impl.AnimationProcessor;
import bond.thematic.api.core.util.SetableSupplier;
import bond.thematic.api.impl.IMutableModel;
import bond.thematic.api.impl.IUpperPartHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

@Mixin(HumanoidModel.class)
public abstract class BipedEntityModelMixin<T extends LivingEntity> extends AgeableListModel<T> implements IMutableModel {
    @Final
    @Shadow
    public ModelPart rightArm;
    @Final
    @Shadow
    public ModelPart leftArm;
    @Unique
    private SetableSupplier<AnimationProcessor> animation = new SetableSupplier<>();

    @Inject(method = "<init>(Lnet/minecraft/client/model/geom/ModelPart;Ljava/util/function/Function;)V", at = @At("RETURN"))
    private void initUpperParts(ModelPart modelPart, Function<ResourceLocation, RenderType> function, CallbackInfo ci){
        // Removed bend initialization since bending is no longer supported
        ((IUpperPartHelper)rightArm).setUpperPart(true);
        ((IUpperPartHelper)leftArm).setUpperPart(true);
        ((IUpperPartHelper)head).setUpperPart(true);
        ((IUpperPartHelper)hat).setUpperPart(true);
    }

    @Override
    public void setEmoteSupplier(SetableSupplier<AnimationProcessor> emoteSupplier){
        this.animation = emoteSupplier;
    }

    @Inject(method = "copyPropertiesTo", at = @At("RETURN"))
    private void copyMutatedAttributes(HumanoidModel<T> bipedEntityModel, CallbackInfo ci){
        if(animation != null) {
            ((IMutableModel) bipedEntityModel).setEmoteSupplier(animation);
        }
    }

    @Intrinsic(displace = true)
    @Override
    public void renderToBuffer(PoseStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha){
        // Removed bend-specific rendering logic since bending is no longer supported
        // Now using standard rendering for all cases
        super.renderToBuffer(matrices, vertices, light, overlay, red, green, blue, alpha);
    }

    @Final
    @Shadow public ModelPart body;

    @Shadow @Final public ModelPart head;

    @Shadow @Final public ModelPart hat;

    @Override
    public SetableSupplier<AnimationProcessor> getEmoteSupplier(){
        return animation;
    }
}
