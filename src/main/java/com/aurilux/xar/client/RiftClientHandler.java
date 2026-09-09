package com.aurilux.xar.client;

import java.util.Arrays;

import net.minecraft.client.Minecraft;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraftforge.event.world.WorldEvent;

import com.aurilux.xar.handler.ConfigHandler;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public class RiftClientHandler {

    private Integer previousDimension;

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        if (!event.world.isRemote || event.world.provider.dimensionId != 0) return;

        Chunk placeholder = event.world.getChunkFromChunkCoords(0, 0);
        if (placeholder instanceof EmptyChunk) {
            // Before chunk packets arrive, sky rendering reads this shared placeholder. Unknown biomes (255)
            // invoke client world generation and race the integrated server's shared IntCache. Real chunks
            // receive their own biome arrays from the server; the placeholder must never generate biomes.
            Arrays.fill(placeholder.getBiomeArray(), (byte) BiomeGenBase.plains.biomeID);
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.theWorld == null || minecraft.thePlayer == null) {
            previousDimension = null;
            return;
        }

        int dimension = minecraft.thePlayer.dimension;
        if (previousDimension != null && previousDimension != dimension
            && (previousDimension == ConfigHandler.DIM_ID || dimension == ConfigHandler.DIM_ID)) {
            // Respawning in another dimension creates a client player without the server's portal cooldown.
            minecraft.thePlayer.timeUntilPortal = minecraft.thePlayer.getPortalCooldown();
        }
        previousDimension = dimension;
    }
}
