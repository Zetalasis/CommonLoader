package com.zetalasis.commonloader;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;

@Mod(CommonLoader.MOD_ID)
public class CommonLoaderForge {
    
    public CommonLoaderForge() {
    
        // This method is invoked by the Forge mod loader when it is ready
        // to load your mod. You can access Forge and Common code in this
        // project.
    
        // Use Forge to bootstrap the Common mod.
        CommonLoader.LOGGER.info("Hello Forge world!");
        CommonLoader.init(FMLPaths.MODSDIR.get());
    }
}