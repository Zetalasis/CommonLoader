package com.zetalasis.commonloader;

import com.zetalasis.commonloader.loader.CommonModLoader;
import com.zetalasis.commonloader.platform.Services;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonLoader {
    public static final String MOD_ID = "commonloader";
    public static final String MOD_NAME = "CommonLoader";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public static void init() {
        if (!Services.PLATFORM.isModLoaded("commonloader"))
            return;

        LOGGER.info("Loading CommonLoader mods...");
        CommonModLoader.bootstrap();
    }
}