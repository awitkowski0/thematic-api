package bond.thematic.api.mixin;

import bond.thematic.api.core.impl.AnimationProcessor;
import bond.thematic.api.core.util.SetableSupplier;
import bond.thematic.api.IMutableModel;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.AnimalModel;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

@Mixin(BipedEntityModel.class)
public abstract class BipedEntityModelMixin<T extends LivingEntity> extends AnimalModel<T> implements IMutableModel {
    @Final
    @Shadow
    public ModelPart rightArm;
    @Final
    @Shadow
    public ModelPart leftArm;
    @Unique
    private SetableSupplier<AnimationProcessor> animation = new SetableSupplier<>();

    @Inject(method = "<init>(Lnet/minecraft/client/model/ModelPart;Ljava/util/function/Function;)V", at = @At("RETURN"))
    private void initUpperParts(ModelPart modelPart, Function<Identifier, RenderLayer> function, CallbackInfo ci){
        // Removed bend initialization since bending is no longer supported
        //TODO: readd
//        ((IUpperPartHelper)rightArm).setUpperPart(true);
//        ((IUpperPartHelper)leftArm).setUpperPart(true);
//        ((IUpperPartHelper)head).setUpperPart(true);
//        ((IUpperPartHelper)hat).setUpperPart(true);
    }

    @Override
    public void setEmoteSupplier(SetableSupplier<AnimationProcessor> emoteSupplier){
        this.animation = emoteSupplier;
    }

    @Inject(method = "copyBipedStateTo", at = @At("RETURN"))
    private void copyMutatedAttributes(BipedEntityModel<T> bipedEntityModel, CallbackInfo ci){
        if(animation != null) {
            ((IMutableModel) bipedEntityModel).setEmoteSupplier(animation);
        }
    }

    @Intrinsic(displace = true)
    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha){
        // Removed bend-specific rendering logic since bending is no longer supported
        // Now using standard rendering for all cases
        super.render(matrices, vertices, light, overlay, red, green, blue, alpha);
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
