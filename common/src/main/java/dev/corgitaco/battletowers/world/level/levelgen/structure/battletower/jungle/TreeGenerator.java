package dev.corgitaco.battletowers.world.level.levelgen.structure.battletower.jungle;

import dev.corgitaco.battletowers.data.collections.ChunkDataLookup;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class TreeGenerator {
    private final RandomSource randomSource;
    private CachedNoiseSampler noise;

    private final ChunkDataLookup<ChunkData>[] treeComponentPositions;

    public TreeGenerator(BlockPos origin, long worldSeed, int horizontalRadius, Supplier<ChunkData> factory) {
        this.randomSource = new XoroshiroRandomSource(worldSeed + origin.asLong());
        this.noise = new CachedNoiseSampler(new ImprovedNoise(randomSource), 0.08F);


        TreeComponent[] values = TreeComponent.values();

        this.treeComponentPositions = new ChunkDataLookup[values.length];
        for (int i = 0; i < values.length; i++) {
            treeComponentPositions[i] = new ChunkDataLookup<>(origin, horizontalRadius, ChunkData[]::new, factory);
        }


        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

        LongList treeTrunkPositions = getTreeTrunkPositions(8, origin, randomSource);

        int trunkRadius = 16;
        NoiseSphereGenerator trunkSphereGenerator = new NoiseSphereGenerator(6, trunkRadius, noise);
        generateTrunk(mutableBlockPos, treeTrunkPositions, trunkSphereGenerator);
        generateBranches(mutableBlockPos, treeTrunkPositions, blockPos -> {
        }, new NoiseSphereGenerator(1, 4, this.noise), new NoiseSphereGenerator(1, 6, this.noise));

        this.noise = null;
    }

    public void generateTrunk(BlockPos.MutableBlockPos mutableBlockPos, LongList treeTrunkPositions, NoiseSphereGenerator sphereGenerator) {

        for (int i = 0; i < treeTrunkPositions.size() - 1; i++) {
            long current = treeTrunkPositions.getLong(i);
            long next = treeTrunkPositions.getLong(i + 1);

            mutableBlockPos.set(BlockPos.getX(current) - BlockPos.getX(next), BlockPos.getY(current) - BlockPos.getY(next), BlockPos.getZ(current) - BlockPos.getZ(next));
            Vector3d normalize = normalize(mutableBlockPos);

            double distance = length(mutableBlockPos);

            long last = Long.MIN_VALUE;
            for (int step = 0; step < distance; step++) {
                mutableBlockPos.set(next).move((int) (normalize.x * step), (int) (normalize.y * step), (int) (normalize.z * step));
                long packed = mutableBlockPos.asLong();
                if (packed != last) {
                    sphereGenerator.sphereAroundForComponent(mutableBlockPos, this, TreeComponent.TRUNK_LOGS);
                    last = packed;
                }

            }
        }


        ChunkDataLookup<ChunkData> trunkLogs = getComponentPositions(TreeComponent.TRUNK_LOGS);

        ChunkDataLookup<ChunkData> trunkInside = getComponentPositions(TreeComponent.TRUNK_INSIDE);


        trunkLogs.forEach((chunkX, chunkZ, value) -> {
            if (value != null) {
                value.forEach(new ChunkPos(chunkX, chunkZ), (x, y, z) -> {
                    boolean putInside = true;
                    for (Direction direction : Direction.values()) {
                        for (int depth = 1; depth <= 3; depth++) {

                            int offsetX = x + (direction.getStepX() * depth);
                            int offsetY = y + (direction.getStepY() * depth);
                            int offsetZ = z + (direction.getStepZ() * depth);

                            if (!trunkLogs.getForWorld(offsetX, offsetZ).occupied(offsetX, offsetY, offsetZ)) {
                                putInside = false;
                                break;
                            }
                        }
                    }

                    if (putInside) {
                        trunkInside.getForWorld(x, z).add(x, y, z);
                    }
                });
            }

        });


        trunkInside.forEach((chunkX, chunkZ, value) -> {
            if (value != null) {
                value.forEach(new ChunkPos(chunkX, chunkZ), trunkLogs.getForChunk(chunkX, chunkZ)::remove);
            }
        });
    }


    public LongList getTreeTrunkPositions(int range, BlockPos origin, RandomSource randomSource) {
        LongList positions = new LongArrayList();

        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos().set(origin);

        int height = randomSource.nextInt(150, 170);

        positions.add(mutableBlockPos.asLong());

        int rawStep = 0;
        int heightOffset = randomSource.nextInt(20, 35);


        double noise = (this.noise.getNoise(origin.getX(), origin.getY(), origin.getZ()) + 1) * 0.5F;
        double angle = Mth.clampedLerp(0, 360, noise);
        double xOffset = Math.cos(Math.toRadians(angle)) * range;

        double zOffset = Math.sin(Math.toRadians(angle)) * range;

        for (int y = heightOffset; y <= height; y += heightOffset) {
            if (rawStep % 2 == 0) {
                mutableBlockPos.set(origin.getX() + xOffset, origin.getY() + y, origin.getZ() + zOffset);
            } else {
                mutableBlockPos.set(origin.getX() - xOffset, origin.getY() + y, origin.getZ() - zOffset);
            }
            positions.add(mutableBlockPos.asLong());

            rawStep++;

        }
        return positions;
    }

    private void generateBranches(BlockPos.MutableBlockPos mutableBlockPos, LongList treeTrunkPositions, Consumer<List<BlockPos>> branchesGetter, NoiseSphereGenerator branchGenerator, NoiseSphereGenerator leafGenerator) {
        for (int i = 1; i < treeTrunkPositions.size() - 1; i++) {
            long currentTreeTrunkPackedPosition = treeTrunkPositions.getLong(i);

            long nextTreeTrunkPackedPosition = treeTrunkPositions.getLong(i + 1);

            mutableBlockPos.set(BlockPos.getX(currentTreeTrunkPackedPosition) - BlockPos.getX(nextTreeTrunkPackedPosition), BlockPos.getY(currentTreeTrunkPackedPosition) - BlockPos.getY(nextTreeTrunkPackedPosition), BlockPos.getZ(currentTreeTrunkPackedPosition) - BlockPos.getZ(nextTreeTrunkPackedPosition));

            Vector3d normalized = normalize(mutableBlockPos);

            double distance = length(mutableBlockPos);

            for (double pct = 0; pct < 1.0; pct += 0.3) {
                double scalar = distance * pct;
                mutableBlockPos.set(currentTreeTrunkPackedPosition).move((int) (normalized.x * scalar), (int) (normalized.y * scalar), (int) (normalized.z * scalar));

                List<BlockPos> branchPositions = new ArrayList<>();
                recursivelyGenerateBranches(3, 1, UniformInt.of(20, 30), UniformInt.of(-6, 0), UniformFloat.of(0, 360), mutableBlockPos, 0, branchGenerator, leafGenerator);

                branchesGetter.accept(branchPositions);
            }
        }

        {
            long currentTreeTrunkPackedPosition = treeTrunkPositions.getLong(treeTrunkPositions.size() - 1);
            BlockPos currentTrunkPos = BlockPos.of(currentTreeTrunkPackedPosition);

            List<BlockPos> branchPositions = new ArrayList<>();
            recursivelyGenerateBranches(2, randomSource.nextInt(10, 15), UniformInt.of(20, 40), UniformInt.of(7, 15), UniformFloat.of(0, 360), currentTrunkPos, 0, branchGenerator, leafGenerator);

            branchesGetter.accept(branchPositions);
        }


    }

    private void recursivelyGenerateBranches(int totalSegmentCount, int branchCount, IntProvider rangeGetter, IntProvider yOffset, FloatProvider angleGetter, BlockPos currentTrunkPos, int internalCount, NoiseSphereGenerator branchGenerator, NoiseSphereGenerator leafGenerator) {
        for (int count = 0; count < branchCount; count++) {
            float angle = angleGetter.sample(randomSource);
            int range = rangeGetter.sample(randomSource);
            int randomYOffset = yOffset.sample(randomSource);
            double xOffset = Math.cos(Math.toRadians(angle)) * range;
            double zOffset = Math.sin(Math.toRadians(angle)) * range;

            BlockPos endPos = currentTrunkPos.offset((int) (xOffset), randomYOffset, (int) (zOffset));
            BlockPos difference = endPos.subtract(currentTrunkPos);
            Vector3d normalize = normalize(difference);

            double distance = length(difference);

            long last = Long.MIN_VALUE;
            for (int step = 0; step < distance; step++) {

                double stepPct = ((double) step) / distance;

                BlockPos offset = currentTrunkPos.offset((int) (normalize.x * step), (int) (normalize.y * step), (int) (normalize.z * step));
                long offsetLong = offset.asLong();
                if (offsetLong == last) {
                    continue;
                }

                last = offsetLong;

                branchGenerator.sphereAroundForComponent(offset, this, TreeComponent.BRANCH_LOGS);

                if (randomSource.nextDouble() < stepPct) {
                    BlockPos.MutableBlockPos leaveSpawner = new BlockPos.MutableBlockPos();

                    for (int i = 0; i < randomSource.nextInt(branchGenerator.maxRadius() + 8); i++) {
                        leaveSpawner.setWithOffset(offset, Direction.values()[randomSource.nextInt(Direction.values().length - 1)]);

                        this.getPositions(leaveSpawner, TreeComponent.BRANCH_LOGS).add(leaveSpawner);
                    }

                    leafGenerator.sphereAroundForComponent(offset, this, TreeComponent.BRANCH_LEAVES);
                }
            }

            if (internalCount < totalSegmentCount) {
                float angleRange = angleGetter.getMaxValue() - angleGetter.getMinValue();
                float minInclusive = angle - (Math.max(45, angleRange * 0.4F));
                float maxExclusive = angle + (Math.max(45, angleRange * 0.4F));
                int newMaxRadius = branchGenerator.maxRadius() - 1;
                NoiseSphereGenerator newBranchGenerator = new NoiseSphereGenerator(newMaxRadius * 0.2F, newMaxRadius, this.noise);
                recursivelyGenerateBranches(totalSegmentCount, branchCount + randomSource.nextIntBetweenInclusive(1, 2), ConstantInt.of((int) (range * 0.7F)), yOffset, UniformFloat.of(minInclusive, maxExclusive), endPos, internalCount + 1, newBranchGenerator, leafGenerator);
            }
        }
    }

    public ChunkData getPositions(ChunkPos pos, TreeComponent treeComponent) {
        return getPositions(pos.x, pos.z, treeComponent);
    }

    public ChunkData getPositions(Vec3i pos, TreeComponent treeComponent) {
        return getPositions(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()), treeComponent);
    }

    public ChunkData getPositions(int chunkX, int chunkZ, TreeComponent treeComponent) {
        return getComponentPositions(treeComponent).getForChunk(chunkX, chunkZ);
    }

    public ChunkDataLookup<ChunkData> getComponentPositions(TreeComponent treeComponent) {
        return this.treeComponentPositions[treeComponent.ordinal()];
    }


    public void forAllAvailableChunkData(ChunkDataLookup.Consumer<ChunkData> bitSetLoopInfo) {
        for (TreeComponent value : TreeComponent.values()) {
            getComponentPositions(value).forEach(bitSetLoopInfo);
        }

    }

    public static Vector3d normalize(Vec3i vector) {
        float length = (float) Math.sqrt(vector.getX() * vector.getX() + vector.getY() * vector.getY() + vector.getZ() * vector.getZ());
        if (length == 0) {
            return new Vector3d(0, 0, 0); // Handle zero-length vector
        }
        return new Vector3d(vector.getX() / length, vector.getY() / length, vector.getZ() / length);
    }

    public static double length(Vec3i vector) {
        return Math.sqrt(vector.getX() * vector.getX() + vector.getY() * vector.getY() + vector.getZ() * vector.getZ());
    }

    public static double hLength(Vec3i vector) {
        return Math.sqrt(vector.getX() * vector.getX() + vector.getZ() * vector.getZ());
    }

    public enum TreeComponent {
        TRUNK_LOGS,
        TRUNK_INSIDE,
        TRUNK_EDGE,
        BRANCH_LOGS,
        BRANCH_LEAVES,
        BRANCH_EDGE
    }
}
