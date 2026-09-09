package com.aurilux.xar.handler;

import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.entity.player.EntityPlayerMP;

import com.aurilux.xar.entity.EntityRift;
import com.aurilux.xar.world.TeleporterXthuoth;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public class RiftTravelHandler {

    private static final Map<EntityPlayerMP, Integer> chargeTicks = new WeakHashMap<EntityPlayerMP, Integer>();

    public static void touchRift(EntityPlayerMP player) {
        if (!chargeTicks.containsKey(player)) chargeTicks.put(player, 0);
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        // Player updates can run several times per tick when movement packets arrive together.
        for (Iterator<Map.Entry<EntityPlayerMP, Integer>> it = chargeTicks.entrySet()
            .iterator(); it.hasNext();) {
            Map.Entry<EntityPlayerMP, Integer> entry = it.next();
            EntityPlayerMP player = entry.getKey();
            int charge = entry.getValue();
            if (!isInsideRift(player) || player.timeUntilPortal > 0
                || player.isDead
                || player.ridingEntity != null
                || player.riddenByEntity != null) {
                // Match vanilla's gradual loss of portal progress after stepping out.
                charge = Math.max(0, charge - 4);
                if (charge == 0) it.remove();
                else entry.setValue(charge);
                continue;
            }

            if (charge++ < player.getMaxInPortalTime()) {
                entry.setValue(charge);
                continue;
            }

            it.remove();
            int targetDimension = player.dimension == ConfigHandler.DIM_ID ? 0 : ConfigHandler.DIM_ID;
            player.timeUntilPortal = player.getPortalCooldown();
            player.mcServer.getConfigurationManager()
                .transferPlayerToDimension(
                    player,
                    targetDimension,
                    new TeleporterXthuoth(player.mcServer.worldServerForDimension(targetDimension)));
        }
    }

    private static boolean isInsideRift(EntityPlayerMP player) {
        for (Object entity : player.worldObj.getEntitiesWithinAABB(EntityRift.class, player.boundingBox)) {
            if (!((EntityRift) entity).isDead) return true;
        }
        return false;
    }
}
