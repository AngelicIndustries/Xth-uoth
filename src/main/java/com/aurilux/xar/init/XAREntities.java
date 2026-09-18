package com.aurilux.xar.init;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;

import com.aurilux.xar.Xthuoth;
import com.aurilux.xar.entity.EntityRift;
import com.aurilux.xar.entity.item.EntityRiftCatalyst;
import com.aurilux.xar.entity.monster.EntityAberrantSlime;
import com.aurilux.xar.entity.monster.EntityBlighter;
import com.aurilux.xar.handler.ConfigHandler;

import cpw.mods.fml.common.registry.EntityRegistry;

public class XAREntities {

    // Network IDs are local to this mod and must be identical on both sides. Keep them stable and
    // separate from configurable global spawn-egg IDs, which can overlap these values safely.
    private static final int BLIGHTER_ID = 101;
    private static final int RIFT_ID = 102;
    private static final int CATALYST_ID = 103;
    private static final int ABERRANT_SLIME_ID = 104;

    public static void init() {
        // mobs
        addMobEntity(
            EntityBlighter.class,
            "Blighter",
            BLIGHTER_ID,
            ConfigHandler.BLIGHTER_ENTITY_ID,
            64,
            3,
            true,
            0x7043D9,
            0);
        addMobEntity(
            EntityAberrantSlime.class,
            "AberrantSlime",
            ABERRANT_SLIME_ID,
            ConfigHandler.ABERRANT_SLIME_ENTITY_ID,
            64,
            3,
            true,
            0x161020,
            0x9876BA);

        // other entities
        addEntity(EntityRift.class, "Rift", RIFT_ID, 160, 3, false);
        addEntity(EntityRiftCatalyst.class, "Rift Catalyst", CATALYST_ID, 64, 10, true);
    }

    //// HELPERS
    private static void addEntity(Class<? extends Entity> cl, String name, int entityId, int trackingRange,
        int updateFrequency, boolean sendsVelocityUpdates) {
        EntityRegistry.registerModEntity(
            cl,
            name,
            entityId,
            Xthuoth.instance,
            trackingRange,
            updateFrequency,
            sendsVelocityUpdates);
    }

    private static void addMobEntity(Class<? extends EntityLiving> cl, String name, int entityId, int globalEntityId,
        int trackingRange, int updateFrequency, boolean sendsVelocityUpdates, int primaryColor, int secondaryColor) {
        // registering the mob egg
        EntityRegistry.registerGlobalEntityID(cl, name, globalEntityId, primaryColor, secondaryColor);

        addEntity(cl, name, entityId, trackingRange, updateFrequency, sendsVelocityUpdates);
    }
    //// END HELPERS
}
