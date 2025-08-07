package bond.thematic.api.mixin;

import bond.thematic.api.core.impl.AnimationProcessor;
import bond.thematic.api.core.util.SetableSupplier;
import bond.thematic.api.IAnimatedPlayer;
import bond.thematic.api.IMutableModel;
import bond.thematic.api.IPlayerModel;
import bond.thematic.api.animation.AnimationApplier;
import bond.thematic.api.animation.IBendHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

@Mixin(value = PlayerEntityModel.class, priority = 2000)//Apply after NotEnoughAnimation's inject
public class PlayerModelMixin<T extends LivingEntity> extends BipedEntityModel<T> implements IPlayerModel {
    @Shadow
    @Final
    public ModelPart jacket;
    @Shadow
    @Final
    public ModelPart rightSleeve;
    @Shadow
    @Final
    public ModelPart leftSleeve;
    @Shadow @Final public ModelPart rightPants;
    @Shadow @Final public ModelPart leftPants;
    @Unique
    private final SetableSupplier<AnimationProcessor> emoteSupplier = new SetableSupplier<>();

    @Unique
    private boolean firstPersonNext = false;

    public PlayerModelMixin(ModelPart modelPart, Function<Identifier, RenderLayer> function) {
        super(modelPart, function);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void initBendableStuff(ModelPart modelPart, boolean bl, CallbackInfo ci){
        IMutableModel thisWithMixin = (IMutableModel) this;
        emoteSupplier.set(null);

        thisWithMixin.setEmoteSupplier(emoteSupplier);

        addBendMutator(this.jacket, Direction.DOWN);
        addBendMutator(this.rightPants, Direction.UP);
        addBendMutator(this.rightSleeve, Direction.UP);
        addBendMutator(this.leftPants, Direction.UP);
        addBendMutator(this.leftSleeve, Direction.UP);
// TODO: readd
//        ((IUpperPartHelper)rightSleeve).setUpperPart(true);
//        ((IUpperPartHelper)leftSleeve).setUpperPart(true);

    }

    @Unique
    private void addBendMutator(ModelPart part, Direction d){
        IBendHelper.INSTANCE.initBend(part, d);
    }

    @Unique
    private void setDefaultPivot(){
        this.leftLeg.setPivot(1.9F, 12.0F, 0.0F);
        this.rightLeg.setPivot(- 1.9F, 12.0F, 0.0F);
        this.head.setPivot(0.0F, 0.0F, 0.0F);
        this.rightArm.pivotZ = 0.0F;
        this.rightArm.pivotX = - 5.0F;
        this.leftArm.pivotZ = 0.0F;
        this.leftArm.pivotX = 5.0F;
        this.body.pitch = 0.0F;
        this.rightLeg.pivotZ = 0.1F;
        this.leftLeg.pivotZ = 0.1F;
        this.rightLeg.pivotY = 12.0F;
        this.leftLeg.pivotY = 12.0F;
        this.head.pivotY = 0.0F;
        this.head.roll = 0f;
        this.body.pivotY = 0.0F;
        this.body.pivotX = 0f;
        this.body.pivotZ = 0f;
        this.body.yaw = 0;
        this.body.roll = 0;
    }

    @Inject(method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at = @At(value = "HEAD"))
    private void setDefaultBeforeRender(T livingEntity, float f, float g, float h, float i, float j, CallbackInfo ci){
        setDefaultPivot(); //to not make everything wrong
    }

    @Inject(method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ModelPart;copyTransform(Lnet/minecraft/client/model/ModelPart;)V", ordinal = 0))
    private void setEmote(T livingEntity, float f, float g, float h, float i, float j, CallbackInfo ci){
        if(!firstPersonNext && livingEntity instanceof AbstractClientPlayerEntity && ((IAnimatedPlayer)livingEntity).playerAnimator_getAnimation().isActive()){
            AnimationApplier emote = ((IAnimatedPlayer) livingEntity).playerAnimator_getAnimation();
            emoteSupplier.set(emote);

            emote.updatePart("head", this.head);
            this.hat.copyTransform(this.head);

            emote.updatePart("leftArm", this.leftArm);
            emote.updatePart("rightArm", this.rightArm);
            emote.updatePart("leftLeg", this.leftLeg);
            emote.updatePart("rightLeg", this.rightLeg);
            emote.updatePart("torso", this.body);


        }
        else {
            firstPersonNext = false;
            emoteSupplier.set(null);
            resetBend(this.body);
            resetBend(this.leftArm);
            resetBend(this.rightArm);
            resetBend(this.leftLeg);
            resetBend(this.rightLeg);
        }
    }

    @Unique
    private static void resetBend(ModelPart part) {
        IBendHelper.INSTANCE.bend(part, null);
    }

    /**
     * @author KosmX - Player Animator library
     */
    @Override
    public void playerAnimator_prepForFirstPersonRender() {
        firstPersonNext = true;
    }
}