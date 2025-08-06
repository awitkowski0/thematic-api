package com.funalex.themAnim.api.layered.modifier;

import com.funalex.themAnim.api.TransformType;
import com.funalex.themAnim.api.firstPerson.FirstPersonConfiguration;
import com.funalex.themAnim.core.util.Vec3f;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
public class MirrorModifier extends AbstractModifier {

    public static final Map<String, String> mirrorMap;

    /**
     * Enable the modifier
     */
    @Getter
    @Setter
    private boolean enabled = true;

    @Override
    public @NotNull Vec3f get3DTransform(@NotNull String modelName, @NotNull TransformType type, float tickDelta, @NotNull Vec3f value0) {
        if (!isEnabled()) return super.get3DTransform(modelName, type, tickDelta, value0);

        if (mirrorMap.containsKey(modelName)) modelName = mirrorMap.get(modelName);
        value0 = transformVector(value0, type);

        Vec3f vec3f = super.get3DTransform(modelName, type, tickDelta, value0);
        return transformVector(vec3f, type);
    }

    protected Vec3f transformVector(Vec3f value0, TransformType type) {
        switch (type) {
            case POSITION:
                return new Vec3f(-value0.getX(), value0.getY(), value0.getZ());
            case ROTATION:
                return new Vec3f(value0.getX(), -value0.getY(), -value0.getZ());
            case BEND:
                return new Vec3f(value0.getX(), -value0.getY(), value0.getZ());
            default:
                return value0; //why?!
        }
    }

    static {
        HashMap<String, String> partMap = new HashMap<>();
        partMap.put("leftArm", "rightArm");
        partMap.put("leftLeg", "rightLeg");
        partMap.put("leftItem", "rightItem");
        partMap.put("rightArm", "leftArm");
        partMap.put("rightLeg", "leftLeg");
        partMap.put("rightItem", "leftItem");
        mirrorMap = Collections.unmodifiableMap(partMap);
    }
}