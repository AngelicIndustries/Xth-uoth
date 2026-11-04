package com.aurilux.xar.handler;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.util.StatCollector;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import com.aurilux.xar.XARModInfo;
import com.aurilux.xar.Xthuoth;
import com.aurilux.xar.init.XARPotions;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class ConfigHandler {

    // config settings
    public static int RADIANT_ENCH_ID;
    public static int BLIGHTER_ENTITY_ID;
    public static int ABERRANT_SLIME_ENTITY_ID;
    public static int DIM_ID;
    public static int ABERRANT_BIOME_ID;
    public static int POISON_IMMUNITY_POTION_ID;
    public static boolean GENERATE_PRYSMAL_ORE = true;
    public static int PRYSMAL_ORE_HARVEST_LEVEL = 2;
    public static volatile Set<String> ADDITIONAL_ABERRATIONS = Collections.emptySet();
    public static volatile boolean RIFT_TAINT_SPREAD = true;
    public static volatile boolean RIFT_MOB_SPAWNING = true;
    public static volatile String[] ICHOR_CONTACT_EFFECTS = new String[0];
    public static boolean IMMIXER_3D_BOTTLES = false;
    public static boolean ABERRANT_SLIME_SHADERS = true;

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
        // These entities only need fixed mod-local network IDs, not configurable global IDs.
        config.getCategory(category)
            .remove("rift_entity_id");
        config.getCategory(category)
            .remove("catalyst_entity_id");
        ABERRANT_SLIME_ENTITY_ID = loadIntProp(category, "aberrant_slime_entity_id", 104);
        Set<String> aberrations = new HashSet<>();
        for (String id : config.get(
            category,
            "additional_aberrations",
            new String[0],
            "Additional mobs that take Radiant's bonus damage. Enter exact, case-sensitive entity IDs as used by /summon, one per line, e.g. Zombie or Thaumcraft.Wisp. Blighters always qualify. Does not change spawning or existing creature types. Applies immediately; use the same list on the server and clients.")
            .setLanguageKey("additional_aberrations")
            .getStringList()) {
            if (!id.trim()
                .isEmpty()) aberrations.add(id.trim());
        }
        ADDITIONAL_ABERRATIONS = Collections.unmodifiableSet(aberrations);

        // WORLDGEN
        category = "worldgen";
        ABERRANT_BIOME_ID = loadIntPropClamped(category, "aberrant_biome_id", 40, 40, 255);
        DIM_ID = loadIntProp(category, "xthuoth_dim_id", DimensionManager.getNextFreeDimId());
        GENERATE_PRYSMAL_ORE = config.get(
            category,
            "generate_prysmal_ore",
            true,
            "Generate Prysmal ore in new Overworld chunks. Existing ore is not removed. Uses the server's setting in multiplayer.")
            .setLanguageKey("generate_prysmal_ore")
            .setRequiresWorldRestart(true)
            .getBoolean(true);

        // POTIONS
        if (config.hasKey("potions", "lifesight_potion_id")) {
            config.getCategory("potions")
                .remove("lifesight_potion_id");
        }
        POISON_IMMUNITY_POTION_ID = loadPotionId("poison_immunity_potion_id", "Poison Immunity");

        // MISC
        category = "misc";
        RADIANT_ENCH_ID = loadIntProp(category, "radiant_enchant_id", 15);
        PRYSMAL_ORE_HARVEST_LEVEL = Math.max(
            0,
            config.get(
                category,
                "prysmal_ore_harvest_level",
                2,
                "Pickaxe harvest level required for Prysmal ore: 0 = wood/gold, 1 = stone, 2 = iron, 3 = diamond. Higher levels may be provided by other mods. Use the same setting on the server and clients. Requires a game restart.",
                0,
                Integer.MAX_VALUE)
                .setLanguageKey("prysmal_ore_harvest_level")
                .setRequiresMcRestart(true)
                .getInt(2));
        RIFT_TAINT_SPREAD = config.get(
            category,
            "rift_taint_spread",
            true,
            "Allow rifts to spread tainted biomes and fibrous taint in the Overworld. Does not remove existing taint or affect other Thaumcraft spreading sources. Uses the server's setting in multiplayer; applies immediately.")
            .setLanguageKey("rift_taint_spread")
            .getBoolean(true);
        RIFT_MOB_SPAWNING = config.get(
            category,
            "rift_mob_spawning",
            true,
            "Allow rifts to spawn mobs from the Aberrant biome's monster list. Does not affect portal travel, taint spreading, natural mob spawning, or existing mobs. Uses the server's setting in multiplayer; applies immediately.")
            .setLanguageKey("rift_mob_spawning")
            .getBoolean(true);
        ICHOR_CONTACT_EFFECTS = config.get(
            category,
            "ichor_contact_effects",
            new String[0],
            "Potion effects given to creatures touching ichor, one per line as: effect, level, seconds. The effect is a potion name such as potion.moveSlowdown (or just moveSlowdown), or a numeric effect ID. Level 1 is the weakest. E.g. \"potion.moveSlowdown, 2, 5\" gives Slowness II for 5 seconds, reapplied whenever it wears off while the creature stays in ichor. Instant effects such as potion.heal and potion.harm are applied on contact and repeat every given number of seconds instead. Invalid entries are skipped and logged. Uses the server's setting in multiplayer; applies immediately.")
            .setLanguageKey("ichor_contact_effects")
            .getStringList()
            .clone();

        // CLIENT
        ABERRANT_SLIME_SHADERS = config.get(
            "client",
            "aberrant_slime_shaders",
            true,
            "Animate Aberrant Slimes with distortion and grain effects. Disable to use only the slime texture. Client-side only; applies immediately.")
            .setLanguageKey("aberrant_slime_shaders")
            .getBoolean(true);
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
    private static int loadPotionId(String identifier, String name) {
        int maxId = XARPotions.getMaxPotionId();
        return config.get(
            "potions",
            identifier,
            -1,
            name + " effect ID. Use -1 to choose and save an unused ID automatically, or an unused ID from 1 to "
                + maxId
                + ". IDs above 127 require the EndlessIDs potion module. Must match on the server and clients. Requires a game restart.",
            -1,
            maxId)
            .setLanguageKey(identifier)
            .setRequiresMcRestart(true)
            .getInt(-1);
    }

    public static void savePoisonImmunityId(int poisonImmunityId) {
        POISON_IMMUNITY_POTION_ID = poisonImmunityId;
        config.get("potions", "poison_immunity_potion_id", -1)
            .set(poisonImmunityId);
        if (config.hasChanged()) config.save();
    }

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
