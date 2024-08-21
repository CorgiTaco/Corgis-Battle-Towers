package dev.corgitaco.battletowers.fabric;

import dev.corgitaco.battletowers.ExampleMod;
import net.fabricmc.api.ModInitializer;

/**
 * This class is the entrypoint for the mod on the Fabric platform.
 */
public class ExampleModFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        ExampleMod.init();
    }
}
