package dev.corgitaco.floatingislands;

import dev.corgitaco.floatingislands.core.FloatingIslandsRegistries;
import dev.corgitaco.floatingislands.core.world.data.*;
import dev.corgitaco.floatingislands.level.gen.config.FloatingIslandBuilders;
import dev.corgitaco.floatingislands.core.world.data.FloatingIslandComponentBuilders;
import dev.corgitaco.floatingislands.providers.FloatingIslandsDatapackBuiltinEntriesProvider;
import dev.corgitaco.floatingislands.providers.FloatingIslandsWorldPresetTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = Constants.MOD_ID)
public class FloatingIslandsDataProvider {

    private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(FloatingIslandsRegistries.FAST_NOISE, context -> {
                FastNoiseLiteBuilders.FACTORIES.forEach((key, factory) -> {
                    context.register(key, factory.generate(context));
                });
            })
            .add(FloatingIslandsRegistries.NOISE_INTERPOLATOR, context -> {
                NoiseInterpolatorBuilders.FACTORIES.forEach((key, factory) -> {
                    context.register(key, factory.generate(context));
                });
            })
            .add(FloatingIslandsRegistries.SHAPE_2D, context -> {
                Shape2DBuilders.FACTORIES.forEach((key, factory) -> {
                    context.register(key, factory.generate(context));
                });
            })
            .add(FloatingIslandsRegistries.NDF, context -> {
                NDFBuilders.FACTORIES.forEach((key, factory) -> {
                    context.register(key, factory.generate(context));
                });
            })
            .add(FloatingIslandsRegistries.FLOATING_ISLAND_COMPONENTS, context -> {
                FloatingIslandComponentBuilders.FACTORIES.forEach((key, factory) -> {
                    context.register(key, factory.generate(context));
                });
            })
            .add(FloatingIslandsRegistries.FLOATING_ISLANDS, context -> {
                FloatingIslandBuilders.FACTORIES.forEach((key, factory) -> {
                    context.register(key, factory.generate(context));
                });
            })
            .add(FloatingIslandsRegistries.FLOATING_ISLAND_GENERATOR_SETTINGS, context -> {
                FloatingIslandGeneratorSettingsRegistry.FACTORIES.forEach((key, factory) -> {
                    context.register(key, factory.generate(context));
                });
            })
            .add(Registries.WORLD_PRESET, context -> {
                FloatingIslandWorldPresets.WORLD_PRESET_FACTORIES.forEach((key, factory) -> {
                    context.register(key, factory.generate(context));

                });
            });

    @SubscribeEvent
    public static void onGatherData(GatherDataEvent event) {
        final var gen = event.getGenerator();
        CommonClass.init();

        DatapackBuiltinEntriesProvider datapackProvider = new FloatingIslandsDatapackBuiltinEntriesProvider(event.getGenerator().getPackOutput(), event.getLookupProvider(), BUILDER, Set.of(Constants.MOD_ID));

        CompletableFuture<HolderLookup.Provider> datapackRegistryProvider = datapackProvider.getRegistryProvider();

        gen.addProvider(event.includeServer(), datapackProvider);
        gen.addProvider(event.includeServer(), new FloatingIslandsWorldPresetTagsProvider(gen.getPackOutput(), datapackRegistryProvider, event.getExistingFileHelper()));
    }
}
