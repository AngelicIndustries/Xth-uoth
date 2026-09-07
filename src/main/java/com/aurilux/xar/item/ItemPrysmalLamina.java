package com.aurilux.xar.item;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.Item;

import com.aurilux.xar.util.ResourceUtils;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemPrysmalLamina extends Item {

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister reg) {
        this.itemIcon = ResourceUtils.getIcon(reg, "prysmalLamina");
    }
}
