package com.aurilux.xar.util;

import java.util.Arrays;

import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;

import com.falsepattern.endlessids.mixin.helpers.ChunkBiomeHook;

import cpw.mods.fml.common.Loader;

/** Biome initialization for newly generated chunks and the client's shared empty chunk. */
public final class ChunkBiomeUtils {

    private ChunkBiomeUtils() {}

    public static void fill(Chunk chunk, BiomeGenBase biome) {
        if (Loader.isModLoaded("endlessids_biome")) {
            EndlessIds.fill(chunk, biome);
        } else {
            Arrays.fill(chunk.getBiomeArray(), (byte) biome.biomeID);
        }
    }

    public static void setBiomes(Chunk chunk, BiomeGenBase[] biomes) {
        if (Loader.isModLoaded("endlessids_biome")) {
            EndlessIds.setBiomes(chunk, biomes);
        } else {
            byte[] biomeArray = chunk.getBiomeArray();
            for (int i = 0; i < biomeArray.length; i++) {
                biomeArray[i] = (byte) biomes[i].biomeID;
            }
        }
    }

    // Keep optional API references in a separate class, loaded only when the biome module is enabled.
    private static final class EndlessIds {

        private static void fill(Chunk chunk, BiomeGenBase biome) {
            Arrays.fill(((ChunkBiomeHook) chunk).getBiomeShortArray(), (short) biome.biomeID);
        }

        private static void setBiomes(Chunk chunk, BiomeGenBase[] biomes) {
            short[] biomeArray = ((ChunkBiomeHook) chunk).getBiomeShortArray();
            for (int i = 0; i < biomeArray.length; i++) {
                biomeArray[i] = (short) biomes[i].biomeID;
            }
        }
    }
}
