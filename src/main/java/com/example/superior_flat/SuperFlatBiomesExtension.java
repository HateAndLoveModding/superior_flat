package com.example.superior_flat;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class SuperFlatBiomesExtension implements ModInitializer {
    public static final String MOD_ID = "superior_flat";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    @Override
    public void onInitialize() {
        SuperFlatBiomesPresets.registerAll();
    }
}
