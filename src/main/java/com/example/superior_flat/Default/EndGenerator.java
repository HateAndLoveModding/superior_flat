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
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.util.math.random.RandomSeed;
import net.minecraft.util.math.random.Xoroshiro128PlusPlusRandom;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
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

public class EndGenerator extends NoiseChunkGenerator {
    public final RegistryEntry<ChunkGeneratorSettings> settings;
    public final Supplier<AquiferSampler.FluidLevelSampler> fluidLevelSampler;
    public final Supplier<List<PlacedFeatureIndexer.IndexedFeatures>> indexedFeaturesListSupplier;
    public static List<Block> endVoidBlocks = new ArrayList<>();
    public static List<Block> endBlocks = new ArrayList<>();
    public static List<Block> endBlocks3 = new ArrayList<>();
    public static List<Block> endBlocks7 = new ArrayList<>();
    public static List<Block> endBlocks11 = new ArrayList<>();
    public static List<Block> endBlocks15 = new ArrayList<>();
    public static List<Block> endBlocks19 = new ArrayList<>();
    public EndGenerator(BiomeSource biomeSource, RegistryEntry<ChunkGeneratorSettings> settings) {
        super(biomeSource, settings);

        this.settings = settings;
        this.fluidLevelSampler = Suppliers.memoize(() -> {
            return createFluidLevelSampler(settings.value());
        });
        this.indexedFeaturesListSupplier = Suppliers.memoize(() -> PlacedFeatureIndexer.collectIndexedFeatures(List.copyOf(biomeSource.getBiomes()), biomeEntry -> biomeEntry.value().getGenerationSettings().getFeatures(), true));
    }
    public static final Codec<EndGenerator> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(BiomeSource.CODEC.fieldOf("biome_source").forGetter((generator) -> {
            return generator.biomeSource;
        }), ChunkGeneratorSettings.REGISTRY_CODEC.fieldOf("settings").forGetter((generator) -> {
            return generator.settings;
        })).apply(instance, instance.stable(EndGenerator::new));
    });    @Override
    public int getHeight(int x, int z, Heightmap.Type heightmap, HeightLimitView world, NoiseConfig noiseConfig) {
        for(int i = Math.min(endBlocks11.size(), world.getTopY()) - 1; i >= 0; --i) {
            BlockState blockState = endBlocks11.get(i).getDefaultState();
            if (blockState != null && heightmap.getBlockPredicate().test(blockState)) {
                return world.getBottomY() + i + 1;
            }
        }

        return world.getBottomY();
    }

    @Override
    public VerticalBlockSample getColumnSample(int x, int z, HeightLimitView world, NoiseConfig noiseConfig) {
        return new VerticalBlockSample(world.getBottomY(), endBlocks11.stream().limit(world.getHeight()).map((state) -> state == null ? Blocks.AIR.getDefaultState() : state.getDefaultState()).toArray(BlockState[]::new));
    }
    @Override
    public CompletableFuture<Chunk> populateNoise(
            Executor executor, Blender blender, NoiseConfig noiseConfig, StructureAccessor accessor, Chunk chunk) {
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        Heightmap heightmap = chunk.getHeightmap(Heightmap.Type.MOTION_BLOCKING);
        Heightmap heightmap2 = chunk.getHeightmap(Heightmap.Type.WORLD_SURFACE_WG);
        Random random = new Random();
        int randomNumber = random.nextInt(5) + 1;

        Registry<Biome> biomeRegistry = accessor.getRegistryManager().get(RegistryKeys.BIOME);
        for (int i = 0; i < Math.min(chunk.getHeight(), endBlocks11.size()); ++i) {
            BlockState blockState;
            for (int k = 0; k < 16; ++k) {
                for (int l = 0; l < 16; ++l) {
                    mutable.set(k, i, l);
                    Biome currentBiome = chunk.getBiomeForNoiseGen(k >> 2, i, l >> 2).value();
                    if (currentBiome.equals(biomeRegistry.get(BiomeKeys.END_HIGHLANDS)) || currentBiome.equals(biomeRegistry.get(BiomeKeys.SMALL_END_ISLANDS))) {
                        if (randomNumber == 1) {
                            blockState = endBlocks3.get(i).getDefaultState();
                        } else if (randomNumber == 2) {
                            blockState = endBlocks7.get(i).getDefaultState();
                        } else if (randomNumber == 3) {
                            blockState = endBlocks11.get(i).getDefaultState();
                        } else if (randomNumber == 4) {
                            blockState = endBlocks15.get(i).getDefaultState();
                        } else {
                            blockState = endBlocks19.get(i).getDefaultState();
                        }
                    } else if (currentBiome.equals(biomeRegistry.get(BiomeKeys.THE_END))) {
                        blockState = endBlocks.get(i).getDefaultState();
                    } else {
                        blockState = endVoidBlocks.get(i).getDefaultState();
                    }
                    chunk.setBlockState(mutable, blockState, false);
                    heightmap.trackUpdate(k, i, l, blockState);
                    heightmap2.trackUpdate(k, i, l, blockState);
                }
            }
        }
        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    protected Codec<? extends EndGenerator> getCodec() {
        return CODEC;
    }

    static {
        for (int i = 0; i < 70; i++) {endVoidBlocks.add(Blocks.AIR);}

        for (int i = 0; i < 46; i++) {endBlocks.add(Blocks.AIR);}
        for (int i = 0; i < 3; i++) {endBlocks.add(Blocks.END_STONE);}
        for (int i = 0; i < 21; i++) {endBlocks.add(Blocks.AIR);}

        for (int i = 0; i < 66; i++) {endBlocks3.add(Blocks.AIR);}
        for (int i = 0; i < 4; i++) {endBlocks3.add(Blocks.END_STONE);}

        for (int i = 0; i < 62; i++) {endBlocks7.add(Blocks.AIR);}
        for (int i = 0; i < 8; i++) {endBlocks7.add(Blocks.END_STONE);}

        for (int i = 0; i < 58; i++) {endBlocks11.add(Blocks.AIR);}
        for (int i = 0; i < 12; i++) {endBlocks11.add(Blocks.END_STONE);}

        for (int i = 0; i < 54; i++) {endBlocks15.add(Blocks.AIR);}
        for (int i = 0; i < 16; i++) {endBlocks15.add(Blocks.END_STONE);}

        for (int i = 0; i < 50; i++) {endBlocks19.add(Blocks.AIR);}
        for (int i = 0; i < 20; i++) {endBlocks19.add(Blocks.END_STONE);}
    }

}
