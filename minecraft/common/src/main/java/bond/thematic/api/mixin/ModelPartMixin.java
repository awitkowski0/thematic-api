package bond.thematic.api.mixin;

import bond.thematic.api.IUpperPartHelper;
import net.minecraft.client.model.ModelPart;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ModelPart.class)
public abstract class ModelPartMixin implements IUpperPartHelper {
    private boolean Emotecraft_upper = false;

    @Override
    public boolean isUpperPart() {
        return Emotecraft_upper;
    }

    @Override
    public void setUpperPart(boolean bl) {
        Emotecraft_upper = bl;
    }
}
