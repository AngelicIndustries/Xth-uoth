package com.aurilux.xar.world.biome;

import java.util.ArrayList;
import java.util.Arrays;

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.biome.BiomeDecorator;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;

import com.aurilux.xar.entity.monster.EntityBlighter;
import com.aurilux.xar.init.XARBlocks;

import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.relauncher.ReflectionHelper;

public class BiomeGenAberrant extends BiomeGenBase {

    private static final Type ABERRANT = createAberrantType();

    public BiomeGenAberrant(int id) {
        super(id, true);
        this.setBiomeName("Aberrant");
        setDisableRain();
        this.topBlock = XARBlocks.aberrack;
        this.fillerBlock = XARBlocks.aberrack;

        // An explicit type prevents Forge from guessing PLAINS for this alien biome.
        BiomeDictionary.registerBiomeType(this, ABERRANT);

        // clear all of the spawn lists
        this.spawnableMonsterList.clear();
        this.spawnableCreatureList.clear();
        this.spawnableWaterCreatureList.clear();
        this.spawnableCaveCreatureList.clear();

        // add native entities
        EntityRegistry.addSpawn(EntityBlighter.class, 5, 1, 2, EnumCreatureType.monster, this);
    }

    private static Type createAberrantType() {
        Type type = Type.getType("ABERRANT");
        // Forge 1.7.10 grows this array to ordinal rather than ordinal + 1 for custom types.
        ArrayList<BiomeGenBase>[] biomesByType = ReflectionHelper
            .getPrivateValue(BiomeDictionary.class, null, "typeInfoList");
        if (biomesByType.length <= type.ordinal()) {
            ReflectionHelper.setPrivateValue(
                BiomeDictionary.class,
                null,
                Arrays.copyOf(biomesByType, type.ordinal() + 1),
                "typeInfoList");
        }
        return type;
    }

    @Override
    public BiomeDecorator createBiomeDecorator() {
        return getModdedBiomeDecorator(new BiomeDecoratorXthuoth());
    }
}
