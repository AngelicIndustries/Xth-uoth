package com.aurilux.xar.client.gui.config;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;

import com.aurilux.xar.XARModInfo;
import com.aurilux.xar.handler.ConfigHandler;

import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.IConfigElement;

public class XARGuiConfig extends GuiConfig {

    public XARGuiConfig(GuiScreen screen) {
        super(
            screen,
            getConfigElements(),
            XARModInfo.MOD_ID,
            false,
            false,
            GuiConfig.getAbridgedConfigPath(ConfigHandler.config.toString()));
    }

    private static List<IConfigElement> getConfigElements() {
        List<IConfigElement> elements = new ArrayList();
        elements.addAll(new ConfigElement(ConfigHandler.config.getCategory("worldgen")).getChildElements());
        elements.addAll(new ConfigElement(ConfigHandler.config.getCategory("entities")).getChildElements());
        elements.addAll(new ConfigElement(ConfigHandler.config.getCategory("misc")).getChildElements());
        return elements;
    }
}
