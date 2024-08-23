package dev.corgitaco.battletowers.forge;

import dev.corgitaco.battletowers.ExampleMod;
import dev.corgitaco.battletowers.client.entity.render.CBTEntityRenderers;
import dev.corgitaco.battletowers.forge.platform.ForgeRegistrationService;
import dev.corgitaco.battletowers.world.entity.CBTEntityTypes;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.function.Consumer;

/**
 * Main class for the mod on the Forge platform.
 */
@Mod(ExampleMod.MOD_ID)
public class ExampleModForge {
    public ExampleModForge() {
        ExampleMod.init();
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ForgeRegistrationService.CACHED.values().forEach(deferredRegister -> deferredRegister.register(modEventBus));
        ForgeRegistrationService.DATAPACK_REGISTRIES.forEach(modEventBus::addListener);

        modEventBus.addListener((Consumer<EntityRenderersEvent.RegisterRenderers>) event -> CBTEntityRenderers.registerEntityRenderers(event::registerEntityRenderer));
        modEventBus.addListener((Consumer<EntityAttributeCreationEvent>) event -> CBTEntityTypes.registerEntityAttributes(event::put));

    }
}
