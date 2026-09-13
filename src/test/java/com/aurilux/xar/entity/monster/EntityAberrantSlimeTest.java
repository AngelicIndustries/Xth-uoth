package com.aurilux.xar.entity.monster;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.chunk.IChunkProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.aurilux.xar.handler.ConfigHandler;

import sun.misc.Unsafe;

public class EntityAberrantSlimeTest {

    private SpawnWorld world;
    private TestSlime slime;
    private int previousDimensionId;

    @Before
    public void setup() throws Exception {
        previousDimensionId = ConfigHandler.DIM_ID;
        // Avoid starting a server or generating chunks: only supply the world queries used by spawning.
        Field field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        world = (SpawnWorld) ((Unsafe) field.get(null)).allocateInstance(SpawnWorld.class);
        Field provider = World.class.getDeclaredField("provider");
        provider.setAccessible(true);
        provider.set(world, new WorldProviderSurface());
        ConfigHandler.DIM_ID = 7;
        world.provider.dimensionId = ConfigHandler.DIM_ID;
        world.difficultySetting = EnumDifficulty.NORMAL;
        world.spawned = new ArrayList<>();
        slime = new TestSlime(world);
        slime.setPosition(0, 64, 0);
    }

    @After
    public void restoreConfig() {
        ConfigHandler.DIM_ID = previousDimensionId;
    }

    @Test
    public void naturalSpawningRequiresXthuothAndNonPeacefulDifficulty() {
        assertTrue(slime.getCanSpawnHere());
        world.provider.dimensionId = 0;
        assertFalse(slime.getCanSpawnHere());
        world.provider.dimensionId = ConfigHandler.DIM_ID;
        world.difficultySetting = EnumDifficulty.PEACEFUL;
        assertFalse(slime.getCanSpawnHere());
    }

    @Test
    public void naturalSpawningRejectsBrightOccupiedAndLiquidLocations() {
        world.light = 15;
        assertFalse(slime.getCanSpawnHere());
        world.light = 0;
        world.entityCollision = true;
        assertFalse(slime.getCanSpawnHere());
        world.entityCollision = false;
        world.blockCollision = true;
        assertFalse(slime.getCanSpawnHere());
        world.blockCollision = false;
        world.liquid = true;
        assertFalse(slime.getCanSpawnHere());
        world.liquid = false;
        assertTrue(slime.getCanSpawnHere());
    }

    @Test
    public void dyingLargeSlimeSplitsIntoSmallerAberrantSlimes() {
        slime.resize(4);
        slime.setHealth(0);
        slime.setDead();
        assertTrue(world.spawned.size() >= 2 && world.spawned.size() <= 4);
        for (Entity child : world.spawned) {
            assertEquals(EntityAberrantSlime.class, child.getClass());
            assertSame(world, child.worldObj);
            assertEquals(2, ((EntitySlime) child).getSlimeSize());
        }
    }

    @Test
    public void smallestSlimeDoesNotSplitAndSizeSurvivesSaving() {
        slime.resize(4);
        NBTTagCompound tag = new NBTTagCompound();
        slime.writeEntityToNBT(tag);
        TestSlime restored = new TestSlime(world);
        restored.readEntityFromNBT(tag);
        assertEquals(4, restored.getSlimeSize());
        assertEquals("portal", restored.getSlimeParticle());
        restored.resize(1);
        restored.setHealth(0);
        restored.setDead();
        assertTrue(world.spawned.isEmpty());
    }

    private static class TestSlime extends EntityAberrantSlime {

        TestSlime(World world) {
            super(world);
        }

        void resize(int size) {
            setSlimeSize(size);
        }
    }

    private static class SpawnWorld extends World {

        int light;
        boolean entityCollision;
        boolean blockCollision;
        boolean liquid;
        List<Entity> spawned;

        private SpawnWorld() {
            super(null, "unused", null, new WorldProviderSurface(), new Profiler());
        }

        @Override
        public int getSavedLightValue(EnumSkyBlock type, int x, int y, int z) {
            return 0;
        }

        @Override
        public int getBlockLightValue(int x, int y, int z) {
            return light;
        }

        @Override
        public float getLightBrightness(int x, int y, int z) {
            return light / 15.0F;
        }

        @Override
        public boolean isThundering() {
            return false;
        }

        @Override
        public boolean checkNoEntityCollision(AxisAlignedBB box) {
            return !entityCollision;
        }

        @Override
        public List<AxisAlignedBB> getCollidingBoundingBoxes(Entity entity, AxisAlignedBB box) {
            return blockCollision ? Collections.singletonList(box) : Collections.emptyList();
        }

        @Override
        public boolean isAnyLiquid(AxisAlignedBB box) {
            return liquid;
        }

        @Override
        public boolean spawnEntityInWorld(Entity entity) {
            spawned.add(entity);
            return true;
        }

        @Override
        protected IChunkProvider createChunkProvider() {
            throw new AssertionError("Unexpected chunk generation");
        }

        @Override
        protected int func_152379_p() {
            return 0;
        }

        @Override
        public Entity getEntityByID(int id) {
            return null;
        }
    }
}
