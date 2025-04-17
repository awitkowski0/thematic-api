package com.funalex.themAnim.impl.animation;


import com.funalex.themAnim.api.TransformType;
import com.funalex.themAnim.api.layered.IAnimation;
import com.funalex.themAnim.core.impl.AnimationProcessor;
import com.funalex.themAnim.core.util.MathHelper;
import com.funalex.themAnim.core.util.Pair;
import com.funalex.themAnim.core.util.Vec3f;
import net.minecraft.client.model.geom.ModelPart;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class AnimationApplier extends AnimationProcessor {
    public AnimationApplier(IAnimation animation) {
        super(animation);
    }

    public void updatePart(String partName, ModelPart part) {
        Vec3f pos = this.get3DTransform(partName, TransformType.POSITION, new Vec3f(part.x, part.y, part.z));
        part.x = pos.getX();
        part.y = pos.getY();
        part.z = pos.getZ();
        Vec3f rot = this.get3DTransform(partName, TransformType.ROTATION, new Vec3f( // clamp guards
                MathHelper.clampToRadian(part.xRot),
                MathHelper.clampToRadian(part.yRot),
                MathHelper.clampToRadian(part.zRot)));
        part.setRotation(rot.getX(), rot.getY(), rot.getZ());
        if (!partName.equals("head")) {
            if (partName.equals("torso")) {
                Pair<Float, Float> torsoBend = getBend(partName);
                Pair<Float, Float> bodyBend = getBend("body");
                IBendHelper.INSTANCE.bend(part, new Pair<>(torsoBend.getLeft() + bodyBend.getLeft(), torsoBend.getRight() + bodyBend.getRight()));
            } else {
                IBendHelper.INSTANCE.bend(part, getBend(partName));
            }
        }
    }

}
