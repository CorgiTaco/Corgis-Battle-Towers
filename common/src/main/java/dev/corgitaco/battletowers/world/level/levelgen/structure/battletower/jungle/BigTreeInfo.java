package dev.corgitaco.battletowers.world.level.levelgen.structure.battletower.jungle;

import it.unimi.dsi.fastutil.longs.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public record BigTreeInfo(
    TrunkInfo trunkInfo,
    BranchInfo branchInfo

) {
    public record TrunkInfo(Long2ObjectMap<BitSetChunkData> insideTrunkPositions,
                            Long2ObjectMap<BitSetChunkData> wallPositions,
                            Long2ObjectMap<BitSetChunkData> trunkEdgePositions
    ) {
    }

    public record BranchInfo(Long2ObjectMap<BitSetChunkData> branchPositions,
                             Long2ObjectMap<BitSetChunkData> branchEdgePositions,
                             Long2ObjectMap<BitSetChunkData> branchLeavePositions,
                             List<LongList> branches
    ) {
    }


    public static @NotNull BigTreeInfo getBigTreeInfo(BlockPos pos, long seed, Supplier<BitSetChunkData> chunkDataSupplier) {
        Long2ObjectMap<BitSetChunkData> insideTrunkPositions = new Long2ObjectOpenHashMap<>();
        Long2ObjectMap<BitSetChunkData> placedTrunkPositions = new Long2ObjectOpenHashMap<>();
        Long2ObjectMap<BitSetChunkData> trunkEdgePositions = new Long2ObjectOpenHashMap<>();
        Long2ObjectMap<BitSetChunkData> branchPositions = new Long2ObjectOpenHashMap<>();
        Long2ObjectMap<BitSetChunkData> branchEdgePositions = new Long2ObjectOpenHashMap<>();
        Long2ObjectMap<BitSetChunkData> leavePositions = new Long2ObjectOpenHashMap<>();
        List<LongList> branches = new ArrayList<>();

        Consumer<BlockPos> trunkLogPlacer = trunkLogPos -> placedTrunkPositions.computeIfAbsent(ChunkPos.asLong(trunkLogPos), key -> chunkDataSupplier.get()).add(trunkLogPos);
        Consumer<BlockPos> trunkInsidePlacer = insideTrunkPos -> insideTrunkPositions.computeIfAbsent(ChunkPos.asLong(insideTrunkPos), key -> chunkDataSupplier.get()).add(insideTrunkPos);
        Consumer<BlockPos> branchLogPlacer = branchLogPos -> branchPositions.computeIfAbsent(ChunkPos.asLong(branchLogPos), key -> chunkDataSupplier.get()).add(branchLogPos);


        Consumer<List<BlockPos>> branchGetter = branch -> {
            LongList positions = new LongArrayList(branch.size());
            for (BlockPos blockPos : branch) {
                positions.add(blockPos.asLong());
            }
            branches.add(LongLists.unmodifiable(positions));
        };

        Consumer<BlockPos> leavePlacer = leavesPlacer -> leavePositions.computeIfAbsent(ChunkPos.asLong(leavesPlacer), key -> chunkDataSupplier.get()).add(leavesPlacer);

        XoroshiroRandomSource randomSource = new XoroshiroRandomSource(pos.asLong() + seed);
        JungleBattleTowerStructure.forAllPositions(pos,
                randomSource,
                trunkLogPlacer,
                trunkInsidePlacer,
                branchLogPlacer,
                branchGetter,
                leavePlacer
        );

        BigTreeInfo cachedTreeData1 = new BigTreeInfo(
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
                ));
        return cachedTreeData1;
    }
}