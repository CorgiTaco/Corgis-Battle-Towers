package dev.corgitaco.battletowers.world.level.levelgen.structure.battletower.jungle;

import dev.corgitaco.battletowers.world.level.levelgen.structure.CBTStructurePieceTypes;
import dev.corgitaco.battletowers.world.level.levelgen.structure.CBTStructures;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

import javax.annotation.Nullable;
import java.util.List;

public class JungleBattleTowerPiece extends StructurePiece {

    private final BlockPos origin;

    @Nullable
    private TreeGenerator cachedTreeData = null;

    private volatile boolean calculated = false;

    public JungleBattleTowerPiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(CBTStructurePieceTypes.JUNGLE_BATTLE_TOWER_PIECE.get(), tag);
        this.origin = NbtUtils.readBlockPos(tag.getCompound("origin"));
    }

    public JungleBattleTowerPiece(int genDepth, BoundingBox boundingBox, BlockPos origin, TreeGenerator treeGenerator) {
        super(CBTStructurePieceTypes.JUNGLE_BATTLE_TOWER_PIECE.get(), genDepth, boundingBox);
        this.origin = origin;
        this.cachedTreeData = treeGenerator;
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.put("origin", NbtUtils.writeBlockPos(this.origin));
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random, BoundingBox box, ChunkPos chunkPos, BlockPos pos) {

        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        TreeGenerator treeInfo = getTreeInfo(chunkPos, structureManager, level);

        ChunkAccess chunk = level.getChunk(chunkPos.x, chunkPos.z);

        {
            ChunkData trunkWallPositions = treeInfo.getPositions(chunkPos, TreeGenerator.TreeComponent.TRUNK_LOGS);
            if (trunkWallPositions != null) {
                trunkWallPositions.forEach(chunkPos, (x, y, z) -> {
                    mutable.set(x, y, z);
                    if (this.boundingBox.isInside(mutable)) {
                        chunk.setBlockState(mutable, Blocks.JUNGLE_WOOD.defaultBlockState(), false);

                    }
                });
            }
        }
        {
            ChunkData branchPositions = treeInfo.getPositions(chunkPos, TreeGenerator.TreeComponent.BRANCH_LOGS);
            if (branchPositions != null) {
                branchPositions.forEach(chunkPos, (x, y, z) -> {
                    mutable.set(x, y, z);
                    if (this.boundingBox.isInside(mutable)) {
                        chunk.setBlockState(mutable, Blocks.JUNGLE_WOOD.defaultBlockState(), false);
                    }
                });
            }
        }

        {
            ChunkData leavePositions = treeInfo.getPositions(chunkPos, TreeGenerator.TreeComponent.BRANCH_LEAVES);
            if (leavePositions != null) {
                leavePositions.forEach(chunkPos, (x, y, z) -> {
                    mutable.set(x, y, z);
                    if (this.boundingBox.isInside(mutable) && chunk.getBlockState(mutable).isAir()) {
                        chunk.setBlockState(mutable, Blocks.JUNGLE_LEAVES.defaultBlockState(), false);
                        chunk.markPosForPostprocessing(mutable);
                    }
                });
            }
        }
    }

    @Nullable
    public TreeGenerator getTreeInfo(ChunkPos currentGeneratingChunk, StructureManager structureManager, WorldGenLevel level) {
        List<StructureStart> structureAt = structureManager.startsForStructure(SectionPos.of(currentGeneratingChunk, level.getMinSection()), level.registryAccess().registry(Registries.STRUCTURE).orElseThrow().getOrThrow(CBTStructures.JUNGLE_BATTLE_TOWER));

        if (cachedTreeData == null) {

            for (StructureStart structureStart : structureAt) {
                StructurePiece structurePiece = structureStart.getPieces().get(0);

                if (structurePiece instanceof JungleBattleTowerPiece jungleBattleTowerPiece) {
                    if (jungleBattleTowerPiece.origin.equals(this.origin)) {
                        jungleBattleTowerPiece.loadInfo(level.getSeed(), level);

                        return jungleBattleTowerPiece.cachedTreeData;
                    }
                }
            }
        }


        return cachedTreeData;
    }


    public void loadInfo(long seed, LevelHeightAccessor levelHeightAccessor) {
        if (!calculated) {
            synchronized (this) {
                if (!calculated) {
                    this.cachedTreeData = new TreeGenerator(this.origin, seed, 128, () -> new IntSetChunkData(16, levelHeightAccessor, IntOpenHashSet::new));

                    this.calculated = true;
                }
            }
        }
    }
}
