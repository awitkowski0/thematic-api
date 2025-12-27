package bond.thematic.api.forge;

import bond.thematic.api.minecraftApi.PlayerAnimationRegistry;
import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.jetbrains.annotations.NotNull;

@Mod.EventBusSubscriber(modid = "thematic_api", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ForgeClientEvent {

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        // Removed SkinLayers integration since bending support was removed
    }

    @SubscribeEvent
    public static void resourceLoadingListener(@NotNull RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((SynchronousResourceReloader) manager -> PlayerAnimationRegistry
                .resourceLoaderCallback(manager, ForgeModInterface.LOGGER));
    }

}
