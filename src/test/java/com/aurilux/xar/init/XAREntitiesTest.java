package com.aurilux.xar.init;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;

import org.apache.logging.log4j.LogManager;
import org.junit.Test;

import com.aurilux.xar.XARModInfo;
import com.aurilux.xar.Xthuoth;
import com.aurilux.xar.entity.EntityRift;
import com.aurilux.xar.entity.item.EntityRiftCatalyst;
import com.aurilux.xar.entity.monster.EntityAberrantSlime;
import com.aurilux.xar.entity.monster.EntityBlighter;
import com.aurilux.xar.handler.ConfigHandler;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.Multimap;

import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.EntityRegistry.EntityRegistration;
import cpw.mods.fml.relauncher.FMLRelaunchLog;
import sun.misc.Unsafe;

public class XAREntitiesTest {

    @Test
    public void spawnEggIdsCannotChangeNetworkEntityTypes() throws Exception {
        EntityRegistry registry = EntityRegistry.instance();
        List<Runnable> restore = new ArrayList<>();
        snapshotMaps(EntityList.class, null, restore);
        snapshotMaps(EntityRegistry.class, registry, restore);
        Field indices = field(EntityRegistry.class, "availableIndicies");
        BitSet previousIndices = (BitSet) ((BitSet) indices.get(registry)).clone();
        Field loaderInstance = field(Loader.class, "instance");
        Object previousLoader = loaderInstance.get(null);
        Field loggingConfigured = field(FMLRelaunchLog.class, "configured");
        Object previousLoggingConfigured = loggingConfigured.get(null);
        Field logger = field(FMLRelaunchLog.class, "myLog");
        Object previousLogger = logger.get(FMLRelaunchLog.log);
        Xthuoth previousMod = Xthuoth.instance;
        int previousBlighterId = ConfigHandler.BLIGHTER_ENTITY_ID;
        int previousSlimeId = ConfigHandler.ABERRANT_SLIME_ENTITY_ID;

        try {
            // Use the test logger without FML replacing the process's stdout/stderr streams.
            logger.set(FMLRelaunchLog.log, LogManager.getLogger("FML"));
            loggingConfigured.set(null, true);
            // Supply only the mod lookup needed by Forge registration, without launching Minecraft.
            Unsafe unsafe = (Unsafe) field(Unsafe.class, "theUnsafe").get(null);
            Loader loader = (Loader) unsafe.allocateInstance(Loader.class);
            loaderInstance.set(null, loader);
            LoadController controller = new LoadController(loader);
            field(Loader.class, "modController").set(loader, controller);
            ModMetadata metadata = new ModMetadata();
            metadata.modId = XARModInfo.MOD_ID;
            DummyModContainer container = new DummyModContainer(metadata);
            Xthuoth.instance = new Xthuoth();
            field(LoadController.class, "modObjectList")
                .set(controller, ImmutableBiMap.of(container, Xthuoth.instance));

            // Deliberately overlap the rift/catalyst network IDs with configurable mob egg IDs.
            // Before the fix, Forge resolves the rift's spawn packet to the slime registered first.
            ConfigHandler.ABERRANT_SLIME_ENTITY_ID = 102;
            ConfigHandler.BLIGHTER_ENTITY_ID = 103;
            XAREntities.init();

            Set<Integer> networkIds = new HashSet<>();
            List<Class<? extends Entity>> types = new ArrayList<>();
            types.add(EntityBlighter.class);
            types.add(EntityRift.class);
            types.add(EntityRiftCatalyst.class);
            types.add(EntityAberrantSlime.class);
            for (Class<? extends Entity> type : types) {
                EntityRegistration registration = registry.lookupModSpawn(type, false);
                assertTrue(
                    "Duplicate network ID for " + type.getSimpleName(),
                    networkIds.add(registration.getModEntityId()));
                assertSame(
                    type,
                    registry.lookupModSpawn(container, registration.getModEntityId())
                        .getEntityClass());
            }

            assertEquals(
                102,
                registry.lookupModSpawn(EntityRift.class, false)
                    .getModEntityId());
            assertEquals(
                103,
                registry.lookupModSpawn(EntityRiftCatalyst.class, false)
                    .getModEntityId());
            assertSame(EntityAberrantSlime.class, EntityList.IDtoClassMapping.get(102));
            assertSame(EntityBlighter.class, EntityList.IDtoClassMapping.get(103));
            assertTrue(EntityList.entityEggs.containsKey(102));
            assertTrue(EntityList.entityEggs.containsKey(103));
            // Preserve the names stored in existing worlds.
            assertEquals("Xthuoth.Rift", EntityList.classToStringMapping.get(EntityRift.class));
            assertEquals("Xthuoth.Rift Catalyst", EntityList.classToStringMapping.get(EntityRiftCatalyst.class));
            assertEquals("AberrantSlime", EntityList.classToStringMapping.get(EntityAberrantSlime.class));
            assertEquals("Blighter", EntityList.classToStringMapping.get(EntityBlighter.class));
        } finally {
            for (Runnable action : restore) action.run();
            indices.set(registry, previousIndices);
            loaderInstance.set(null, previousLoader);
            logger.set(FMLRelaunchLog.log, previousLogger);
            loggingConfigured.set(null, previousLoggingConfigured);
            Xthuoth.instance = previousMod;
            ConfigHandler.BLIGHTER_ENTITY_ID = previousBlighterId;
            ConfigHandler.ABERRANT_SLIME_ENTITY_ID = previousSlimeId;
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static void snapshotMaps(Class<?> type, Object target, List<Runnable> restore) throws Exception {
        for (Field field : type.getDeclaredFields()) {
            field.setAccessible(true);
            Object value = field.get(target);
            if (value instanceof Map) {
                Map map = (Map) value;
                Map copy = new HashMap(map);
                restore.add(() -> {
                    map.clear();
                    map.putAll(copy);
                });
            } else if (value instanceof Multimap) {
                Multimap map = (Multimap) value;
                Multimap copy = ArrayListMultimap.create(map);
                restore.add(() -> {
                    map.clear();
                    map.putAll(copy);
                });
            }
        }
    }

    private static Field field(Class<?> type, String name) throws Exception {
        Field field = type.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }
}
