package bond.thematic.api.layered;

import bond.thematic.api.TransformType;
import bond.thematic.api.layered.modifier.AbstractFadeModifier;
import bond.thematic.api.layered.modifier.AbstractModifier;
import bond.thematic.api.core.util.Ease;
import bond.thematic.api.core.util.Vec3f;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


/**
 * Layer to easily swap animations, add modifiers or do other sort of effects
 * Modifiers <b>affect</b> each other. For example if you put a fade modifier after a speed modifier, it will be affected by the modifier.
 *
 * @param <T>
 */
public class ModifierLayer<T extends IAnimation> implements IAnimation {

    private final List<AbstractModifier> modifiers = new ArrayList<>();
    @Nullable
    @Getter
    T animation;


    public ModifierLayer(@Nullable T animation, AbstractModifier... modifiers) {
        this.animation = animation;
        Collections.addAll(this.modifiers, modifiers);
    }

    public ModifierLayer() {
        this(null);
    }

    @Override
    public void tick() {
        for (int i = 0; i < modifiers.size(); i++) {
            if (modifiers.get(i).canRemove()) {
                removeModifier(i--);
            }
        }
        if (modifiers.size() > 0) {
            modifiers.get(0).tick();
        } else if (animation != null) animation.tick();
    }

    public void addModifier(@NotNull AbstractModifier modifier, int idx) {
        modifier.setHost(this);
        modifiers.add(idx, modifier);
        this.linkModifiers();
    }

    public void addModifierBefore(@NotNull AbstractModifier modifier) {
        this.addModifier(modifier, 0);
    }

    public void addModifierLast(@NotNull AbstractModifier modifier) {
        this.addModifier(modifier, modifiers.size());
    }

    public void removeModifier(int idx) {
        modifiers.remove(idx);
        this.linkModifiers();
    }


    public void setAnimation(@Nullable T animation) {
        this.animation = animation;
        this.linkModifiers();
    }

    /**
     * Fade out from current animation into new animation.
     * Does not fade if there is currently no active animation
     *
     * @param fadeModifier Fade modifier, use {@link AbstractFadeModifier#standardFadeIn(int, Ease)} for simple fade.
     * @param newAnimation New animation, can be null to fade into default state.
     */
    public void replaceAnimationWithFade(@NotNull AbstractFadeModifier fadeModifier, @Nullable T newAnimation) {
        replaceAnimationWithFade(fadeModifier, newAnimation, false);
    }

    /**
     * Fade out from current to a new animation
     *
     * @param fadeModifier    Fade modifier, use {@link AbstractFadeModifier#standardFadeIn(int, Ease)} for simple fade.
     * @param newAnimation    New animation, can be null to fade into default state.
     * @param fadeFromNothing Do fade even if we go from nothing. (for KeyframeAnimation, it can be false by default)
     */
    public void replaceAnimationWithFade(@NotNull AbstractFadeModifier fadeModifier, @Nullable T newAnimation, boolean fadeFromNothing) {
        if (fadeFromNothing || getAnimation() != null && getAnimation().isActive()) {
            fadeModifier.setBeginAnimation(this.getAnimation());
            addModifierLast(fadeModifier);
        }
        this.setAnimation(newAnimation);
    }

    public int size() {
        return modifiers.size();
    }

    protected void linkModifiers() {
        Iterator<AbstractModifier> modifierIterator = modifiers.iterator();
        if (modifierIterator.hasNext()) {
            AbstractModifier tmp = modifierIterator.next();
            while (modifierIterator.hasNext()) {
                AbstractModifier tmp2 = modifierIterator.next();
                tmp.setAnim(tmp2);
                tmp = tmp2;
            }
            tmp.setAnim(this.animation);
        }
    }


    @Override
    public boolean isActive() {
        if (modifiers.size() > 0) {
            return modifiers.get(0).isActive();
        } else if (animation != null) return animation.isActive();
        return false;
    }

    @Override
    public @NotNull Vec3f get3DTransform(@NotNull String modelName, @NotNull TransformType type, float tickDelta, @NotNull Vec3f value0) {
        if (!modifiers.isEmpty()) {
            return modifiers.get(0).get3DTransform(modelName, type, tickDelta, value0);
        } else if (animation != null) return animation.get3DTransform(modelName, type, tickDelta, value0);
        return value0;
    }

    @Override
    public void setupAnim(float tickDelta) {
        if (!modifiers.isEmpty()) {
            modifiers.get(0).setupAnim(tickDelta);
        } else if (animation != null) animation.setupAnim(tickDelta);
    }

    @Override
    public KeyframeType getKeyframeType() {
        if (!modifiers.isEmpty()) {
            return modifiers.get(0).getKeyframeType();
        } else if (animation != null) return animation.getKeyframeType();
        return KeyframeType.ADDITIVE;
    }

    @Override
    public KeyframeType getKeyframeType(String partName) {
        if (!modifiers.isEmpty()) {
            return modifiers.get(0).getKeyframeType(partName);
        } else if (animation != null) return animation.getKeyframeType(partName);
        return KeyframeType.ADDITIVE;
    }

    @Override
    public boolean isPartAnimated(String partName) {
        if (!modifiers.isEmpty()) {
            return modifiers.get(0).isPartAnimated(partName);
        } else if (animation != null) return animation.isPartAnimated(partName);
        return false;
    }
}