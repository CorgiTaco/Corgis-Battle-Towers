package dev.corgitaco.battletowers.world.level.levelgen.structure;

import dev.corgitaco.battletowers.ExampleMod;
import dev.corgitaco.battletowers.world.level.levelgen.structure.battletower.jungle.JungleBattleTowerStructure;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.Structures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;

import java.util.Map;

public class CBTStructures {
    public static final Map<ResourceKey<Structure>, StructureFactory> STRUCTURE_FACTORIES = new Reference2ObjectOpenHashMap<>();

    public static final ResourceKey<Structure> JUNGLE_BATTLE_TOWER = register("jungle_battle_tower", (structureFactoryBootstapContext) ->
            new JungleBattleTowerStructure(
                    structure(structureFactoryBootstapContext.lookup(Registries.BIOME).getOrThrow(BiomeTags.IS_JUNGLE), GenerationStep.Decoration.RAW_GENERATION, TerrainAdjustment.NONE)
            )
    );


    private static ResourceKey<Structure> register(String id, StructureFactory factory) {
        ResourceKey<Structure> structureSetResourceKey = ResourceKey.create(Registries.STRUCTURE, ExampleMod.id(id));
        STRUCTURE_FACTORIES.put(structureSetResourceKey, factory);
        return structureSetResourceKey;
    }

    private static Structure.StructureSettings structure(HolderSet<Biome> tag, TerrainAdjustment adj) {
        return Structures.structure(tag, Map.of(), GenerationStep.Decoration.SURFACE_STRUCTURES, adj);
    }

    private static Structure.StructureSettings structure(HolderSet<Biome> tag, Map<MobCategory, StructureSpawnOverride> spawnOverrides, TerrainAdjustment adj) {
        return Structures.structure(tag, spawnOverrides, GenerationStep.Decoration.SURFACE_STRUCTURES, adj);
    }

    private static Structure.StructureSettings structure(HolderSet<Biome> tag, GenerationStep.Decoration decoration, TerrainAdjustment adj) {
        return Structures.structure(tag, Map.of(), decoration, adj);
    }

    @FunctionalInterface
    public interface StructureFactory {
        Structure generate(BootstapContext<Structure> structureFactoryBootstapContext);
    }
}
