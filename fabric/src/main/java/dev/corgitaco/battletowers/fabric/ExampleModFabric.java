package dev.corgitaco.battletowers.fabric;

import dev.corgitaco.battletowers.ExampleMod;
import dev.corgitaco.battletowers.world.entity.CBTEntityTypes;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;

/**
 * This class is the entrypoint for the mod on the Fabric platform.
 */
public class ExampleModFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        ExampleMod.init();
        CBTEntityTypes.registerEntityAttributes(FabricDefaultAttributeRegistry::register);
    }
}
