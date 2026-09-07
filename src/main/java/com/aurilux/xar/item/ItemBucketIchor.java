package com.aurilux.xar.item;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;

import com.aurilux.xar.init.XARBlocks;
import com.aurilux.xar.util.ResourceUtils;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemBucketIchor extends ItemBucket {

    public ItemBucketIchor() {
        super(XARBlocks.ichor);
        FluidContainerRegistry.registerFluidContainer(
            FluidRegistry.getFluidStack("ichor", FluidContainerRegistry.BUCKET_VOLUME),
            new ItemStack(this),
            new ItemStack(Items.bucket));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister reg) {
        this.itemIcon = ResourceUtils.getIcon(reg, "bucketIchor");
    }
}
