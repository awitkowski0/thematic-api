package bond.thematic.api.mixin;

import bond.thematic.api.IUpperPartHelper;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FeatureRenderer.class)
public class FeatureRendererMixin implements IUpperPartHelper {
    @Unique
    private boolean isUpperPart = true;


    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(FeatureRendererContext context, CallbackInfo ci) {
        if (this.getClass().getPackageName().contains("skinlayers") && !this.getClass().getSimpleName().toLowerCase().contains("head")) {
            isUpperPart = false;
        }
    }

    @Override
    public boolean isUpperPart() {
        return this.isUpperPart;
    }

    @Override
    public void setUpperPart(boolean bl) {
        this.isUpperPart = bl;
    }
}
