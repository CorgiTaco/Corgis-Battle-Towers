package dev.corgitaco.battletowers.world.level.levelgen.structure;

import dev.corgitaco.battletowers.ExampleMod;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;

import java.util.List;
import java.util.Map;

public class CBTStructureSets {

    public static final Map<ResourceKey<StructureSet>, StructureSetFactory> STRUCTURE_SET_FACTORIES = new Reference2ObjectOpenHashMap<>();

    public static final ResourceKey<StructureSet> JUNGLE_BATTLE_TOWER = register("jungle_battle_tower", structureHolderGetter ->
            new StructureSet(List.of(StructureSet.entry(structureHolderGetter.getOrThrow(CBTStructures.JUNGLE_BATTLE_TOWER))), new RandomSpreadStructurePlacement(36, 9, RandomSpreadType.TRIANGULAR, 348457856))
    );

    private static ResourceKey<StructureSet> register(String id, StructureSetFactory factory) {
        ResourceKey<StructureSet> structureSetResourceKey = ResourceKey.create(Registries.STRUCTURE_SET, ExampleMod.id(id));
        STRUCTURE_SET_FACTORIES.put(structureSetResourceKey, factory);
        return structureSetResourceKey;
    }

    @FunctionalInterface
    public interface StructureSetFactory {
        StructureSet generate(HolderGetter<Structure> placedFeatureHolderGetter);
    }
}
