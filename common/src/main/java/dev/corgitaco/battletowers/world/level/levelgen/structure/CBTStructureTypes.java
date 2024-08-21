package dev.corgitaco.battletowers.world.level.levelgen.structure;

import dev.corgitaco.battletowers.RegistrationService;
import dev.corgitaco.battletowers.world.level.levelgen.structure.battletower.jungle.JungleBattleTowerStructure;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.function.Supplier;

public class CBTStructureTypes {

    public static final Supplier<StructureType<JungleBattleTowerStructure>> JUNGLE_BATTLE_TOWER = create("jungle_battle_tower", () -> () -> JungleBattleTowerStructure.CODEC);


    public static <S extends Structure, ST extends StructureType<S>> Supplier<ST> create(String id, Supplier<ST> structureTypeSupplier) {
        Supplier<ST> register = RegistrationService.INSTANCE.register((net.minecraft.core.Registry<ST>) BuiltInRegistries.STRUCTURE_TYPE, id, structureTypeSupplier);
        return register;
    }

    public static void structureTypes() {
    }
}