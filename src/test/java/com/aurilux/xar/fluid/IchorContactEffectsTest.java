package com.aurilux.xar.fluid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.potion.Potion;

import org.junit.Test;

import com.aurilux.xar.fluid.IchorContactEffects.Effect;

public class IchorContactEffectsTest {

    @Test
    public void parsesNamesIdsLevelsAndSeconds() {
        List<String> errors = new ArrayList<>();
        List<Effect> effects = IchorContactEffects.parse(
            new String[] { "potion.moveSlowdown, 2, 5", " POISON ,1,0.5", "20, 1, 1000000", "", "  " },
            Potion.potionTypes,
            errors);

        assertTrue(errors.toString(), errors.isEmpty());
        assertEquals(3, effects.size());
        assertEffect(effects.get(0), Potion.moveSlowdown, 1, 100);
        assertEffect(effects.get(1), Potion.poison, 0, 10);
        assertEffect(effects.get(2), Potion.wither, 0, 20000000);
    }

    @Test
    public void shortDurationsLastAtLeastOneTick() {
        List<String> errors = new ArrayList<>();
        List<Effect> effects = IchorContactEffects
            .parse(new String[] { "heal, 256, 0.01" }, Potion.potionTypes, errors);
        assertTrue(errors.toString(), errors.isEmpty());
        assertEffect(effects.get(0), Potion.heal, 255, 1);
    }

    @Test
    public void invalidEntriesAreReportedAndSkipped() {
        String[] entries = { "poison, 1", "poison, 1, 5, 2", "notAnEffect, 1, 5", "0, 1, 5", "99999999999, 1, 5",
            "poison, 0, 5", "poison, 257, 5", "poison, II, 5", "poison, 1, 0", "poison, 1, -3", "poison, 1, NaN",
            "poison, 1, 1000001", "poison, 1, soon", "regeneration, 1, 5" };
        List<String> errors = new ArrayList<>();
        List<Effect> effects = IchorContactEffects.parse(entries, Potion.potionTypes, errors);

        assertEquals(entries.length - 1, errors.size());
        assertEquals(1, effects.size());
        assertEffect(effects.get(0), Potion.regeneration, 0, 100);
        assertTrue(
            errors.get(2),
            errors.get(2)
                .startsWith("\"notAnEffect, 1, 5\": "));
    }

    @Test
    public void exactNamesWinButAmbiguousNamesNeedAnId() {
        Potion[] registry = new Potion[32];
        registry[19] = Potion.poison;
        registry[30] = createUnregisteredPotion(30, "poison");
        registry[31] = createUnregisteredPotion(31, "potion.poison");
        List<String> errors = new ArrayList<>();

        List<Effect> effects = IchorContactEffects
            .parse(new String[] { "poison, 1, 1", "potion.poison, 1, 1", "19, 1, 1" }, registry, errors);

        assertEquals(1, errors.size());
        assertTrue(
            errors.get(0),
            errors.get(0)
                .contains("use its ID"));
        assertSame(registry[30], effects.get(0).potion);
        assertSame(Potion.poison, effects.get(1).potion);
    }

    /** Potion's constructor adds it to the global registry, so put back whatever was in that slot. */
    private static Potion createUnregisteredPotion(int id, String name) {
        Potion previous = Potion.potionTypes[id];
        try {
            return new TestPotion(id).setPotionName(name);
        } finally {
            Potion.potionTypes[id] = previous;
        }
    }

    private static void assertEffect(Effect effect, Potion potion, int amplifier, int ticks) {
        assertSame(potion, effect.potion);
        assertEquals(amplifier, effect.amplifier);
        assertEquals(ticks, effect.ticks);
    }

    private static class TestPotion extends Potion {

        TestPotion(int id) {
            super(id, true, 0);
        }
    }
}
