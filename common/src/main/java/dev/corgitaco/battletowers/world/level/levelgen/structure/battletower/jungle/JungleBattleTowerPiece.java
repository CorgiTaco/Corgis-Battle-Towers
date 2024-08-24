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
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

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
            LongSet trunkWallPositions = treeInfo.trunkInfo().wallPositions().get(chunkPosLong);
            if (trunkWallPositions != null) {
                trunkWallPositions.forEach(value -> {
                    mutable.set(value);
                    if (this.boundingBox.isInside(mutable)) {
                        chunk.setBlockState(mutable, Blocks.JUNGLE_WOOD.defaultBlockState(), false);
                    }
                });
            }
        }
        {
            LongSet branchPositions = treeInfo.branchInfo().branchPositions().get(chunkPosLong);
            if (branchPositions != null) {
                branchPositions.forEach(value -> {
                    mutable.set(value);
                    if (this.boundingBox.isInside(mutable)) {
                        chunk.setBlockState(mutable, Blocks.JUNGLE_WOOD.defaultBlockState(), false);
                    }
                });
            }
        }

        {
            LongSet leavePositions = treeInfo.branchInfo().branchLeavePositions().get(chunkPosLong);
            if (leavePositions != null) {
                leavePositions.forEach(value -> {
                    mutable.set(value);
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
            bigTreeInfo.loadInfo(level.getSeed());

            return bigTreeInfo.cachedTreeData;
        }


        return null;

    }

    public void loadInfo(long seed) {
        if (!calculated) {
            synchronized (this) {
                if (!calculated) {
                    Long2ObjectMap<LongSet> insideTrunkPositions = new Long2ObjectOpenHashMap<>();
                    Long2ObjectMap<LongSet> placedTrunkPositions = new Long2ObjectOpenHashMap<>();
                    Long2ObjectMap<LongSet> trunkEdgePositions = new Long2ObjectOpenHashMap<>();
                    Long2ObjectMap<LongSet> branchPositions = new Long2ObjectOpenHashMap<>();
                    Long2ObjectMap<LongSet> branchEdgePositions = new Long2ObjectOpenHashMap<>();
                    Long2ObjectMap<LongSet> leavePositions = new Long2ObjectOpenHashMap<>();
                    List<LongList> branches = new ArrayList<>();

                    Consumer<BlockPos> trunkLogPlacer = trunkLogPos -> placedTrunkPositions.computeIfAbsent(ChunkPos.asLong(trunkLogPos), key -> new LongOpenHashBigSet()).add(trunkLogPos.asLong());
                    Consumer<BlockPos> trunkInsidePlacer = insideTrunkPos -> insideTrunkPositions.computeIfAbsent(ChunkPos.asLong(insideTrunkPos), key -> new LongOpenHashBigSet()).add(insideTrunkPos.asLong());
                    Consumer<BlockPos> branchLogPlacer = branchLogPos -> branchPositions.computeIfAbsent(ChunkPos.asLong(branchLogPos), key -> new LongOpenHashBigSet()).add(branchLogPos.asLong());


                    Consumer<List<BlockPos>> branchGetter = branch -> {
                        LongList positions = new LongArrayList(branch.size());
                        for (BlockPos blockPos : branch) {
                            positions.add(blockPos.asLong());
                        }
                        branches.add(LongLists.unmodifiable(positions));
                    };

                    Consumer<BlockPos> leavePlacer = leavesPlacer -> leavePositions.computeIfAbsent(ChunkPos.asLong(leavesPlacer), key -> new LongOpenHashBigSet()).add(leavesPlacer.asLong());

                    XoroshiroRandomSource randomSource = new XoroshiroRandomSource(origin.asLong() + seed);
                    JungleBattleTowerStructure.forAllPositions(this.origin,
                            randomSource,
                            trunkLogPlacer,
                            trunkInsidePlacer,
                            branchLogPlacer,
                            branchGetter,
                            leavePlacer
                    );

                    this.cachedTreeData = new BigTreeInfo(
                            new BigTreeInfo.TrunkInfo(
                                    Long2ObjectMaps.unmodifiable(insideTrunkPositions),
                                    Long2ObjectMaps.unmodifiable(placedTrunkPositions),
                                    Long2ObjectMaps.unmodifiable(trunkEdgePositions)
                            ),
                            new BigTreeInfo.BranchInfo(
                                    Long2ObjectMaps.unmodifiable(branchPositions),
                                    Long2ObjectMaps.unmodifiable(branchEdgePositions),
                                    Long2ObjectMaps.unmodifiable(leavePositions),
                                    Collections.unmodifiableList(branches)
                            ))
                    ;

                    this.calculated = true;
                }
            }
        }
    }
}
