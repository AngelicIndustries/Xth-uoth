package com.aurilux.xar.mixins.early;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.aurilux.xar.enchantment.EnchantmentRadiant;

@Mixin(EnchantmentHelper.class)
public abstract class MixinEnchantmentHelper {

    @Inject(method = "getEnchantmentModifierLiving", at = @At("RETURN"), cancellable = true, require = 1)
    private static void xthuoth$addRadiantDamage(EntityLivingBase attacker, EntityLivingBase target,
        CallbackInfoReturnable<Float> cir) {
        float bonus = EnchantmentRadiant.getAdditionalDamage(attacker, target);
        if (bonus != 0.0F) cir.setReturnValue(cir.getReturnValueF() + bonus);
    }
}
