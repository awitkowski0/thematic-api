package bond.thematic.api.fabric.client;

import bond.thematic.api.minecraftApi.PlayerAnimationRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApiStatus.Internal
public class FabricClientInitializer implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("thematic-api");

    @Override
    public void onInitializeClient() {
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return new Identifier("thematic-api", "animation");
            }

            @Override
            public void reload(@NotNull ResourceManager manager) {
                PlayerAnimationRegistry.resourceLoaderCallback(manager, LOGGER);
            }
        });
    }

}
