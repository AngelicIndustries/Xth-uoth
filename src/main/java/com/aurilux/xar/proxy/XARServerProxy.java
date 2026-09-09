package com.aurilux.xar.proxy;

import com.aurilux.xar.Xthuoth;
import com.aurilux.xar.handler.GuiHandler;
import com.aurilux.xar.handler.RiftTravelHandler;
import com.aurilux.xar.tileentity.TileEntityImmixer;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;

public class XARServerProxy {

    public void init() {
        FMLCommonHandler.instance()
            .bus()
            .register(new RiftTravelHandler());
        GameRegistry.registerTileEntity(TileEntityImmixer.class, "immixer");
        NetworkRegistry.INSTANCE.registerGuiHandler(Xthuoth.instance, new GuiHandler());
    }

    public void refreshBlockRenderers() {
        // Rendering settings have no effect on the dedicated server.
    }
}
