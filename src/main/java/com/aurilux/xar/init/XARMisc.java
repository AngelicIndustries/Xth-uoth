package com.aurilux.xar.init;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.EnumHelper;

import com.aurilux.xar.XARModInfo;
import com.aurilux.xar.enchantment.EnchantmentRadiant;
import com.aurilux.xar.handler.ConfigHandler;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class XARMisc {

    public static EnumCreatureAttribute ABERRATION;
    public static ItemArmor.ArmorMaterial CRYSTAL;
    public static Enchantment radiant;

    public static final CreativeTabs tab = new CreativeTabs(XARModInfo.MOD_ID) {

        @Override
        public Item getTabIconItem() {
            return XARItems.riftCatalyst;
        }

        @Override
        @SideOnly(Side.CLIENT)
        public void displayAllReleventItems(List<ItemStack> items) {
            super.displayAllReleventItems(items);
            // The registered vanilla egg normally appears only in the miscellaneous tab.
            items.add(new ItemStack(Items.spawn_egg, 1, ConfigHandler.BLIGHTER_ENTITY_ID));
            items.add(new ItemStack(Items.spawn_egg, 1, ConfigHandler.ABERRANT_SLIME_ENTITY_ID));
        }
    };

    public static void init() {
        // enums
        ABERRATION = EnumHelper.addCreatureAttribute("ABERRATION");
        CRYSTAL = EnumHelper.addArmorMaterial("CRYSTAL", 10, new int[] { 2, 5, 4, 1 }, 15);

        // enchantments
        radiant = new EnchantmentRadiant(ConfigHandler.RADIANT_ENCH_ID, 5, 3);
    }
}
