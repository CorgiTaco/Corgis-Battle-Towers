package dev.corgitaco.battletowers.forge;

import dev.corgitaco.battletowers.ExampleMod;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

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
    }
}
