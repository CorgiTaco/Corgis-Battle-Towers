package dev.corgitaco.battletowers.world.level.levelgen.structure;

import dev.corgitaco.battletowers.platform.RegistrationService;
import dev.corgitaco.battletowers.world.level.levelgen.structure.battletower.jungle.JungleBattleTowerPiece;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;

import java.util.function.Supplier;

public class CBTStructurePieceTypes {


    public static final Supplier<StructurePieceType> JUNGLE_BATTLE_TOWER_PIECE = create("jungle_battle_tower_piece", () -> JungleBattleTowerPiece::new);

    public static Supplier<StructurePieceType> create(String id, Supplier<StructurePieceType> structureTypeSupplier) {
        return RegistrationService.INSTANCE.register(BuiltInRegistries.STRUCTURE_PIECE, id, structureTypeSupplier);
    }

    public static void structurePieceTypes(){}
}
