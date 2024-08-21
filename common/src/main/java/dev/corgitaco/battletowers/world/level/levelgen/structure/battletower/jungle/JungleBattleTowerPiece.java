package dev.corgitaco.battletowers.world.level.levelgen.structure.battletower.jungle;

import dev.corgitaco.battletowers.world.level.levelgen.structure.CBTStructurePieceTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

public class JungleBattleTowerPiece extends StructurePiece {

    private final BlockPos origin;

    public JungleBattleTowerPiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(CBTStructurePieceTypes.JUNGLE_BATTLE_TOWER_PIECE.get(), tag);
        this.origin = NbtUtils.readBlockPos(tag.getCompound("origin"));
    }

    public JungleBattleTowerPiece(int genDepth, BoundingBox boundingBox, BlockPos origin) {
        super(CBTStructurePieceTypes.JUNGLE_BATTLE_TOWER_PIECE.get(), genDepth, boundingBox);
        this.origin = origin;
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.put("origin", NbtUtils.writeBlockPos(this.origin));
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random, BoundingBox box, ChunkPos chunkPos, BlockPos pos) {
        JungleBattleTowerStructure.forAllPositions(this.origin, new XoroshiroRandomSource(origin.asLong() + level.getSeed()), pos1 -> {
            if (box.isInside(pos1)) {
                level.setBlock(pos1, Blocks.JUNGLE_WOOD.defaultBlockState(), 2);
            }
        }, branch -> {
        }, pos1 -> {
        });
    }
}
