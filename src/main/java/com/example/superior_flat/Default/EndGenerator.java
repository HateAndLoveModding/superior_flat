package com.example.superior_flat.Default;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.structure.StructureSet;
import net.minecraft.util.dynamic.RegistryOps;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import net.minecraft.world.gen.chunk.VerticalBlockSample;
import net.minecraft.world.gen.feature.util.PlacedFeatureIndexer;
import net.minecraft.world.gen.noise.NoiseConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

public class EndGenerator extends NoiseChunkGenerator {
    public final Registry<DoublePerlinNoiseSampler.NoiseParameters> noiseRegistry;
    public final Supplier<List<PlacedFeatureIndexer.IndexedFeatures>> indexedFeaturesListSupplier;
    public static List<Block> endVoidBlocks = new ArrayList<>();
    public static List<Block> endBlocks = new ArrayList<>();
    public static List<Block> endBlocks3 = new ArrayList<>();
    public static List<Block> endBlocks7 = new ArrayList<>();
    public static List<Block> endBlocks11 = new ArrayList<>();
    public static List<Block> endBlocks15 = new ArrayList<>();
    public static List<Block> endBlocks19 = new ArrayList<>();
    public EndGenerator(Registry<StructureSet> structureSetRegistry, Registry<DoublePerlinNoiseSampler.NoiseParameters> noiseRegistry, BiomeSource populationSource, RegistryEntry<ChunkGeneratorSettings> settings) {
        super(structureSetRegistry, noiseRegistry, populationSource, settings);

        this.noiseRegistry = noiseRegistry;
        this.indexedFeaturesListSupplier = Suppliers.memoize(() -> PlacedFeatureIndexer.collectIndexedFeatures(List.copyOf(biomeSource.getBiomes()), biomeEntry -> biomeEntry.value().getGenerationSettings().getFeatures(), true));
    }
    public static final Codec<EndGenerator> CODEC =
            RecordCodecBuilder.create(instance -> NoiseChunkGenerator.createStructureSetRegistryGetter(instance).and(instance.group(
                            RegistryOps.createRegistryCodec(Registry.NOISE_KEY).forGetter(generator -> generator.noiseRegistry),
                            (BiomeSource.CODEC.fieldOf("biome_source")).forGetter(EndGenerator::getBiomeSource),
                            (ChunkGeneratorSettings.REGISTRY_CODEC.fieldOf("settings")).forGetter(EndGenerator::getSettings)))
                    .apply(instance, instance.stable(EndGenerator::new)));
    @Override
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

        Registry<Biome> biomeRegistry = accessor.getRegistryManager().get(Registry.BIOME_KEY);
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
