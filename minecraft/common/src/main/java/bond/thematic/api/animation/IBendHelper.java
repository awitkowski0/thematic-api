package bond.thematic.api.animation;

import bond.thematic.api.core.util.Pair;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Bend helper interface for model part bending functionality.
 * 
 * Note: Bending functionality is no longer supported in thematic-api.
 * All methods are no-op implementations.
 */
@ApiStatus.Internal
public interface IBendHelper {

    IBendHelper INSTANCE = new BendHelper();
    
    static void rotateMatrixStack(MatrixStack matrices, Pair<Float, Float> pair){
        float offset = 0.375f;
        matrices.translate(0, offset, 0);
        float bend = pair.getRight();
        float axisf = - pair.getLeft();
        Vector3f axis = new Vector3f((float) Math.cos(axisf), 0, (float) Math.sin(axisf));
        matrices.multiply(new Quaternionf().rotateAxis(bend, axis));
        matrices.translate(0, - offset, 0);
    }

    void bend(ModelPart modelPart, float a, float b);

    void bend(ModelPart modelPart, @Nullable Pair<Float, Float> pair);

    void initBend(ModelPart modelPart, Direction direction);
}
