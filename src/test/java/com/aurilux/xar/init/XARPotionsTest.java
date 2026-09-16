package com.aurilux.xar.init;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import net.minecraft.potion.Potion;

import org.junit.Test;

public class XARPotionsTest {

    @Test
    public void configuredIdsArePreservedAcrossSignedByteAndShortBoundaries() {
        Potion[] registry = new Potion[65536];
        for (int id : new int[] { 33, 127, 128, 255, 256, 32767, 32768, 65535 }) {
            assertEquals(id, XARPotions.resolvePotionId(id, registry, 65535));
        }
    }

    @Test
    public void automaticAllocationCanContinueBeyondVanillaRange() {
        Potion[] registry = occupiedRegistry(65536);
        registry[128] = null;
        registry[256] = null;
        assertEquals(128, XARPotions.resolvePotionId(-1, registry, 65535));
        registry[128] = Potion.moveSpeed;
        assertEquals(256, XARPotions.resolvePotionId(-1, registry, 65535));
    }

    @Test
    public void automaticAllocationCanUseTheHighestExtendedId() {
        Potion[] registry = occupiedRegistry(65536);
        registry[65535] = null;
        assertEquals(65535, XARPotions.resolvePotionId(-1, registry, 65535));
        registry[65535] = Potion.moveSpeed;
        assertThrows(IllegalStateException.class, () -> XARPotions.resolvePotionId(-1, registry, 65535));
    }

    @Test
    public void aLargerRegistryAloneDoesNotLiftTheVanillaLimit() {
        Potion[] registry = new Potion[65536];
        Arrays.fill(registry, 1, 128, Potion.moveSpeed);
        assertThrows(IllegalArgumentException.class, () -> XARPotions.resolvePotionId(128, registry, 127));
        assertThrows(IllegalStateException.class, () -> XARPotions.resolvePotionId(-1, registry, 127));
    }

    @Test
    public void occupiedExtendedIdsAreRejectedWithoutChangingTheRegistry() {
        Potion[] registry = new Potion[65536];
        registry[256] = Potion.moveSpeed;
        IllegalArgumentException error = assertThrows(
            IllegalArgumentException.class,
            () -> XARPotions.resolvePotionId(256, registry, 65535));
        assertTrue(
            error.getMessage()
                .contains("already used"));
        assertEquals(Potion.moveSpeed, registry[256]);
    }

    @Test
    public void invalidExtendedIdsAreRejected() {
        Potion[] registry = new Potion[65536];
        for (int id : new int[] { -2, 0, 65536, Integer.MAX_VALUE }) {
            assertThrows(IllegalArgumentException.class, () -> XARPotions.resolvePotionId(id, registry, 65535));
        }
    }

    @Test
    public void vanillaAllocationStillSupportsExpansionAndLowerGaps() {
        assertEquals(32, XARPotions.resolvePotionId(-1, new Potion[32], 127));
        Potion[] registry = occupiedRegistry(128);
        registry[7] = null;
        assertEquals(7, XARPotions.resolvePotionId(-1, registry, 127));
    }

    private static Potion[] occupiedRegistry(int length) {
        Potion[] registry = new Potion[length];
        Arrays.fill(registry, Potion.moveSpeed);
        return registry;
    }
}
