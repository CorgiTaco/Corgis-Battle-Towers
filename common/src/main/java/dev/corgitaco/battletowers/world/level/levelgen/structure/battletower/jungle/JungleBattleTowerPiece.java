package dev.corgitaco.battletowers.world.level.levelgen.structure.battletower.jungle;

import dev.corgitaco.battletowers.world.entity.CBTEntityTypes;
import dev.corgitaco.battletowers.world.entity.DurianTurretEntity;
import dev.corgitaco.battletowers.world.level.levelgen.structure.CBTStructurePieceTypes;
import dev.corgitaco.battletowers.world.level.levelgen.structure.CBTStructures;
import it.unimi.dsi.fastutil.longs.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class JungleBattleTowerPiece extends StructurePiece {

    private final BlockPos origin;

    @Nullable
    private BigTreeInfo cachedTreeData = null;

    private volatile boolean calculated = false;

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

        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        BigTreeInfo treeInfo = getTreeInfo(structureManager, level);

        ChunkAccess chunk = level.getChunk(chunkPos.x, chunkPos.z);

        long chunkPosLong = chunkPos.toLong();
        {
            BitSetChunkData trunkWallPositions = treeInfo.trunkInfo().wallPositions().get(chunkPosLong);
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
            BitSetChunkData branchPositions = treeInfo.branchInfo().branchPositions().get(chunkPosLong);
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
            BitSetChunkData leavePositions = treeInfo.branchInfo().branchLeavePositions().get(chunkPosLong);
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


        for (LongList branch : treeInfo.branchInfo().branches()) {


            int tries = 0;
            for (int i = 0; i < branch.size(); i++) {
                long value = branch.getLong(i);

                mutable.set(value);

                if (this.boundingBox.isInside(mutable)) {

                    tries++;

                    if (random.nextDouble() < 0.003) {

                        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos().set(mutable);
                        for (int y = 0; y < 20; y++) {
                            if (chunk.getBlockState(mutableBlockPos).isAir()) {
                                DurianTurretEntity entity = CBTEntityTypes.DURIAN_TURRET.get().create(level.getLevel());
                                entity.setPos(Vec3.atBottomCenterOf(mutableBlockPos));
                                level.addFreshEntity(entity);
                                break;
                            }
                            mutableBlockPos.move(Direction.DOWN);
                        }
                    }
                }

            }


            if (tries > 2) {
                break;
            }

        }
    }

    @Nullable
    public BigTreeInfo getTreeInfo(StructureManager structureManager, WorldGenLevel level) {
        StructureStart structureAt = structureManager.getStructureAt(this.origin, level.registryAccess().registry(Registries.STRUCTURE).orElseThrow().getOrThrow(CBTStructures.JUNGLE_BATTLE_TOWER));

        StructurePiece structurePiece = structureAt.getPieces().get(0);

        if (structurePiece instanceof JungleBattleTowerPiece bigTreeInfo) {
            bigTreeInfo.loadInfo(level.getSeed(), level);

            return bigTreeInfo.cachedTreeData;
        }


        return null;
    }


    public void loadInfo(long seed, LevelHeightAccessor levelHeightAccessor) {
        if (!calculated) {
            synchronized (this) {
                if (!calculated) {
                    this.cachedTreeData = BigTreeInfo.getBigTreeInfo(this.origin, seed, () -> new BitSetBasedTreeChunkData(16, levelHeightAccessor))
                    ;

                    this.calculated = true;
                }
            }
        }
    }


}
