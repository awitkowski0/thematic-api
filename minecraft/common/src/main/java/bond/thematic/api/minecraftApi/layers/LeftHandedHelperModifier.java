package bond.thematic.api.minecraftApi.layers;

import bond.thematic.api.layered.modifier.MirrorModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Arm;

/**
 * Left-handedness helper
 * If enabled, automatically mirror all animation if player is left-handed
 */
public class LeftHandedHelperModifier extends MirrorModifier {
    private final PlayerEntity player;

    public LeftHandedHelperModifier(PlayerEntity player) {
        this.player = player;
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled() && player.getMainArm() == Arm.LEFT;
    }
}
