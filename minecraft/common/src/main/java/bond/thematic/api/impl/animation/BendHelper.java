package bond.thematic.api.impl.animation;

import bond.thematic.api.core.util.Pair;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * BendHelper implementation - no-op since bending is no longer supported.
 * 
 * Bending functionality has been removed from thematic-api.
 * All bend operations are ignored.
 */
@ApiStatus.Internal
public class BendHelper implements IBendHelper {
    @Override
    public void bend(ModelPart modelPart, float axis, float rotation){
        // No-op: bending is no longer supported in thematic-api
    }

    @Override
    public void bend(ModelPart modelPart, @Nullable Pair<Float, Float> pair){
        // No-op: bending is no longer supported in thematic-api
    }

    @Override
    public void initBend(ModelPart modelPart, Direction direction) {
        // No-op: bending is no longer supported in thematic-api
    }
}
