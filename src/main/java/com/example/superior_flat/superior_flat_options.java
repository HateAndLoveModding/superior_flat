package com.example.superior_flat;

import com.example.superior_flat.Classic.ClassicEndGenerator;
import com.example.superior_flat.Classic.ClassicNetherGenerator;
import com.example.superior_flat.Classic.ClassicOverworldGenerator;
import com.example.superior_flat.Default.EndGenerator;
import com.example.superior_flat.Default.NetherGenerator;
import com.example.superior_flat.Default.OverworldGenerator;
import com.example.superior_flat.Leaves.LeavesEndGenerator;
import com.example.superior_flat.Leaves.LeavesGenerator;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.world.biome.source.MultiNoiseBiomeSource;
import net.minecraft.world.biome.source.TheEndBiomeSource;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionTypes;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;

public class superior_flat_options {
    //Default. Overworld ice structures not spawning in frozen ocean and deep frozen ocean
    //Eroded Badlands does not spawn spike features

    public static final DimensionOptions OVERWORLD_OPTIONS = new DimensionOptions(
            BuiltinRegistries.DIMENSION_TYPE.getOrCreateEntry(DimensionTypes.OVERWORLD),
            new OverworldGenerator(BuiltinRegistries.STRUCTURE_SET, BuiltinRegistries.NOISE_PARAMETERS, MultiNoiseBiomeSource.Preset.OVERWORLD.getBiomeSource(BuiltinRegistries.BIOME), BuiltinRegistries.CHUNK_GENERATOR_SETTINGS.getOrCreateEntry(ChunkGeneratorSettings.OVERWORLD)));
    public static final DimensionOptions NETHER_OPTIONS = new DimensionOptions(
            BuiltinRegistries.DIMENSION_TYPE.getOrCreateEntry(DimensionTypes.THE_NETHER),
            new NetherGenerator(BuiltinRegistries.STRUCTURE_SET, BuiltinRegistries.NOISE_PARAMETERS, MultiNoiseBiomeSource.Preset.NETHER.getBiomeSource(BuiltinRegistries.BIOME), BuiltinRegistries.CHUNK_GENERATOR_SETTINGS.getOrCreateEntry(ChunkGeneratorSettings.NETHER)));
    public static final DimensionOptions END_OPTIONS = new DimensionOptions(
            BuiltinRegistries.DIMENSION_TYPE.getOrCreateEntry(DimensionTypes.THE_END),
            new EndGenerator(BuiltinRegistries.STRUCTURE_SET, BuiltinRegistries.NOISE_PARAMETERS, new TheEndBiomeSource(BuiltinRegistries.BIOME), BuiltinRegistries.CHUNK_GENERATOR_SETTINGS.getOrCreateEntry(ChunkGeneratorSettings.END)));
    public static final DimensionOptions LEAF_OVERWORLD_OPTIONS = new DimensionOptions(
            BuiltinRegistries.DIMENSION_TYPE.getOrCreateEntry(DimensionTypes.OVERWORLD),
            new LeavesGenerator(BuiltinRegistries.STRUCTURE_SET, BuiltinRegistries.NOISE_PARAMETERS, MultiNoiseBiomeSource.Preset.OVERWORLD.getBiomeSource(BuiltinRegistries.BIOME), BuiltinRegistries.CHUNK_GENERATOR_SETTINGS.getOrCreateEntry(ChunkGeneratorSettings.OVERWORLD)));
    public static final DimensionOptions LEAF_NETHER_OPTIONS = new DimensionOptions(
            BuiltinRegistries.DIMENSION_TYPE.getOrCreateEntry(DimensionTypes.THE_NETHER),
            new LeavesGenerator(BuiltinRegistries.STRUCTURE_SET, BuiltinRegistries.NOISE_PARAMETERS, MultiNoiseBiomeSource.Preset.NETHER.getBiomeSource(BuiltinRegistries.BIOME), BuiltinRegistries.CHUNK_GENERATOR_SETTINGS.getOrCreateEntry(ChunkGeneratorSettings.NETHER)));
    public static final DimensionOptions LEAF_END_OPTIONS = new DimensionOptions(
            BuiltinRegistries.DIMENSION_TYPE.getOrCreateEntry(DimensionTypes.THE_END),
            new LeavesEndGenerator(BuiltinRegistries.STRUCTURE_SET, BuiltinRegistries.NOISE_PARAMETERS, new TheEndBiomeSource(BuiltinRegistries.BIOME), BuiltinRegistries.CHUNK_GENERATOR_SETTINGS.getOrCreateEntry(ChunkGeneratorSettings.END)));

    public static final DimensionOptions CLASSIC_OVERWORLD_OPTIONS = new DimensionOptions(
            BuiltinRegistries.DIMENSION_TYPE.getOrCreateEntry(DimensionTypes.OVERWORLD),
            new ClassicOverworldGenerator(BuiltinRegistries.STRUCTURE_SET, BuiltinRegistries.NOISE_PARAMETERS, MultiNoiseBiomeSource.Preset.OVERWORLD.getBiomeSource(BuiltinRegistries.BIOME), BuiltinRegistries.CHUNK_GENERATOR_SETTINGS.getOrCreateEntry(ChunkGeneratorSettings.OVERWORLD)));
    public static final DimensionOptions CLASSIC_NETHER_OPTIONS = new DimensionOptions(
            BuiltinRegistries.DIMENSION_TYPE.getOrCreateEntry(DimensionTypes.THE_NETHER),
            new ClassicNetherGenerator(BuiltinRegistries.STRUCTURE_SET, BuiltinRegistries.NOISE_PARAMETERS, MultiNoiseBiomeSource.Preset.NETHER.getBiomeSource(BuiltinRegistries.BIOME), BuiltinRegistries.CHUNK_GENERATOR_SETTINGS.getOrCreateEntry(ChunkGeneratorSettings.NETHER)));
    public static final DimensionOptions CLASSIC_END_OPTIONS = new DimensionOptions(
            BuiltinRegistries.DIMENSION_TYPE.getOrCreateEntry(DimensionTypes.THE_END),
            new ClassicEndGenerator(BuiltinRegistries.STRUCTURE_SET, BuiltinRegistries.NOISE_PARAMETERS, new TheEndBiomeSource(BuiltinRegistries.BIOME), BuiltinRegistries.CHUNK_GENERATOR_SETTINGS.getOrCreateEntry(ChunkGeneratorSettings.END)));

}
