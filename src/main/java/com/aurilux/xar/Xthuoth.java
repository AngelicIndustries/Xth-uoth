package com.aurilux.xar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.aurilux.xar.fluid.IchorContactEffects;
import com.aurilux.xar.handler.ConfigHandler;
import com.aurilux.xar.init.XARAchievements;
import com.aurilux.xar.init.XARAspects;
import com.aurilux.xar.init.XARBlocks;
import com.aurilux.xar.init.XAREntities;
import com.aurilux.xar.init.XARFluids;
import com.aurilux.xar.init.XARItems;
import com.aurilux.xar.init.XARMisc;
import com.aurilux.xar.init.XARPotions;
import com.aurilux.xar.init.XARRecipes;
import com.aurilux.xar.init.XARResearch;
import com.aurilux.xar.init.XARWorldgen;
import com.aurilux.xar.proxy.XARServerProxy;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(
    modid = XARModInfo.MOD_ID,
    name = XARModInfo.MOD_NAME,
    version = XARModInfo.MOD_VERSION,
    guiFactory = XARModInfo.GUI_FACTORY,
    dependencies = XARModInfo.MOD_DEPEND,
    customProperties = { @Mod.CustomProperty(k = "license", v = "LGPLv3"),
        @Mod.CustomProperty(k = "issueTrackerUrl", v = "https://github.com/AngelicIndustries/Xth-uoth/issues"),
        @Mod.CustomProperty(k = "iconFile", v = "assets/xthuoth/icon.png"),
        @Mod.CustomProperty(k = "backgroundFile", v = "assets/xthuoth/background.png") })
public class Xthuoth {

    @Instance(XARModInfo.MOD_ID)
    public static Xthuoth instance;

    @SidedProxy(clientSide = XARModInfo.CLIENT_PROXY, serverSide = XARModInfo.SERVER_PROXY)
    public static XARServerProxy proxy;

    public static final Logger log = LogManager.getLogger(XARModInfo.MOD_NAME);

    @EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        ConfigHandler.init(e.getSuggestedConfigurationFile());
        // unlike most other event handlers, the 'ConfigHandler' must be initialized here in preInit to work properly
        FMLCommonHandler.instance()
            .bus()
            .register(new ConfigHandler());

        // pre-initialization
        XARMisc.init();
        XAREntities.init();
        XARFluids.init();
        XARBlocks.init();
        XARItems.init();
        XARPotions.init();
        XARAchievements.init();
    }

    @EventHandler
    public void init(FMLInitializationEvent e) {
        // initialization
        XARWorldgen.init();

        // register client-specific(renderers, tile entities, client event handlers, etc) and
        // server-specific(?) objects
        proxy.init();

        // Exclude foreign generation through the mods' supported dimension blacklist hooks.
        FMLInterModComms.sendMessage("Thaumcraft", "dimensionBlacklist", ConfigHandler.DIM_ID + ":0");
        FMLInterModComms.sendMessage("arsmagica2", "dwg", Integer.toString(ConfigHandler.DIM_ID));
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent e) {
        // post-initialization
        XARAspects.init();
        XARRecipes.init();
        XARResearch.init();
    }

    @EventHandler
    public void loadComplete(FMLLoadCompleteEvent e) {
        // Check the ichor effect list once every mod has registered its potions, so mistakes are logged at startup.
        IchorContactEffects.getEffects();
    }
}
