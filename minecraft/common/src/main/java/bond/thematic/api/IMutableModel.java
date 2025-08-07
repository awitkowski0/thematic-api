package bond.thematic.api;

import bond.thematic.api.core.impl.AnimationProcessor;
import bond.thematic.api.core.util.SetableSupplier;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface IMutableModel {

    void setEmoteSupplier(SetableSupplier<AnimationProcessor> emoteSupplier);

    SetableSupplier<AnimationProcessor> getEmoteSupplier();

}
