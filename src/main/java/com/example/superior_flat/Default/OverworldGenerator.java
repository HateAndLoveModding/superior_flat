package com.example.superior_flat.Default;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.random.CheckedRandom;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.util.math.random.RandomSeed;
import net.minecraft.util.math.random.Xoroshiro128PlusPlusRandom;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.*;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.util.PlacedFeatureIndexer;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.gen.structure.Structure;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import java.util.stream.Collectors;
public class OverworldGenerator extends NoiseChunkGenerator {
    public final RegistryEntry<ChunkGeneratorSettings> settings;
    public final Supplier<AquiferSampler.FluidLevelSampler> fluidLevelSampler;
    public final Supplier<List<PlacedFeatureIndexer.IndexedFeatures>> indexedFeaturesListSupplier;
    public static List<Block> worldBlocks = new ArrayList<>();
    public static List<Block> riverBlocks = new ArrayList<>();
    public static List<Block> iceSpikeBlocks = new ArrayList<>();
    public static List<Block> myceliumBlocks = new ArrayList<>();
    public static List<Block> badlandsBlocks = new ArrayList<>();
    public static List<Block> deepOceanBlocks = new ArrayList<>();
    public static List<Block> shallowOceanBlocks = new ArrayList<>();
    public static List<Block> desertBlocks = new ArrayList<>();
    public static List<Block> beachBlocks = new ArrayList<>();
    public static List<Block> stoneBlocks = new ArrayList<>();
    public static List<Block> swampBlocks = new ArrayList<>();
    public static List<Block> mangroveBlocks = new ArrayList<>();
    public static List<Block> windsweptGravellyHillBlocks = new ArrayList<>();
    public static List<Block> voidBlocks = new ArrayList<>();

    public static final Codec<OverworldGenerator> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(BiomeSource.CODEC.fieldOf("biome_source").forGetter((generator) -> {
            return generator.biomeSource;
        }), ChunkGeneratorSettings.REGISTRY_CODEC.fieldOf("settings").forGetter((generator) -> {
            return generator.settings;
        })).apply(instance, instance.stable(OverworldGenerator::new));
    });
    public OverworldGenerator(BiomeSource biomeSource, RegistryEntry<ChunkGeneratorSettings> settings) {
        super(biomeSource, settings);

        this.settings = settings;
        this.fluidLevelSampler = Suppliers.memoize(() -> {
            return createFluidLevelSampler(settings.value());
        });
        this.indexedFeaturesListSupplier = Suppliers.memoize(() -> PlacedFeatureIndexer.collectIndexedFeatures(List.copyOf(biomeSource.getBiomes()), biomeEntry -> biomeEntry.value().getGenerationSettings().getFeatures(), true));
    }

    @Override
    public int getHeight(int x, int z, Heightmap.Type heightmap, HeightLimitView world, NoiseConfig noiseConfig) {
        for(int i = Math.min(worldBlocks.size(), world.getTopY()) - 1; i >= 0; --i) {
            BlockState blockState = worldBlocks.get(i).getDefaultState();
            if (blockState != null && heightmap.getBlockPredicate().test(blockState)) {
                return world.getBottomY() + i + 1;
            }
        }

        return world.getBottomY();
    }

    @Override
    public VerticalBlockSample getColumnSample(int x, int z, HeightLimitView world, NoiseConfig noiseConfig) {
        return new VerticalBlockSample(world.getBottomY(), worldBlocks.stream().limit(world.getHeight()).map((state) -> state == null ? Blocks.AIR.getDefaultState() : state.getDefaultState()).toArray(BlockState[]::new));
    }
    @Override
    public CompletableFuture<Chunk> populateNoise(
            Executor executor, Blender blender, NoiseConfig noiseConfig, StructureAccessor accessor, Chunk chunk) {
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        Heightmap heightmap = chunk.getHeightmap(Heightmap.Type.OCEAN_FLOOR_WG);
        Heightmap heightmap2 = chunk.getHeightmap(Heightmap.Type.WORLD_SURFACE_WG);
        Random random = new Random();
        Registry<Biome> biomeRegistry = accessor.getRegistryManager().get(RegistryKeys.BIOME);
        for (int i = 0; i < Math.min(chunk.getHeight(), worldBlocks.size()); ++i) {
            BlockState blockState;
            for (int k = 0; k < 16; ++k) {
                for (int l = 0; l < 16; ++l) {
                    mutable.set(k, i - 63, l);
                    Biome currentBiome = chunk.getBiomeForNoiseGen(k >> 2, i, l >> 2).value();
                    if (currentBiome.equals(biomeRegistry.get(BiomeKeys.RIVER))) {
                        blockState = riverBlocks.get(i).getDefaultState();
                    } else if (currentBiome.equals(biomeRegistry.get(BiomeKeys.MUSHROOM_FIELDS))) {
                        blockState = myceliumBlocks.get(i).getDefaultState();
                    } else if (currentBiome.equals(biomeRegistry.get(BiomeKeys.WINDSWEPT_GRAVELLY_HILLS))) {
                        blockState = windsweptGravellyHillBlocks.get(i).getDefaultState();
                    } else if (currentBiome.equals(biomeRegistry.get(BiomeKeys.DESERT)) || currentBiome.equals(biomeRegistry.get(BiomeKeys.SNOWY_BEACH))) {
                        blockState = desertBlocks.get(i).getDefaultState();
                    } else if (currentBiome.equals(biomeRegistry.get(BiomeKeys.STONY_SHORE)) || currentBiome.equals(biomeRegistry.get(BiomeKeys.JAGGED_PEAKS)) || currentBiome.equals(biomeRegistry.get(BiomeKeys.FROZEN_PEAKS)) || currentBiome.equals(biomeRegistry.get(BiomeKeys.STONY_PEAKS))) {
                        blockState = stoneBlocks.get(i).getDefaultState();
                    } else if (currentBiome.equals(biomeRegistry.get(BiomeKeys.BADLANDS)) || currentBiome.equals(biomeRegistry.get(BiomeKeys.WOODED_BADLANDS)) || currentBiome.equals(biomeRegistry.get(BiomeKeys.ERODED_BADLANDS))) {
                        blockState = badlandsBlocks.get(i).getDefaultState();
                    } else if (currentBiome.equals(biomeRegistry.get(BiomeKeys.BEACH))) {
                        blockState = beachBlocks.get(i).getDefaultState();
                    } else if (currentBiome.equals(biomeRegistry.get(BiomeKeys.DEEP_FROZEN_OCEAN)) || currentBiome.equals(biomeRegistry.get(BiomeKeys.DEEP_COLD_OCEAN)) || currentBiome.equals(biomeRegistry.get(BiomeKeys.DEEP_OCEAN)) || currentBiome.equals(biomeRegistry.get(BiomeKeys.DEEP_LUKEWARM_OCEAN))) {
                        blockState = deepOceanBlocks.get(i).getDefaultState();
                    } else if (currentBiome.equals(biomeRegistry.get(BiomeKeys.OCEAN)) || currentBiome.equals(biomeRegistry.get(BiomeKeys.COLD_OCEAN)) || currentBiome.equals(biomeRegistry.get(BiomeKeys.FROZEN_OCEAN)) || currentBiome.equals(biomeRegistry.get(BiomeKeys.LUKEWARM_OCEAN)) || currentBiome.equals(biomeRegistry.get(BiomeKeys.WARM_OCEAN))) {
                        blockState = shallowOceanBlocks.get(i).getDefaultState();
                    } else if (currentBiome.equals(biomeRegistry.get(BiomeKeys.FROZEN_RIVER))) {
                        blockState = riverBlocks.get(i).getDefaultState();
                    } else if (currentBiome.equals(biomeRegistry.get(BiomeKeys.ICE_SPIKES))) {
                        blockState = iceSpikeBlocks.get(i).getDefaultState();
                    } else if (currentBiome.equals(biomeRegistry.get(BiomeKeys.SWAMP))) {
                        if (random.nextBoolean()) {
                            blockState = worldBlocks.get(i).getDefaultState();
                        } else {
                            blockState = swampBlocks.get(i).getDefaultState();
                        }
                    } else if (currentBiome.equals(biomeRegistry.get(BiomeKeys.MANGROVE_SWAMP))) {
                        blockState = mangroveBlocks.get(i).getDefaultState();
                    } else if (currentBiome.equals(biomeRegistry.get(BiomeKeys.DEEP_DARK)) || currentBiome.equals(biomeRegistry.get(BiomeKeys.DRIPSTONE_CAVES)) || currentBiome.equals(biomeRegistry.get(BiomeKeys.LUSH_CAVES))) {
                        blockState = voidBlocks.get(i).getDefaultState();
                    } else {
                        blockState = worldBlocks.get(i).getDefaultState();
                    }
                    chunk.setBlockState(mutable, blockState, false);
                    heightmap.trackUpdate(k, i - 63, l, blockState);
                    heightmap2.trackUpdate(k, i - 63, l, blockState);
                }
            }
        }

        return CompletableFuture.completedFuture(chunk);
    }
    @Override
    public void carve(ChunkRegion chunkRegion, long seed, NoiseConfig noiseConfig, BiomeAccess biomeAccess, StructureAccessor structureAccessor, Chunk chunk, GenerationStep.Carver carverStep) {
    }
    @Override
    protected Codec<? extends OverworldGenerator> getCodec() {
        return CODEC;
    }

    static {
        worldBlocks.add(Blocks.BEDROCK);
        for (int i = 0; i < 65; i++) {worldBlocks.add(Blocks.DEEPSLATE);}
        for (int i = 0; i < 65; i++) {worldBlocks.add(Blocks.STONE);}
        for (int i = 0; i < 11; i++) {worldBlocks.add(Blocks.DIRT);}
        worldBlocks.add(Blocks.GRASS_BLOCK);

        windsweptGravellyHillBlocks.add(Blocks.BEDROCK);
        for (int i = 0; i < 65; i++) {windsweptGravellyHillBlocks.add(Blocks.DEEPSLATE);}
        for (int i = 0; i < 65; i++) {windsweptGravellyHillBlocks.add(Blocks.STONE);}
        for (int i = 0; i < 12; i++) {windsweptGravellyHillBlocks.add(Blocks.GRAVEL);}

        riverBlocks.add(Blocks.BEDROCK);
        for (int i = 0; i < 65; i++) {riverBlocks.add(Blocks.DEEPSLATE);}
        for (int i = 0; i < 65; i++) {riverBlocks.add(Blocks.STONE);}
        for (int i = 0; i < 6; i++) {riverBlocks.add(Blocks.DIRT);}
        for (int i = 0; i < 7; i++) {riverBlocks.add(Blocks.WATER);}
        riverBlocks.add(Blocks.WATER);

        iceSpikeBlocks.add(Blocks.BEDROCK);
        for (int i = 0; i < 65; i++) {iceSpikeBlocks.add(Blocks.DEEPSLATE);}
        for (int i = 0; i < 65; i++) {iceSpikeBlocks.add(Blocks.STONE);}
        for (int i = 0; i < 11; i++) {iceSpikeBlocks.add(Blocks.DIRT);}
        iceSpikeBlocks.add(Blocks.SNOW_BLOCK);

        myceliumBlocks.add(Blocks.BEDROCK);
        for (int i = 0; i < 65; i++) {myceliumBlocks.add(Blocks.DEEPSLATE);}
        for (int i = 0; i < 65; i++) {myceliumBlocks.add(Blocks.STONE);}
        for (int i = 0; i < 11; i++) {myceliumBlocks.add(Blocks.DIRT);}
        myceliumBlocks.add(Blocks.MYCELIUM);

        badlandsBlocks.add(Blocks.BEDROCK);
        for (int i = 0; i < 65; i++) {badlandsBlocks.add(Blocks.DEEPSLATE);}
        for (int i = 0; i < 65; i++) {badlandsBlocks.add(Blocks.STONE);}
        for (int i = 0; i < 12; i++) {badlandsBlocks.add(Blocks.TERRACOTTA);}

        deepOceanBlocks.add(Blocks.BEDROCK);
        for (int i = 0; i < 65; i++) {deepOceanBlocks.add(Blocks.DEEPSLATE);}
        for (int i = 0; i < 37; i++) {deepOceanBlocks.add(Blocks.STONE);}
        for (int i = 0; i < 40; i++) {deepOceanBlocks.add(Blocks.WATER);}

        shallowOceanBlocks.add(Blocks.BEDROCK);
        for (int i = 0; i < 65; i++) {shallowOceanBlocks.add(Blocks.DEEPSLATE);}
        for (int i = 0; i < 57; i++) {shallowOceanBlocks.add(Blocks.STONE);}
        for (int i = 0; i < 20; i++) {shallowOceanBlocks.add(Blocks.WATER);}

        desertBlocks.add(Blocks.BEDROCK);
        for (int i = 0; i < 65; i++) {desertBlocks.add(Blocks.DEEPSLATE);}
        for (int i = 0; i < 65; i++) {desertBlocks.add(Blocks.STONE);}
        for (int i = 0; i < 12; i++) {desertBlocks.add(Blocks.SAND);}

        beachBlocks.add(Blocks.BEDROCK);
        for (int i = 0; i < 65; i++) {beachBlocks.add(Blocks.DEEPSLATE);}
        for (int i = 0; i < 65; i++) {beachBlocks.add(Blocks.STONE);}
        for (int i = 0; i < 7; i++) {beachBlocks.add(Blocks.DIRT);}
        for (int i = 0; i < 6; i++) {beachBlocks.add(Blocks.SAND);}

        stoneBlocks.add(Blocks.BEDROCK);
        for (int i = 0; i < 65; i++) {stoneBlocks.add(Blocks.DEEPSLATE);}
        for (int i = 0; i < 78; i++) {stoneBlocks.add(Blocks.STONE);}

        swampBlocks.add(Blocks.BEDROCK);
        for (int i = 0; i < 65; i++) {swampBlocks.add(Blocks.DEEPSLATE);}
        for (int i = 0; i < 65; i++) {swampBlocks.add(Blocks.STONE);}
        for (int i = 0; i < 9; i++) {swampBlocks.add(Blocks.DIRT);}
        for (int i = 0; i < 4; i++) {swampBlocks.add(Blocks.WATER);}

        mangroveBlocks.add(Blocks.BEDROCK);
        for (int i = 0; i < 65; i++) {mangroveBlocks.add(Blocks.DEEPSLATE);}
        for (int i = 0; i < 65; i++) {mangroveBlocks.add(Blocks.STONE);}
        for (int i = 0; i < 11; i++) {mangroveBlocks.add(Blocks.DIRT);}
        mangroveBlocks.add(Blocks.MUD);

        for (int i = 0; i < 143; i++) {voidBlocks.add(Blocks.CAVE_AIR);}
    }
}
