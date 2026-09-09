package com.aurilux.xar.entity;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.BiomeGenBase.SpawnListEntry;

import com.aurilux.xar.Xthuoth;
import com.aurilux.xar.handler.ConfigHandler;
import com.aurilux.xar.handler.RiftTravelHandler;
import com.aurilux.xar.init.XARWorldgen;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import thaumcraft.common.blocks.BlockTaintFibres;
import thaumcraft.common.config.Config;
import thaumcraft.common.lib.utils.Utils;
import thaumcraft.common.lib.world.ThaumcraftWorldGenerator;

public class EntityRift extends Entity {

    private int tickCounter = 0;

    public EntityRift(World world) {
        super(world);
        this.preventEntitySpawning = true;
        this.setSize(0.6F, 1.8F);
    }

    @Override
    protected void entityInit() {}

    public EntityRift(World world, double x, double y, double z) {
        this(world);
        this.setPosition(x + .5D, y, z + .5D);
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    /**
     * Deliberately left empty because we don't want anything special happening when something collides
     * though I might use this instead of onCollideWithPlayer to allow other entities to join in the fun >:D
     */
    @Override
    public void applyEntityCollision(Entity entity) {}

    @Override
    public void onCollideWithPlayer(EntityPlayer player) {
        if (player.worldObj != worldObj || player.ridingEntity != null
            || player.riddenByEntity != null
            || !boundingBox.intersectsWith(player.boundingBox)) return;

        if (player.timeUntilPortal > 0) {
            // The cooldown must only expire after the player leaves the destination rift.
            player.timeUntilPortal = player.getPortalCooldown();
            return;
        }

        if (worldObj.isRemote) {
            // Use vanilla's portal distortion without sending the server to the Nether.
            player.setInPortal();
        } else if (player instanceof EntityPlayerMP) {
            RiftTravelHandler.touchRift((EntityPlayerMP) player);
        }
    }

    @Override
    public void onEntityUpdate() {
        // Spread taint just like tainted nodes, but twice as fast
        tickCounter++;
        if (!worldObj.isRemote && ConfigHandler.RIFT_TAINT_SPREAD && this.dimension == 0 && tickCounter % 25 == 0) {
            int y = 0;
            int x = MathHelper.floor_double(this.posX - .5D) + worldObj.rand.nextInt(8) - worldObj.rand.nextInt(8);
            int z = MathHelper.floor_double(this.posZ) + worldObj.rand.nextInt(8) - worldObj.rand.nextInt(8);
            BiomeGenBase bg = worldObj.getBiomeGenForCoords(x, z);
            if (bg.biomeID != ThaumcraftWorldGenerator.biomeTaint.biomeID) {
                Utils.setBiomeAt(worldObj, x, z, ThaumcraftWorldGenerator.biomeTaint);
            }
            if ((Config.hardNode) && (worldObj.rand.nextBoolean())) {
                x = MathHelper.floor_double(this.posX - .5D) + worldObj.rand.nextInt(5) - worldObj.rand.nextInt(5);
                y = MathHelper.floor_double(this.posY) + worldObj.rand.nextInt(5) - worldObj.rand.nextInt(5);
                z = MathHelper.floor_double(this.posZ) + worldObj.rand.nextInt(5) - worldObj.rand.nextInt(5);
                if (!BlockTaintFibres.spreadFibres(worldObj, x, y, z)) {}
            }
        }

        // Spawn a random aberration
        int spawnChance = 4000; // nether portals spawn chance is 2000, though entities seem to update more often than
                                // blocks
        if (!worldObj.isRemote && ConfigHandler.RIFT_MOB_SPAWNING
            && worldObj.provider.isSurfaceWorld()
            && worldObj.getGameRules()
                .getGameRuleBooleanValue("doMobSpawning")
            && worldObj.rand.nextInt(spawnChance) < worldObj.difficultySetting.getDifficultyId()) {
            // Get the list of aberrations
            List<SpawnListEntry> aberrations = XARWorldgen.aberrantBiome.getSpawnableList(EnumCreatureType.monster);
            if (aberrations.isEmpty()) return;
            Class<? extends EntityLiving> entityClass = aberrations.get(rand.nextInt(aberrations.size())).entityClass;

            Block blockBelow = worldObj.getBlock(
                MathHelper.floor_double(this.posX - .5D),
                MathHelper.floor_double(this.posY) - 1,
                MathHelper.floor_double(this.posZ));
            if (posY > 0 && blockBelow.isNormalCube()) {
                // Spawn the selected class: tracking distances and mod-local IDs are not global spawn-egg IDs.
                try {
                    EntityLiving entity = entityClass.getConstructor(World.class)
                        .newInstance(worldObj);
                    entity.setLocationAndAngles(
                        posX + 0.5D,
                        posY + .5D,
                        posZ + 0.5D,
                        MathHelper.wrapAngleTo180_float(worldObj.rand.nextFloat() * 360.0F),
                        0.0F);
                    entity.rotationYawHead = entity.rotationYaw;
                    entity.renderYawOffset = entity.rotationYaw;
                    entity.onSpawnWithEgg(null);
                    entity.timeUntilPortal = entity.getPortalCooldown();
                    if (worldObj.spawnEntityInWorld(entity)) entity.playLivingSound();
                } catch (ReflectiveOperationException e) {
                    Xthuoth.log.warn("Unable to spawn rift mob " + entityClass.getName(), e);
                }
            }
        }
    }

    /**
     * Checks if the entity is in range to render by using the past in distance and comparing it to its average edge
     * length * 64 * renderDistanceWeight
     * 
     * @param distance the distance the player is from the entity
     */
    @Override
    @SideOnly(Side.CLIENT)
    public boolean isInRangeToRenderDist(double distance) {
        double d1 = this.boundingBox.getAverageEdgeLength() * 4.0D;
        d1 *= 64.0D;
        return distance < d1 * d1;
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound p_70037_1_) {}

    @Override
    protected void writeEntityToNBT(NBTTagCompound p_70014_1_) {}
}
