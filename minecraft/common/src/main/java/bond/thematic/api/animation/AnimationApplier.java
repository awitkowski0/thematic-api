package bond.thematic.api.animation;


import bond.thematic.api.TransformType;
import bond.thematic.api.layered.IAnimation;
import net.minecraft.client.model.ModelPart;
import bond.thematic.api.core.impl.AnimationProcessor;
import bond.thematic.api.core.util.MathHelper;
import bond.thematic.api.core.util.Pair;
import bond.thematic.api.core.util.Vec3f;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class AnimationApplier extends AnimationProcessor {
    public AnimationApplier(IAnimation animation) {
        super(animation);
    }

    public void updatePart(String partName, ModelPart part, AbstractClientPlayerEntity clientPlayerEntity) {
        String effectivePartName = partName;

        // Arm/head/body fallbacks
        if (partName.equals("leftArm") && !this.isPartAnimated("leftArm")) {
            effectivePartName = "armorLeftArm";
        }
        if (partName.equals("rightArm") && !this.isPartAnimated("rightArm")) {
            effectivePartName = "armorRightArm";
        }
        if (partName.equals("head") && !this.isPartAnimated("head")) {
            effectivePartName = "armorHead";
        }
        if (partName.equals("torso") && !this.isPartAnimated("torso")) {
            effectivePartName = "armorBody";
        }

        Vec3f standingPivot = getStandingPivot(effectivePartName);
        Vec3f pos = this.get3DTransform(effectivePartName, TransformType.POSITION, standingPivot);
        part.pivotX = pos.getX();
        part.pivotY = pos.getY();
        part.pivotZ = pos.getZ();

        if (clientPlayerEntity.isSneaky() && clientPlayerEntity.isOnGround()) {
            if (partName.equals("torso")) {
                part.pivotY += 3.2f;
            } else if (partName.equals("head")) {
                part.pivotY += 4.2f;
            } else if (partName.equals("leftArm") || partName.equals("rightArm")) {
                part.pivotY += 3.2f;
            } else if (partName.equals("leftLeg") || partName.equals("rightLeg")) {
                part.pivotY += 0.2f;
                part.pivotZ += 4.0f;
            }
        }
        Vec3f rot = this.get3DTransform(effectivePartName, TransformType.ROTATION, Vec3f.ZERO);

        // GeckoLibSerializer negates X/Y for body/torso/head; ModelPart uses original convention
        if (effectivePartName.equals("armorBody") || effectivePartName.equals("torso") || effectivePartName.equals("body") ||
            effectivePartName.equals("armorHead") || effectivePartName.equals("head")) {
            rot = new Vec3f(-rot.getX(), -rot.getY(), rot.getZ());
        }

        if (this.getKeyframeType(effectivePartName) == IAnimation.KeyframeType.STATIC) {
            part.pitch = MathHelper.clampToRadian(rot.getX());
            part.yaw = MathHelper.clampToRadian(rot.getY());
            part.roll = MathHelper.clampToRadian(rot.getZ());
        } else {
            part.pitch += MathHelper.clampToRadian(rot.getX());
            part.yaw += MathHelper.clampToRadian(rot.getY());
            part.roll += MathHelper.clampToRadian(rot.getZ());
        }

        if (partName.equals("torso")) {
            Pair<Float, Float> torsoBend = getBend(partName);
            Pair<Float, Float> bodyBend = getBend("body");
            IBendHelper.INSTANCE.bend(part, new Pair<>(torsoBend.getLeft() + bodyBend.getLeft(), torsoBend.getRight() + bodyBend.getRight()));
        } else {
            IBendHelper.INSTANCE.bend(part, getBend(partName));
        }
    }

    private Vec3f getStandingPivot(String partName) {
        switch (partName) {
            case "rightArm":
                return new Vec3f(-5.0f, 2.0f, 0.0f);
            case "leftArm":
                return new Vec3f(5.0f, 2.0f, 0.0f);
            case "rightLeg":
                return new Vec3f(-1.9f, 12.0f, 0.1f);
            case "leftLeg":
                return new Vec3f(1.9f, 12.0f, 0.1f);
            default:
                return Vec3f.ZERO;
        }
    }

}
