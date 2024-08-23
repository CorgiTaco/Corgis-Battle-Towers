package dev.corgitaco.battletowers.fabric.client;

import dev.corgitaco.battletowers.client.entity.render.CBTEntityRenderers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class ExampleModFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        CBTEntityRenderers.registerEntityRenderers(EntityRendererRegistry::register);
    }
}
