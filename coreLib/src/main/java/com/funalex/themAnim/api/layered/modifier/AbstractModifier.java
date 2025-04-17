package com.funalex.themAnim.api.layered.modifier;

import com.funalex.themAnim.api.layered.AnimationContainer;
import com.funalex.themAnim.api.layered.IAnimation;
import com.funalex.themAnim.api.layered.ModifierLayer;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("rawtypes")
public abstract class AbstractModifier extends AnimationContainer<IAnimation> {

    /**
     * ModifierLayer, if you want to do something really fancy!
     * Shouldn't be null when playing
     */
    @Nullable
    @Setter
    protected ModifierLayer host;

    public AbstractModifier() {
        super(null);
    }

    /**
     * @return modifier can be removed.
     */
    public boolean canRemove() {
        return false;
    }
}