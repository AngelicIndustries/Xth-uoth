package com.aurilux.xar.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.aurilux.xar.Xthuoth;
import com.aurilux.xar.handler.GuiHandler;
import com.aurilux.xar.tileentity.TileEntityImmixer;
import com.aurilux.xar.util.ResourceUtils;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** This block combines individual potions into one potion with all the potion effects */
public class BlockImmixer extends BlockContainer {

    // Assigned by the client proxy; the dedicated server does not register renderers.
    public static int renderId = -1;

    public BlockImmixer() {
        super(Material.iron);
        setLightOpacity(0);
        setBlockBounds(0.0625F, 0.0F, 0.0625F, 0.9375F, 0.9375F, 0.9375F);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int p_149915_2_) {
        return new TileEntityImmixer();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        this.blockIcon = ResourceUtils.getIcon(reg, "immixer");
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public int getRenderType() {
        return renderId;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(World world, int x, int y, int z, Random random) {
        Blocks.brewing_stand.randomDisplayTick(world, x, y, z, random);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7,
        float par8, float par9) {
        if (!player.isSneaking()) {
            if (world.getTileEntity(x, y, z) instanceof TileEntityImmixer) {
                player.openGui(Xthuoth.instance, GuiHandler.GUI_ID_IMMIXER, world, x, y, z);
            }
        }
        return true;
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int p_149749_6_) {
        TileEntity tileentity = world.getTileEntity(x, y, z);

        if (tileentity instanceof TileEntityImmixer) {
            TileEntityImmixer tileEntityImmixer = (TileEntityImmixer) tileentity;

            for (int i = 0; i < tileEntityImmixer.getSizeInventory(); i++) {
                ItemStack itemstack = tileEntityImmixer.getStackInSlot(i);

                if (itemstack != null) {
                    float dirX = world.rand.nextFloat() * 0.8F + 0.1F;
                    float dirY = world.rand.nextFloat() * 0.8F + 0.1F;
                    float dirZ = world.rand.nextFloat() * 0.8F + 0.1F;

                    while (itemstack.stackSize > 0) {
                        int stackAmount = world.rand.nextInt(21) + 10;
                        if (stackAmount > itemstack.stackSize) {
                            stackAmount = itemstack.stackSize;
                        }
                        itemstack.stackSize -= stackAmount;

                        EntityItem entityitem = new EntityItem(
                            world,
                            (double) ((float) x + dirX),
                            (double) ((float) y + dirY),
                            (double) ((float) z + dirZ),
                            new ItemStack(itemstack.getItem(), stackAmount, itemstack.getItemDamage()));
                        float f3 = 0.05F;
                        entityitem.motionX = (double) ((float) world.rand.nextGaussian() * f3);
                        entityitem.motionY = (double) ((float) world.rand.nextGaussian() * f3 + 0.2F);
                        entityitem.motionZ = (double) ((float) world.rand.nextGaussian() * f3);
                        world.spawnEntityInWorld(entityitem);
                    }
                }
            }
        }
        super.breakBlock(world, x, y, z, block, p_149749_6_);
    }

}
