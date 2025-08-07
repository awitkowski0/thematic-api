package bond.thematic.api.animation;


import bond.thematic.api.TransformType;
import bond.thematic.api.layered.IAnimation;
import net.minecraft.client.model.ModelPart;
import bond.thematic.api.core.impl.AnimationProcessor;
import bond.thematic.api.core.util.MathHelper;
import bond.thematic.api.core.util.Pair;
import bond.thematic.api.core.util.Vec3f;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class AnimationApplier extends AnimationProcessor {
    public AnimationApplier(IAnimation animation) {
        super(animation);
    }

    public void updatePart(String partName, ModelPart part) {
        Vec3f pos = this.get3DTransform(partName, TransformType.POSITION, new Vec3f(part.pivotX, part.pivotY, part.pivotZ));
        part.pivotX = pos.getX();
        part.pivotY = pos.getY();
        part.pivotZ = pos.getZ();
        Vec3f rot = this.get3DTransform(partName, TransformType.ROTATION, new Vec3f( // clamp guards
                MathHelper.clampToRadian(part.pitch),
                MathHelper.clampToRadian(part.yaw),
                MathHelper.clampToRadian(part.roll)));
        part.setAngles(rot.getX(), rot.getY(), rot.getZ());
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
