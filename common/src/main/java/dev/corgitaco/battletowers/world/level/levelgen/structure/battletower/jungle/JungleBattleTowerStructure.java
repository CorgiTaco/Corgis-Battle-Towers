package dev.corgitaco.battletowers.world.level.levelgen.structure.battletower.jungle;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.corgitaco.battletowers.world.level.levelgen.structure.CBTStructureTypes;
import dev.corgitaco.battletowers.world.level.levelgen.structure.UnsafeBoundingBox;
import it.unimi.dsi.fastutil.longs.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class JungleBattleTowerStructure extends Structure {

    public static final Codec<JungleBattleTowerStructure> CODEC = RecordCodecBuilder.<JungleBattleTowerStructure>mapCodec(builder ->
            builder.group(
                    settingsCodec(builder)
            ).apply(builder, JungleBattleTowerStructure::new)
    ).codec();

    public JungleBattleTowerStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();


        RandomState randomState = context.randomState();
        WorldgenRandom random = context.random();
        ChunkGenerator chunkGenerator = context.chunkGenerator();

        int blockX = chunkPos.getBlockX(random.nextInt(16));
        int blockZ = chunkPos.getBlockZ(random.nextInt(16));
        BlockPos origin = new BlockPos(blockX, chunkGenerator.getBaseHeight(blockX, blockZ, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), randomState), blockZ);


        int blendRadius = 128;
        int blendStep = 16;

        Pair<BlockPos, Holder<Biome>> biomeHorizontal = context.biomeSource().findBiomeHorizontal(origin.getX(), origin.getY(), origin.getZ(), blendRadius, blendStep, biomeHolder -> !context.validBiome().test(biomeHolder), random, false, context.randomState().sampler());

        if (biomeHorizontal != null) {
            return Optional.empty();
        }

        return onTopOfChunkCenter(context, Heightmap.Types.OCEAN_FLOOR_WG, (piecesBuilder) -> {
            XoroshiroRandomSource xoroshiroRandomSource = new XoroshiroRandomSource(origin.asLong() + context.seed());

            Long2ObjectOpenHashMap<UnsafeBoundingBox> map = new Long2ObjectOpenHashMap<>();

            Consumer<BlockPos> boxCreation = pos -> map.computeIfAbsent(ChunkPos.asLong(pos), key -> new UnsafeBoundingBox()).encapsulate(pos);

            forAllPositions(origin, xoroshiroRandomSource, boxCreation, lists -> {
            }, boxCreation);

            for (UnsafeBoundingBox value : map.values()) {
                piecesBuilder.addPiece(new JungleBattleTowerPiece(0, value.toBoundingBox(), origin));
            }
        });
    }

    @Override
    public StructureType<?> type() {
        return CBTStructureTypes.JUNGLE_BATTLE_TOWER.get();
    }


    public static void forAllPositions(BlockPos origin, RandomSource randomSource, Consumer<BlockPos> trunkPlacer, Consumer<List<BlockPos>> branchPlacer, Consumer<BlockPos> leavePlacer) {
        forAllPositions(8, 16, origin, randomSource, trunkPlacer, branchPlacer, leavePlacer);
    }

    public static void forAllPositions(int range, int width, BlockPos origin, RandomSource randomSource, Consumer<BlockPos> trunkPlacer, Consumer<List<BlockPos>> branchPlacer, Consumer<BlockPos> leavePlacer) {
        LongList treeTrunkPositions = getTreeTrunkPositions(range, origin, randomSource);
        LongSet positions = positions(treeTrunkPositions, width, trunkPlacer);
        generateBranches(randomSource, treeTrunkPositions, pos -> {
            if (!positions.contains(pos.asLong())) {
                trunkPlacer.accept(pos);
            }
        }, branchPlacer, leavePlacer);
    }


    public static LongList getTreeTrunkPositions(int range, BlockPos origin, RandomSource randomSource) {
        LongList positions = new LongArrayList();


        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos().set(origin);

        int height = randomSource.nextInt(150, 170);

        positions.add(mutableBlockPos.asLong());

        int rawStep = 0;
        int heightOffset = randomSource.nextInt(20, 35);


        double noise = (new ImprovedNoise(randomSource).noise(0, 0, 0) + 1) * 0.5F;
        double angle = Mth.clampedLerp(-180, 180, noise);
        double xOffset = Math.cos(angle) * range;

        double zOffset = Math.sin(angle) * range;

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


    public static LongSet positions(LongList treeTrunkPositions, int width, Consumer<BlockPos> logPlacer) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

        LongSet trunkPositions = new LongOpenHashBigSet();
        for (int i = 0; i < treeTrunkPositions.size() - 1; i++) {
            long current = treeTrunkPositions.getLong(i);
            mutableBlockPos.set(current);
            BlockPos currentTrunkPos = mutableBlockPos.immutable();

            long next = treeTrunkPositions.getLong(i + 1);
            mutableBlockPos.set(next);
            BlockPos nextTrunkPos = mutableBlockPos.immutable();

            BlockPos difference = nextTrunkPos.subtract(currentTrunkPos);
            Vector3d normalize = normalize(difference);

            double distance = length(difference);

            for (int step = 0; step < distance; step++) {
                BlockPos offset = currentTrunkPos.offset((int) (normalize.x * step), (int) (normalize.y * step), (int) (normalize.z * step));
                sphereAround(width, offset, pos -> trunkPositions.add(pos.asLong()));
            }
        }


        ImprovedNoise improvedNoise = new ImprovedNoise(new LegacyRandomSource(0));


        trunkPositions.forEach(value -> {
            int y = BlockPos.getY(value);
            mutableBlockPos.set(value);
            if (y % 10 == 5) {
                logPlacer.accept(mutableBlockPos);
            }

            double noise = (improvedNoise.noise(mutableBlockPos.getX() * 0.1, mutableBlockPos.getY() * 0.1, mutableBlockPos.getZ() * 0.1) + 1) * 0.5;

            int maxDepth = (int) Mth.clampedLerp(1, 4, noise);

            boolean accept = false;

            for (int depth = 1; depth <= maxDepth; depth++) {
                for (Direction direction : Direction.values()) {
                    long other = BlockPos.asLong(BlockPos.getX(value) + (direction.getStepX() * depth), y + (direction.getStepY() * depth), BlockPos.getZ(value) + (direction.getStepZ() * depth));
                    if (!trunkPositions.contains(other)) {
                        accept = true;
                        break;
                    }
                }
            }
            if (accept) {
                logPlacer.accept(mutableBlockPos.set(value));
            }
        });

        return trunkPositions;
    }

    private static void generateBranches(RandomSource randomSource, LongList treeTrunkPositions, Consumer<BlockPos> logPlacer, Consumer<List<BlockPos>> branchesGetter, Consumer<BlockPos> leavesPlacer) {
        for (int i = 1; i < treeTrunkPositions.size() - 1; i++) {
            long currentTreeTrunkPackedPosition = treeTrunkPositions.getLong(i);
            BlockPos currentTrunkPos = BlockPos.of(currentTreeTrunkPackedPosition);

            long nextTreeTrunkPackedPosition = treeTrunkPositions.getLong(i + 1);
            BlockPos nextTrunkPos = BlockPos.of(nextTreeTrunkPackedPosition);


            BlockPos difference = nextTrunkPos.subtract(currentTrunkPos);

            Vector3d normalized = normalize(difference);

            double distance = length(difference);

            for (double pct = 0; pct < 1.0; pct += 0.3) {
                double scalar = distance * pct;
                BlockPos branchOrigin = currentTrunkPos.offset((int) (normalized.x * scalar), (int) (normalized.y * scalar), (int) (normalized.z * scalar));

                List<BlockPos> branchPositions = new ArrayList<>();
                recursivelyGenerateBranches(3, 1, 5, UniformInt.of(20, 30), UniformInt.of(-6, 0), UniformFloat.of(0, 360), randomSource, logPlacer, pos -> {
                    branchPositions.add(pos.immutable());
                }, leavesPlacer, branchOrigin, 0);

                branchesGetter.accept(branchPositions);
            }
        }

        {
            long currentTreeTrunkPackedPosition = treeTrunkPositions.getLong(treeTrunkPositions.size() - 1);
            BlockPos currentTrunkPos = BlockPos.of(currentTreeTrunkPackedPosition);

            List<BlockPos> branchPositions = new ArrayList<>();
            recursivelyGenerateBranches(2, randomSource.nextInt(10, 15), 4, UniformInt.of(20, 40), UniformInt.of(7, 15), UniformFloat.of(0, 360), randomSource, logPlacer, pos -> {
                branchPositions.add(pos.immutable());
            }, leavesPlacer, currentTrunkPos, 0);

            branchesGetter.accept(branchPositions);
        }


    }

    private static void recursivelyGenerateBranches(int totalSegmentCount, int branchCount, int branchRadius, IntProvider rangeGetter, IntProvider yOffset, FloatProvider angleGetter, RandomSource randomSource, Consumer<BlockPos> logPlacer, Consumer<BlockPos> branchOrigins, Consumer<BlockPos> leavesPlacer, BlockPos currentTrunkPos, int internalCount) {
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

            for (int step = 0; step < distance; step++) {

                double stepPct = ((double) step) / distance;

                BlockPos offset = currentTrunkPos.offset((int) (normalize.x * step), (int) (normalize.y * step), (int) (normalize.z * step));
                branchOrigins.accept(offset);
                sphereAround(branchRadius, offset, logPlacer);

                if (randomSource.nextDouble() < stepPct) {
                    BlockPos.MutableBlockPos leaveSpawner = new BlockPos.MutableBlockPos();

                    for (int i = 0; i < randomSource.nextInt(branchRadius + 8); i++) {
                        leaveSpawner.setWithOffset(offset, Direction.values()[randomSource.nextInt(Direction.values().length - 1)]);
                        logPlacer.accept(leaveSpawner);

                    }

                    sphereAround(randomSource.nextInt(4, 6), Mth.randomBetween(randomSource, 0.1F, 0.6F), Mth.randomBetween(randomSource, 0.1F, 0.6F), leaveSpawner, leavesPlacer);
                }
            }

            if (internalCount < totalSegmentCount) {
                float angleRange = angleGetter.getMaxValue() - angleGetter.getMinValue();
                float minInclusive = angle - (Math.max(45, angleRange * 0.4F));
                float maxExclusive = angle + (Math.max(45, angleRange * 0.4F));
                recursivelyGenerateBranches(totalSegmentCount, branchCount + randomSource.nextIntBetweenInclusive(1, 2), branchRadius - 1, ConstantInt.of((int) (range * 0.7F)), yOffset, UniformFloat.of(minInclusive, maxExclusive), randomSource, logPlacer, branchOrigins, leavesPlacer, endPos, internalCount + 1);
            }
        }
    }

    public static void sphereAround(int width, BlockPos origin, Consumer<BlockPos> consumer) {
        sphereAround(width, 0.1F, 0.5F, origin, consumer);
    }

    public static void sphereAround(int width, double freq, double minRadius, BlockPos origin, Consumer<BlockPos> consumer) {
        ImprovedNoise noise = new ImprovedNoise(new LegacyRandomSource(0));
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int x = -width; x <= width; x++) {
            for (int y = -width; y <= width; y++) {
                for (int z = -width; z <= width; z++) {
                    mutable.setWithOffset(origin, x, y, z);
                    if (mutable.closerThan(origin, width)) {
                        double noise1 = (noise.noise(mutable.getX() * freq, mutable.getY() * freq, mutable.getZ() * freq) + 1) * 0.5;
                        double localRadius = Mth.clampedLerp(width * minRadius, width, noise1);
                        if (mutable.closerThan(origin, localRadius)) {
                            consumer.accept(mutable);
                        }
                    }
                }
            }
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
}