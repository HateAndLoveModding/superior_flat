package com.example.superior_flat;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.gen.WorldPreset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static com.example.superior_flat.superior_flat_options.*;

public class superior_flat implements ModInitializer {
    public static final String MOD_ID = "superior_flat";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final RegistryKey<WorldPreset> SUPERIOR_FLAT = RegistryKey.of(Registry.WORLD_PRESET_KEY, new Identifier(superior_flat.MOD_ID));
    public static final RegistryKey<WorldPreset> SUPERIOR_FLAT_LEAVES = RegistryKey.of(Registry.WORLD_PRESET_KEY, new Identifier("leaves"));
    public static final RegistryKey<WorldPreset> SUPERIOR_FLAT_CLASSIC = RegistryKey.of(Registry.WORLD_PRESET_KEY, new Identifier("classic"));

    @Override
    public void onInitialize() {
        BuiltinRegistries.add(BuiltinRegistries.WORLD_PRESET, SUPERIOR_FLAT, new WorldPreset(Map.of(DimensionOptions.OVERWORLD, OVERWORLD_OPTIONS, DimensionOptions.NETHER, NETHER_OPTIONS, DimensionOptions.END, END_OPTIONS)));
        BuiltinRegistries.add(BuiltinRegistries.WORLD_PRESET, SUPERIOR_FLAT_LEAVES, new WorldPreset(Map.of(DimensionOptions.OVERWORLD, LEAF_OVERWORLD_OPTIONS, DimensionOptions.NETHER, LEAF_NETHER_OPTIONS, DimensionOptions.END, LEAF_END_OPTIONS)));
        BuiltinRegistries.add(BuiltinRegistries.WORLD_PRESET, SUPERIOR_FLAT_CLASSIC, new WorldPreset(Map.of(DimensionOptions.OVERWORLD, CLASSIC_OVERWORLD_OPTIONS, DimensionOptions.NETHER, CLASSIC_NETHER_OPTIONS, DimensionOptions.END, CLASSIC_END_OPTIONS)));
    }
}
