package bond.thematic.api.layered.modifier;

import bond.thematic.api.layered.AnimationContainer;
import bond.thematic.api.layered.IAnimation;
import bond.thematic.api.layered.ModifierLayer;
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