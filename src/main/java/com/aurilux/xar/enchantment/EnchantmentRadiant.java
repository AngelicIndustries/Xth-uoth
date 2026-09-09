package com.aurilux.xar.enchantment;

import net.minecraft.enchantment.EnchantmentDamage;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;

import com.aurilux.xar.handler.ConfigHandler;
import com.aurilux.xar.init.XARMisc;

public class EnchantmentRadiant extends EnchantmentDamage {

    public static float getAdditionalDamage(EntityLivingBase attacker, EntityLivingBase target) {
        // Native aberrations already receive Radiant's bonus through the normal creature-attribute calculation.
        if (XARMisc.radiant == null || target.getCreatureAttribute() == XARMisc.ABERRATION
            || !ConfigHandler.ADDITIONAL_ABERRATIONS.contains(EntityList.getEntityString(target))) return 0.0F;
        int level = EnchantmentHelper.getEnchantmentLevel(XARMisc.radiant.effectId, attacker.getHeldItem());
        return XARMisc.radiant.func_152376_a(level, XARMisc.ABERRATION);
    }

    public EnchantmentRadiant(int id, int weight, int damage) {
        super(id, weight, damage);
    }

    @Override
    public int getMinEnchantability(int level) {
        return 5 + (level - 1) * 8;
    }

    @Override
    public int getMaxEnchantability(int level) {
        return this.getMinEnchantability(level) + 20;
    }

    @Override
    public float func_152376_a(int level, EnumCreatureAttribute attr) {
        return attr == XARMisc.ABERRATION ? (float) level * 2.5F : 0.0F;
    }

    @Override
    public String getName() {
        return "enchantment.damage.aberration";
    }
}
