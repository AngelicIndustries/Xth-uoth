package com.aurilux.xar.world.biome;

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.biome.BiomeDecorator;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;

import com.aurilux.xar.entity.monster.EntityBlighter;
import com.aurilux.xar.init.XARBlocks;

import cpw.mods.fml.common.registry.EntityRegistry;

public class BiomeGenAberrant extends BiomeGenBase {

    public BiomeGenAberrant(int id) {
        super(id, true);
        this.setBiomeName("Aberrant");
        setDisableRain();
        this.topBlock = XARBlocks.aberrack;
        this.fillerBlock = XARBlocks.aberrack;

        // Explicit types prevent Forge from guessing PLAINS for this alien biome.
        BiomeDictionary.registerBiomeType(this, Type.MAGICAL, Type.SPOOKY, Type.WASTELAND, Type.DEAD, Type.COLD);

        // clear all of the spawn lists
        this.spawnableMonsterList.clear();
        this.spawnableCreatureList.clear();
        this.spawnableWaterCreatureList.clear();
        this.spawnableCaveCreatureList.clear();

        // add native entities
        EntityRegistry.addSpawn(EntityBlighter.class, 5, 1, 2, EnumCreatureType.monster, this);
    }

    @Override
    public BiomeDecorator createBiomeDecorator() {
        return getModdedBiomeDecorator(new BiomeDecoratorXthuoth());
    }
}
