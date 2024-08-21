package dev.corgitaco.battletowers.forge.datagen;

import dev.corgitaco.battletowers.ExampleMod;
import dev.corgitaco.battletowers.world.level.levelgen.structure.CBTStructureSets;
import dev.corgitaco.battletowers.world.level.levelgen.structure.CBTStructures;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * This class is used to register the data generators for the mod.
 *
 * @author Joseph T. McQuigg
 * @see GatherDataEvent
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = ExampleMod.MOD_ID)
class DataGeneratorsRegister {
    private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.STRUCTURE, context -> CBTStructures.STRUCTURE_FACTORIES.forEach((structureResourceKey, structureFactory) -> context.register(structureResourceKey, structureFactory.generate(context))))
            .add(Registries.STRUCTURE_SET, context -> CBTStructureSets.STRUCTURE_SET_FACTORIES.forEach((structureSetResourceKey, structureSetFactory) -> context.register(structureSetResourceKey, structureSetFactory.generate(context.lookup(Registries.STRUCTURE)))));


    @SubscribeEvent
    protected static void gatherData(final GatherDataEvent event) {
        ExampleMod.init();
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        DatapackBuiltinEntriesProvider datapackBuiltinEntriesProvider = new DatapackBuiltinEntriesProvider(output, lookupProvider, BUILDER, Set.of(ExampleMod.MOD_ID));
        generator.addProvider(event.includeServer(), datapackBuiltinEntriesProvider);
    }
}
