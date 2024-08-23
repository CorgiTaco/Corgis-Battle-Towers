package dev.corgitaco.battletowers.world.level.levelgen.structure.battletower.jungle;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.List;

public record BigTreeInfo(
    TrunkInfo trunkInfo,
    BranchInfo branchInfo

) {
    public record TrunkInfo(Long2ObjectMap<LongSet> insideTrunkPositions,
                            Long2ObjectMap<LongSet> wallPositions,
                            Long2ObjectMap<LongSet> trunkEdgePositions
    ) {
    }

    public record BranchInfo(Long2ObjectMap<LongSet> branchPositions,
                             Long2ObjectMap<LongSet> branchEdgePositions,
                             Long2ObjectMap<LongSet> branchLeavePositions,
                             List<LongList> branches
    ) {
    }
}