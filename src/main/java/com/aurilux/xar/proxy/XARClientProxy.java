package com.aurilux.xar.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderSnowball;
import net.minecraftforge.common.MinecraftForge;

import com.aurilux.xar.block.BlockImmixer;
import com.aurilux.xar.client.AberrantSlimeShader;
import com.aurilux.xar.client.RiftClientHandler;
import com.aurilux.xar.client.renderer.block.RenderImmixer;
import com.aurilux.xar.client.renderer.entity.RenderAberrantSlime;
import com.aurilux.xar.client.renderer.entity.RenderBlighter;
import com.aurilux.xar.client.renderer.entity.RenderRift;
import com.aurilux.xar.entity.EntityRift;
import com.aurilux.xar.entity.item.EntityRiftCatalyst;
import com.aurilux.xar.entity.monster.EntityAberrantSlime;
import com.aurilux.xar.entity.monster.EntityBlighter;
import com.aurilux.xar.handler.EntityHandler;
import com.aurilux.xar.handler.WorldHandler;
import com.aurilux.xar.init.XARItems;

import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;

public class XARClientProxy extends XARServerProxy {

    @Override
    public void init() {
        super.init();
        // register client-side components such as renderers, key handlers, etc
        registerHandlers();
        registerRenderers();
    }

    private void registerHandlers() {
        FMLCommonHandler.instance()
            .bus()
            .register(new AberrantSlimeShader.AnimationClock());
        RiftClientHandler riftHandler = new RiftClientHandler();
        FMLCommonHandler.instance()
            .bus()
            .register(riftHandler);
        MinecraftForge.EVENT_BUS.register(riftHandler);
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
        RenderRift riftRenderer = new RenderRift();
        MinecraftForge.EVENT_BUS.register(riftRenderer);
        RenderingRegistry.registerEntityRenderingHandler(EntityRift.class, riftRenderer);
        RenderingRegistry
            .registerEntityRenderingHandler(EntityRiftCatalyst.class, new RenderSnowball(XARItems.riftCatalyst, 0));
        RenderingRegistry.registerEntityRenderingHandler(EntityBlighter.class, new RenderBlighter());
        RenderingRegistry.registerEntityRenderingHandler(EntityAberrantSlime.class, new RenderAberrantSlime());
    }
}
