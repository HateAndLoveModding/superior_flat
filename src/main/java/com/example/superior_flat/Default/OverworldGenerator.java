package com.example.superior_flat.Default;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.structure.StructureSet;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.dynamic.RegistryOps;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.util.math.random.CheckedRandom;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.util.math.random.RandomSeed;
import net.minecraft.util.math.random.Xoroshiro128PlusPlusRandom;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryEntryList;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.HeightContext;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import net.minecraft.world.gen.chunk.VerticalBlockSample;
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
    public final Registry<DoublePerlinNoiseSampler.NoiseParameters> noiseRegistry;
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
    public static List<Block> voidBlocks = new ArrayList<>();

    public static final Codec<OverworldGenerator> CODEC =
            RecordCodecBuilder.create(
                    instance ->
                            NoiseChunkGenerator.createStructureSetRegistryGetter(instance).and(
                                            instance
                                                    .group(
                                                            RegistryOps.createRegistryCodec(Registry.NOISE_KEY).forGetter(generator -> generator.noiseRegistry),
                                                            (BiomeSource.CODEC.fieldOf("biome_source")).forGetter(OverworldGenerator::getBiomeSource),
                                                            (ChunkGeneratorSettings.REGISTRY_CODEC.fieldOf("settings")).forGetter(OverworldGenerator::getSettings)))
                                    .apply(instance, instance.stable(OverworldGenerator::new)));

    public OverworldGenerator(Registry<StructureSet> structureSetRegistry, Registry<DoublePerlinNoiseSampler.NoiseParameters> noiseRegistry, BiomeSource populationSource, RegistryEntry<ChunkGeneratorSettings> settings) {
        super(structureSetRegistry, noiseRegistry, populationSource, settings);

        this.noiseRegistry = noiseRegistry;
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
    public void populateEntities(ChunkRegion region) {
        if (!((ChunkGeneratorSettings)this.settings.value()).mobGenerationDisabled()) {
            ChunkPos chunkPos = region.getCenterPos();
            RegistryEntry<Biome> registryEntry = region.getBiome(chunkPos.getStartPos().withY(region.getTopY() - 1));
            ChunkRandom chunkRandom = new ChunkRandom(new CheckedRandom(RandomSeed.getSeed()));
            chunkRandom.setPopulationSeed(region.getSeed(), chunkPos.getStartX(), chunkPos.getStartZ());
            SpawnHelper.populateEntities(region, registryEntry, chunkPos, chunkRandom);
        }
    }
    @Override
    public CompletableFuture<Chunk> populateNoise(
            Executor executor, Blender blender, NoiseConfig noiseConfig, StructureAccessor accessor, Chunk chunk) {
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        Heightmap heightmap = chunk.getHeightmap(Heightmap.Type.OCEAN_FLOOR_WG);
        Heightmap heightmap2 = chunk.getHeightmap(Heightmap.Type.WORLD_SURFACE_WG);
        Random random = new Random();
        Registry<Biome> biomeRegistry = accessor.getRegistryManager().get(Registry.BIOME_KEY);
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
                    } else if (currentBiome.equals(biomeRegistry.get(BiomeKeys.DESERT))) {
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
    public void buildSurface(Chunk chunk, HeightContext heightContext, NoiseConfig noiseConfig, StructureAccessor structureAccessor, BiomeAccess biomeAccess, Registry<Biome> biomeRegistry, Blender blender) {}

    @Override
    public void carve(ChunkRegion chunkRegion, long seed, NoiseConfig noiseConfig, BiomeAccess biomeAccess, StructureAccessor structureAccessor, Chunk chunk, GenerationStep.Carver carverStep) {
    }

    @Override
    public void buildSurface(ChunkRegion region, StructureAccessor structures, NoiseConfig noiseConfig, Chunk chunk) {
    }
    @Override
    protected Codec<? extends OverworldGenerator> getCodec() {
        return CODEC;
    }
    @Override
    public void generateFeatures(StructureWorldAccess world, Chunk chunk, StructureAccessor structureAccessor) {
        ChunkPos chunkPos = chunk.getPos();
        ChunkSectionPos chunkSectionPos = ChunkSectionPos.from(chunkPos, world.getBottomSectionCoord());
        BlockPos minChunkPos = chunkSectionPos.getMinPos();

        Registry<Structure> structureRegistry = world.getRegistryManager().get(Registry.STRUCTURE_KEY);
        Map<Integer, List<Structure>> structuresByStep = structureRegistry.stream().collect(Collectors.groupingBy(structureType -> structureType.getFeatureGenerationStep().ordinal()));
        List<PlacedFeatureIndexer.IndexedFeatures> indexedFeatures = this.indexedFeaturesListSupplier.get();

        ChunkRandom chunkRandom = new ChunkRandom(new Xoroshiro128PlusPlusRandom(RandomSeed.getSeed()));
        long populationSeed = chunkRandom.setPopulationSeed(world.getSeed(), minChunkPos.getX(), minChunkPos.getZ());

        // Get all surrounding biomes for biome-based structures
        ObjectArraySet<Biome> biomeSet = new ObjectArraySet<>();
        ChunkPos.stream(chunkSectionPos.toChunkPos(), 1).forEach(curChunkPos -> {
            Chunk curChunk = world.getChunk(curChunkPos.x, curChunkPos.z);
            for (ChunkSection chunkSection : curChunk.getSectionArray()) {
                chunkSection.getBiomeContainer().forEachValue(registryEntry -> biomeSet.add(registryEntry.value()));
            }
        });
        biomeSet.retainAll(this.biomeSource.getBiomes().stream().map(RegistryEntry::value).collect(Collectors.toSet()));

        int numIndexedFeatures = indexedFeatures.size();
        try {
            Registry<PlacedFeature> placedFeatures = world.getRegistryManager().get(Registry.PLACED_FEATURE_KEY);
            int numSteps = Math.max(GenerationStep.Feature.values().length, numIndexedFeatures);
            for (int genStep = 0; genStep < numSteps; ++genStep) {
                int m = 0;
                if (structureAccessor.shouldGenerateStructures()) {
                    List<Structure> structuresForStep = structuresByStep.getOrDefault(genStep, Collections.emptyList());
                    for (Structure structure : structuresForStep) {
                        chunkRandom.setDecoratorSeed(populationSeed, m, genStep);
                        Supplier<String> featureNameSupplier = () -> structureRegistry.getKey(structure).map(Object::toString).orElseGet(structure::toString);
                        world.setCurrentlyGeneratingStructureName(featureNameSupplier);
                        structureAccessor.getStructureStarts(chunkSectionPos, structure).forEach((start) -> {
                            start.place(world, structureAccessor, this, chunkRandom, getBlockBoxForChunk(chunk), chunkPos);
                        });
                        ++m;
                    }
                }
                if (genStep >= numIndexedFeatures) continue;
                IntArraySet intSet = new IntArraySet();
                for (Biome biome : biomeSet) {
                    List<RegistryEntryList<PlacedFeature>> biomeFeatureStepList = biome.getGenerationSettings().getFeatures();
                    if (genStep >= biomeFeatureStepList.size()) continue;
                    RegistryEntryList<PlacedFeature> biomeFeaturesForStep = biomeFeatureStepList.get(genStep);
                    PlacedFeatureIndexer.IndexedFeatures indexedFeature = indexedFeatures.get(genStep);
                    biomeFeaturesForStep.stream().map(RegistryEntry::value).forEach(placedFeature -> intSet.add(indexedFeature.indexMapping().applyAsInt(placedFeature)));
                }
                int n = intSet.size();
                int[] is = intSet.toIntArray();
                Arrays.sort(is);
                PlacedFeatureIndexer.IndexedFeatures indexedFeature = indexedFeatures.get(genStep);
                for (int o = 0; o < n; ++o) {
                    int p = is[o];
                    PlacedFeature placedFeature = indexedFeature.features().get(p);
                    Supplier<String> placedFeatureNameSupplier = () -> placedFeatures.getKey(placedFeature).map(Object::toString).orElseGet(placedFeature::toString);
                    chunkRandom.setDecoratorSeed(populationSeed, p, genStep);
                    world.setCurrentlyGeneratingStructureName(placedFeatureNameSupplier);
                    try {
                        placedFeature.generate(world, this, chunkRandom, minChunkPos);
                    } catch (Exception e) {
                        CrashReport crashReport = CrashReport.create(e, "Feature placement");
                        crashReport.addElement("Feature").add("Description", placedFeatureNameSupplier::get);
                        throw new CrashException(crashReport);
                    }
                }
            }
            world.setCurrentlyGeneratingStructureName(null);
        } catch (Exception e) {
            CrashReport crashReport = CrashReport.create(e, "Biome decoration");
            crashReport.addElement("Generation").add("CenterX", chunkPos.x).add("CenterZ", chunkPos.z).add("Seed", populationSeed);
            throw new CrashException(crashReport);
        }
    }

    private static BlockBox getBlockBoxForChunk(Chunk chunk) {
        ChunkPos chunkPos = chunk.getPos();
        int startX = chunkPos.getStartX();
        int startZ = chunkPos.getStartZ();
        HeightLimitView heightLimitView = chunk.getHeightLimitView();
        int bottomY = heightLimitView.getBottomY() + 1;
        int topY = heightLimitView.getTopY() - 1;
        return new BlockBox(startX, bottomY, startZ, startX + 15, topY, startZ + 15);
    }

    static {
        worldBlocks.add(Blocks.BEDROCK);
        for (int i = 0; i < 65; i++) {worldBlocks.add(Blocks.DEEPSLATE);}
        for (int i = 0; i < 65; i++) {worldBlocks.add(Blocks.STONE);}
        for (int i = 0; i < 11; i++) {worldBlocks.add(Blocks.DIRT);}
        worldBlocks.add(Blocks.GRASS_BLOCK);

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
        for (int i = 0; i < 65; i++) {deepOceanBlocks.add(Blocks.STONE);}
        for (int i = 0; i < 12; i++) {deepOceanBlocks.add(Blocks.WATER);}

        shallowOceanBlocks.add(Blocks.BEDROCK);
        for (int i = 0; i < 65; i++) {shallowOceanBlocks.add(Blocks.DEEPSLATE);}
        for (int i = 0; i < 65; i++) {shallowOceanBlocks.add(Blocks.STONE);}
        for (int i = 0; i < 4; i++) {shallowOceanBlocks.add(Blocks.DIRT);}
        for (int i = 0; i < 9; i++) {shallowOceanBlocks.add(Blocks.WATER);}

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
