package com.example.superior_flat;

import com.example.superior_flat.Classic.ClassicEndGenerator;
import com.example.superior_flat.Classic.ClassicNetherGenerator;
import com.example.superior_flat.Classic.ClassicOverworldGenerator;
import com.example.superior_flat.Default.EndGenerator;
import com.example.superior_flat.Default.NetherGenerator;
import com.example.superior_flat.Default.OverworldGenerator;
import com.example.superior_flat.Leaves.LeavesEndGenerator;
import com.example.superior_flat.Leaves.LeavesGenerator;
import net.fabricmc.api.ModInitializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class superior_flat implements ModInitializer {
    //Default. Overworld ice structures not spawning in frozen ocean and deep frozen ocean
    //Eroded Badlands does not spawn spike features
    public static final String MOD_ID = "superior_flat";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    @Override
    public void onInitialize() {
        Registry.register(Registries.CHUNK_GENERATOR, new Identifier(superior_flat.MOD_ID, "overworld"), OverworldGenerator.CODEC);
        Registry.register(Registries.CHUNK_GENERATOR, new Identifier(superior_flat.MOD_ID, "nether"), NetherGenerator.CODEC);
        Registry.register(Registries.CHUNK_GENERATOR, new Identifier(superior_flat.MOD_ID, "end"), EndGenerator.CODEC);

        Registry.register(Registries.CHUNK_GENERATOR, new Identifier(superior_flat.MOD_ID, "classic_overworld"), ClassicOverworldGenerator.CODEC);
        Registry.register(Registries.CHUNK_GENERATOR, new Identifier(superior_flat.MOD_ID, "classic_nether"), ClassicNetherGenerator.CODEC);
        Registry.register(Registries.CHUNK_GENERATOR, new Identifier(superior_flat.MOD_ID, "classic_end"), ClassicEndGenerator.CODEC);

        Registry.register(Registries.CHUNK_GENERATOR, new Identifier(superior_flat.MOD_ID, "end_leaves"), LeavesEndGenerator.CODEC);
        Registry.register(Registries.CHUNK_GENERATOR, new Identifier(superior_flat.MOD_ID, "leaves"), LeavesGenerator.CODEC);
    }
}
