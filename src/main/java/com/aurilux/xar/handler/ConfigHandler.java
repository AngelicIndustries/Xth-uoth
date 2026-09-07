package com.aurilux.xar.handler;

import java.io.File;

import net.minecraft.util.StatCollector;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import com.aurilux.xar.XARModInfo;
import com.aurilux.xar.Xthuoth;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class ConfigHandler {

    // config settings
    public static int RADIANT_ENCH_ID;
    public static int BLIGHTER_ENTITY_ID;
    public static int RIFT_ENTITY_ID;
    public static int CATALYST_ENTITY_ID;
    public static int DIM_ID;
    public static int ABERRANT_BIOME_ID;
    public static boolean IMMIXER_3D_BOTTLES = false;

    public static Configuration config;

    public static void init(File configFile) {
        if (config == null) {
            config = new Configuration(configFile);
            loadConfig();
        }
    }

    private static void loadConfig() {
        // ENTITY
        String category = "entities";
        BLIGHTER_ENTITY_ID = loadIntProp(category, "blighter_entity_id", 101);
        RIFT_ENTITY_ID = loadIntProp(category, "rift_entity_id", 102);
        CATALYST_ENTITY_ID = loadIntProp(category, "catalyst_entity_id", 103);

        // WORLDGEN
        category = "worldgen";
        ABERRANT_BIOME_ID = loadIntPropClamped(category, "aberrant_biome_id", 40, 40, 255);
        DIM_ID = loadIntProp(category, "xthuoth_dim_id", DimensionManager.getNextFreeDimId());

        // MISC
        category = "misc";
        RADIANT_ENCH_ID = loadIntProp(category, "radiant_enchant_id", 15);

        // CLIENT
        IMMIXER_3D_BOTTLES = config.get(
            "client",
            "immixer_3d_bottles",
            false,
            "Render Immixer bottles, holders and upper bars in 3D. Disabled uses flat sprites like a vanilla brewing stand. Client-side only; applies immediately.")
            .setLanguageKey("immixer_3d_bottles")
            .getBoolean(false);

        if (config.hasChanged()) {
            config.save();
        }
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.modID.equalsIgnoreCase(XARModInfo.MOD_ID)) {
            // resync configs
            boolean previousBottles3D = IMMIXER_3D_BOTTLES;
            loadConfig();
            if (previousBottles3D != IMMIXER_3D_BOTTLES) {
                Xthuoth.proxy.refreshBlockRenderers();
            }
        }
    }

    //// HELPERS
    private static int loadIntProp(String category, String identifier, int defaultValue) {
        Property prop = config
            .get(category, identifier, defaultValue, StatCollector.translateToLocal(identifier + ".desc"));
        return prop.getInt();
    }

    private static int loadIntPropClamped(String category, String identifier, int defaultValue, int minValue,
        int maxValue) {
        Property prop = config.get(
            category,
            identifier,
            defaultValue,
            StatCollector.translateToLocal(identifier + ".desc"),
            minValue,
            maxValue);
        return prop.getInt();
    }
    //// END HELPERS
}
