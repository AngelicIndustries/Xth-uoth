package com.aurilux.xar.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderSnowball;
import net.minecraftforge.common.MinecraftForge;

import com.aurilux.xar.block.BlockImmixer;
import com.aurilux.xar.client.renderer.block.RenderImmixer;
import com.aurilux.xar.client.renderer.entity.RenderBlighter;
import com.aurilux.xar.client.renderer.entity.RenderRift;
import com.aurilux.xar.entity.EntityRift;
import com.aurilux.xar.entity.item.EntityRiftCatalyst;
import com.aurilux.xar.entity.monster.EntityBlighter;
import com.aurilux.xar.handler.EntityHandler;
import com.aurilux.xar.handler.WorldHandler;
import com.aurilux.xar.init.XARItems;

import cpw.mods.fml.client.registry.RenderingRegistry;

public class XARClientProxy extends XARServerProxy {

    @Override
    public void init() {
        super.init();
        // register client-side components such as renderers, key handlers, etc
        registerHandlers();
        registerRenderers();
    }

    private void registerHandlers() {
        MinecraftForge.EVENT_BUS.register(new WorldHandler());
        MinecraftForge.EVENT_BUS.register(new EntityHandler());
    }

    @Override
    public void refreshBlockRenderers() {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.theWorld != null) {
            minecraft.renderGlobal.loadRenderers();
        }
    }

    private void registerRenderers() {
        BlockImmixer.renderId = RenderingRegistry.getNextAvailableRenderId();
        RenderingRegistry.registerBlockHandler(new RenderImmixer(BlockImmixer.renderId));
        RenderingRegistry.registerEntityRenderingHandler(EntityRift.class, new RenderRift());
        RenderingRegistry
            .registerEntityRenderingHandler(EntityRiftCatalyst.class, new RenderSnowball(XARItems.riftCatalyst, 0));
        RenderingRegistry.registerEntityRenderingHandler(EntityBlighter.class, new RenderBlighter());
    }
}
